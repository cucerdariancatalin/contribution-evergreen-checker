// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.evergreen.data

import android.content.Context
import android.os.Build.MODEL
import android.util.Log
import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.evergreen.R
import app.evergreen.config.EvergreenConfig
import app.evergreen.extensions.md5
import app.evergreen.services.AppServices.httpClient
import app.evergreen.services.log
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repo(private val context: Context) {
  private val evergreenConfigLiveData: MutableLiveData<EvergreenConfig> = MutableLiveData()

  private val errorsLiveData: MutableLiveData<FetchError> = MutableLiveData()

  val deviceUniqueId: String = MODEL

  @get:AnyThread
  val evergreenConfig: LiveData<EvergreenConfig>
    get() = evergreenConfigLiveData

  @get:AnyThread
  val errors: LiveData<FetchError>
    get() = errorsLiveData

  fun refreshFromServer() {
    log(TAG, "refreshFromServer")
    CoroutineScope(Dispatchers.Main).launch {
      withContext(Dispatchers.IO) {
        val configUrl = getConfigUrl(deviceUniqueId)
        Log.e(TAG, "| $configUrl | $deviceUniqueId |")
        var jsonString = httpClient.httpGet(configUrl)
        if (jsonString == null) {
          Log.i(TAG, deviceUniqueId)
          // If we don’t have a device-specific config for this device, then use a default config. It’ll include
          // the list of apps of interest, but without specific pinned versions listed.
          jsonString = httpClient.httpGet(context.getString(R.string.default_config_url))
        }

        // If the default.json could not be located, then still don’t raise a FetchError, since Evergreen’s Tools
        // can be used successfully even without network connectivity.
        if (jsonString == null) {
          Log.i(TAG, deviceUniqueId)
          return@withContext
        }

        log(TAG, jsonString)
        val json = try {
          EvergreenConfig.moshiAdapter().fromJson(jsonString)
        } catch (e: JsonEncodingException) {
          errorsLiveData.postValue(FetchError(deviceUniqueId, e.message ?: context.getString(R.string.unknown)))
          null
        } ?: return@withContext

        evergreenConfigLiveData.postValue(json)
      }
    }
  }

  fun getConfigUrl(deviceUniqueId: String) =
    context.getString(R.string.device_config_url, deviceUniqueId.md5())

  companion object {
    private const val TAG = "Repo"
  }
}

data class FetchError(val deviceUniqueId: String, val message: String)
