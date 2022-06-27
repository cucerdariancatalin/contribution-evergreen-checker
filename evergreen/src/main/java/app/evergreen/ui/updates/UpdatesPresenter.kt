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

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.leanback.widget.BaseCardView.CARD_REGION_VISIBLE_ALWAYS
import androidx.leanback.widget.BaseCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.PresenterSelector
import app.evergreen.R
import app.evergreen.R.color
import app.evergreen.config.EvergreenConfig
import app.evergreen.config.Kind.APK
import app.evergreen.config.Kind.REMOTE_FIRMWARE
import app.evergreen.config.Kind.SYSTEM_BUILD
import app.evergreen.config.Updatable
import app.evergreen.extensions.color
import app.evergreen.extensions.drawable
import app.evergreen.extensions.safeStartActivity
import app.evergreen.extensions.toTargetSize
import app.evergreen.ui.DialogOpener
import app.evergreen.ui.MAIN_IMAGE_SIZE_DP
import app.evergreen.ui.QrCodeFragment
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.CONFIGURATION_ERROR
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.NOT_INSTALLED
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.VERSION_IS_LATEST
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.VERSION_NEWER_THAN_LATEST
import app.evergreen.ui.updates.AbstractUpdatableViewModel.VersionStatus.VERSION_OLDER_THAN_LATEST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdatesObjectAdapter(
  private val evergreenConfig: EvergreenConfig,
  private val dialogOpener: DialogOpener
) : ObjectAdapter() {
  init {
    presenterSelector = object : PresenterSelector() {
      override fun getPresenter(item: Any?) = UpdatesPresenter(dialogOpener)
    }
  }

  override fun size() = evergreenConfig.updatables.size

  override fun get(position: Int) = evergreenConfig.updatables[position]
}

private class UpdatesPresenter(private val dialogOpener: DialogOpener) : Presenter() {
  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    return ViewHolder(ImageCardView(parent.context).apply {
      setMainImageDimensions(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
      isFocusable = true
      isFocusableInTouchMode = true
      cardType = CARD_TYPE_INFO_UNDER_WITH_EXTRA
      infoVisibility = CARD_REGION_VISIBLE_ALWAYS
      setBackgroundColor(context.color(color.grey_700))
    }, dialogOpener)
  }

  override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
    (viewHolder as ViewHolder).bind(item as Updatable)
  }

  override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
    // Nothing
  }

  class ViewHolder(view: View, private val dialogOpener: DialogOpener) : Presenter.ViewHolder(view) {
    private val imageCardView: ImageCardView = view as ImageCardView
    private val context = view.context

    fun bind(updatable: Updatable) {
      val updatableViewModel: AbstractUpdatableViewModel = when (updatable.kind) {
        APK -> ApkViewModel(context, updatable)
        SYSTEM_BUILD -> SystemBuildViewModel(context, updatable)
        REMOTE_FIRMWARE -> RemoteFirmwareViewModel(context, updatable)
      }

      imageCardView.apply {
        titleText = updatableViewModel.titleText
        contentText = updatableViewModel.contentText
        setInfoAreaBackgroundColor(context.color(updatableViewModel.backgroundColor))
        badgeImage = context.drawable(
          when (updatableViewModel.versionStatus) {
            VERSION_OLDER_THAN_LATEST -> R.drawable.cellphone_arrow_down
            VERSION_NEWER_THAN_LATEST -> R.drawable.bomb
            VERSION_IS_LATEST -> R.drawable.check_bold
            NOT_INSTALLED -> R.drawable.skull_crossbones
            CONFIGURATION_ERROR -> R.drawable.help_circle
          }
        )

        setOnClickListener {
          AlertDialog.Builder(context).apply {
            setTitle(updatableViewModel.dialogTitle)
            setMessage(updatableViewModel.dialogMessage)
            setPositiveButton(R.string.check_for_update) { dialog, _ ->
              updatableViewModel.onUpdate()
              dialog.dismiss()
            }
            if (updatable.kind == APK) {
              setNegativeButton(R.string.app_details) { dialog, _ ->
                context.safeStartActivity(
                  Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:${updatable.id}"))
                )
                dialog.dismiss()
              }
            } else {
              setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            }
            setNeutralButton(R.string.qr_code) { dialog, _ ->
              dialogOpener.invoke(QrCodeFragment.withText(updatableViewModel.dialogMessage), QrCodeFragment.TAG)
              dialog.dismiss()
            }
          }.show()
        }
      }

      CoroutineScope(Dispatchers.Main).launch {
        imageCardView.mainImage = withContext(Dispatchers.IO) {
          updatableViewModel.getIcon()
            .toBitmap(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
            .toTargetSize(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
            .toDrawable(context.resources)
        }
      }
    }
  }
}
