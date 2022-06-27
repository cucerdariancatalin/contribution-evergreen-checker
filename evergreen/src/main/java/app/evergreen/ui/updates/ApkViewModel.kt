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
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.pm.PackageInfoCompat
import app.evergreen.R
import app.evergreen.config.Updatable
import app.evergreen.extensions.drawable
import app.evergreen.services.AppServices.opener

class ApkViewModel(context: Context, updatable: Updatable) : AbstractUpdatableViewModel(context, updatable) {
  override val installedVersion: String?
    get() = if (updatable.id == null) {
      null
    } else {
      try {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(updatable.id!!, 0).versionName
      } catch (e: PackageManager.NameNotFoundException) {
        null // App not installed, so it does not have a version name yet.
      }
    }

  override val installedVersionCode: Long?
    get() = if (updatable.id == null) {
      null
    } else {
      try {
        PackageInfoCompat.getLongVersionCode(context.packageManager.getPackageInfo(updatable.id!!, 0))
      } catch (e: PackageManager.NameNotFoundException) {
        null // App not installed, so it does not have a version name yet.
      }
    }

  override val titleText: String
    get() = try {
      val packageManager = context.packageManager
      packageManager.getApplicationLabel(
        context.packageManager.getPackageInfo(updatable.id!!, 0).applicationInfo
      ).toString()
    } catch (e: PackageManager.NameNotFoundException) {
      updatable.id ?: context.getString(R.string.unknown)
    }

  override suspend fun getIcon(): Drawable = try {
    context.packageManager.getApplicationIcon(updatable.id!!)
  } catch (e: PackageManager.NameNotFoundException) {
    context.drawable(R.drawable.apps)!!
  }

  override fun onUpdate() = opener.openPlayStore(updatable.id!!)
}
