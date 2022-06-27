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

package app.evergreen.services

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpClient {
  private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .followRedirects(true)
      .followSslRedirects(true)
      .retryOnConnectionFailure(true)
      .build()
  }

  suspend fun httpGet(url: String): String? = withContext(Dispatchers.IO) {
    try {
      okHttpClient.newCall(
        Request.Builder().url(url)
          .cacheControl(CacheControl.Builder().noCache().build())
          .build()
      ).execute().use { response ->
        return@withContext if (response.isSuccessful) {
          response.body?.string()
        } else {
          null
        }
      }
    } catch (e: IOException) {
      log(TAG, e)
      return@withContext null
    }
  }

  companion object {
    private const val TAG = "HttpClient"
  }
}
