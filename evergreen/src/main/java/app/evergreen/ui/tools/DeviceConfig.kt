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

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build.BRAND
import android.os.Build.DEVICE
import android.os.Build.MODEL
import android.os.Build.PRODUCT
import android.service.voice.VoiceInteractionService
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import app.evergreen.R
import app.evergreen.extensions.drawable
import app.evergreen.extensions.humanReadableByteCountSI
import app.evergreen.extensions.toTargetSize
import app.evergreen.services.log
import app.evergreen.ui.BigTextFragment
import app.evergreen.ui.DialogOpener
import app.evergreen.ui.MAIN_IMAGE_SIZE_DP

/**
 * Checks whether Katniss is correctly configured as the platform-default [VoiceInteractionService]
 * especially on Low RAM devices.
 */
class DeviceConfig(private val context: Context, private val dialogOpener: DialogOpener) : Tool {
  override val titleText: String
    get() = context.getString(R.string.device_config)

  override val mainImage: Drawable
    get() = context.drawable(R.drawable.wrench)!!
      .toBitmap(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
      .toTargetSize(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
      .toDrawable(context.resources)

  override fun doAction() {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
      ?: return
    val memInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memInfo)

    val isLowRamDevice = activityManager.isLowRamDevice

    val isKatnissActiveVoiceInteractionService = VoiceInteractionService.isActiveService(
      context,
      ComponentName(
        "com.google.android.katniss",
        "com.google.android.katniss.search.serviceapi.KatnissVoiceInteractionService"
      )
    )

    val isConfigOK = !isLowRamDevice || isKatnissActiveVoiceInteractionService

    val deviceInfo = "Low RAM flag: $isLowRamDevice / " +
        "Katniss is VoiceInteractionService: $isKatnissActiveVoiceInteractionService " +
        "→ ${if (isConfigOK) "OK" else "Not OK"}" +
        "\n" +
        "totalMem: ${memInfo.totalMem.humanReadableByteCountSI()} / " +
        "availMem: ${memInfo.availMem.humanReadableByteCountSI()}" +
        "\n" +
        "BRAND: $BRAND / DEVICE: $DEVICE / PRODUCT: $PRODUCT / MODEL: $MODEL"
    log(TAG, deviceInfo)

    dialogOpener.invoke(
      BigTextFragment.withText(context.getString(R.string.device_config), deviceInfo), BigTextFragment.TAG
    )
  }

  companion object {
    private const val TAG = "DeviceConfig"
  }
}
