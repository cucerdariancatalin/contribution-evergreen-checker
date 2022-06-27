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

package app.evergreen.ui.updates

import android.content.Context
import android.graphics.drawable.Drawable
import app.evergreen.R
import app.evergreen.config.Updatable
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.CONFIGURATION_ERROR
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.NOT_INSTALLED
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.VERSION_IS_LATEST
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.VERSION_NEWER_THAN_LATEST
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.VERSION_OLDER_THAN_LATEST

abstract class AbstractUpdatableViewModel(
  protected val context: Context,
  protected val updatable: Updatable
) {

  open val availableVersion: String?
    get() = updatable.latestProd?.versionName

  open val installedVersion: String?
    get() = null

  open val installedVersionCode: Long?
    get() = null

  open val versionStatus: VersionStatus
    get() {
      val installedVersion = installedVersion
      val availableVersion = availableVersion
      return when {
        installedVersion == null -> NOT_INSTALLED
        availableVersion == null -> CONFIGURATION_ERROR
        installedVersion == availableVersion -> VERSION_IS_LATEST
        installedVersion < availableVersion -> VERSION_OLDER_THAN_LATEST
        installedVersion > availableVersion -> VERSION_NEWER_THAN_LATEST
        else -> CONFIGURATION_ERROR
      }
    }

  open val titleText: String
    get() = updatable.id ?: context.getString(R.string.unknown)

  open val contentText: String
    get() = installedVersion ?: context.getString(R.string.unknown)

  open val backgroundColor: Int
    get() = when (versionStatus) {
      VERSION_IS_LATEST -> R.color.green_900
      VERSION_OLDER_THAN_LATEST -> R.color.deep_orange_600
      VERSION_NEWER_THAN_LATEST -> R.color.green_400
      NOT_INSTALLED -> R.color.yellow_700
      CONFIGURATION_ERROR -> R.color.grey_900
    }

  abstract suspend fun getIcon(): Drawable

  open fun onUpdate(): Boolean = false

  open val dialogTitle: String
    get() = context.getString(
      when (versionStatus) {
        VERSION_OLDER_THAN_LATEST -> R.string.update_required
        VERSION_NEWER_THAN_LATEST -> R.string.newer_version_installed
        VERSION_IS_LATEST -> R.string.no_update_required
        NOT_INSTALLED -> R.string.app_not_installed
        CONFIGURATION_ERROR -> R.string.configuration_error
      }
    )

  open val dialogMessage: String
    get() = context.getString(
      R.string.version_details,
      titleText,
      installedVersion + (if (installedVersionCode != null) " ($installedVersionCode)" else ""),
      availableVersion ?: "UNKNOWN"
    )

  enum class VersionStatus {
    VERSION_OLDER_THAN_LATEST,
    VERSION_NEWER_THAN_LATEST,
    VERSION_IS_LATEST,
    NOT_INSTALLED,
    CONFIGURATION_ERROR
  }
}
