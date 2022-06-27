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

package app.evergreen.ui.tools

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import app.evergreen.R
import app.evergreen.config.EvergreenConfig
import app.evergreen.config.Kind.APK
import app.evergreen.config.Kind.REMOTE_FIRMWARE
import app.evergreen.config.Kind.SYSTEM_BUILD
import app.evergreen.config.Updatable
import app.evergreen.config.Version
import app.evergreen.extensions.drawable
import app.evergreen.extensions.toTargetSize
import app.evergreen.services.AppServices.repo
import app.evergreen.services.log
import app.evergreen.ui.BigTextFragment
import app.evergreen.ui.DialogOpener
import app.evergreen.ui.MAIN_IMAGE_SIZE_DP

class AppVersions(private val context: Context, private val dialogOpener: DialogOpener) : Tool {

  private fun getInstalledVersion(updatable: Updatable): String? = when (updatable.kind) {
    APK -> {
      try {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(updatable.id!!, 0).versionName
      } catch (e: PackageManager.NameNotFoundException) {
        null  // App not installed, so it does not have a version name yet.
      }
    }
    SYSTEM_BUILD -> Build.DISPLAY
    REMOTE_FIRMWARE -> null
  }

  override val titleText: String
    get() = context.getString(R.string.app_versions)

  override val mainImage: Drawable
    get() = context.drawable(R.drawable.code_json)!!
      .toBitmap(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
      .toTargetSize(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
      .toDrawable(context.resources)

  override fun doAction() {
    repo.evergreenConfig.observeForever { evergreenConfig ->
      evergreenConfig.updatables.forEach { updatable ->
        getInstalledVersion(updatable)?.let { installedVersion ->
          updatable.latestProd = Version(installedVersion)
        }
      }

      val json = EvergreenConfig.moshiAdapter().toJson(evergreenConfig)
      log(TAG, "URL: ${repo.getConfigUrl(repo.deviceUniqueId)}")
      log(TAG, json)
      dialogOpener.invoke(
        BigTextFragment.withText(
          context.getString(R.string.print_app_versions), evergreenConfig.toCompactString()
        ), BigTextFragment.TAG
      )
    }
  }

  companion object {
    private const val TAG = "AppVersions"
  }
}
