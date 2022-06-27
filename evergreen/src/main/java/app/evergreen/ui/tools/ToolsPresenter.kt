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
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.leanback.widget.BaseCardView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.PresenterSelector
import app.evergreen.R
import app.evergreen.extensions.color
import app.evergreen.ui.DialogOpener
import app.evergreen.ui.MAIN_IMAGE_SIZE_DP

class ToolsObjectAdapter(private val context: Context, private val dialogOpener: DialogOpener) : ObjectAdapter() {
  init {
    presenterSelector = object : PresenterSelector() {
      override fun getPresenter(item: Any?) = ToolsPresenter()
    }
  }

  override fun size() = 5

  override fun get(position: Int): Tool = when (position) {
    0 -> DeviceConfig(context, dialogOpener)
    1 -> AppVersions(context, dialogOpener)
    2 -> RequestForDevice(context, dialogOpener)
    3 -> AdbConfig(context, dialogOpener)
    4 -> LaunchPlayStore(context)
    else -> throw UnsupportedOperationException()
  }
}

private class ToolsPresenter : Presenter() {
  override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(ImageCardView(parent.context).apply {
    setMainImageDimensions(MAIN_IMAGE_SIZE_DP, MAIN_IMAGE_SIZE_DP)
    isFocusable = true
    isFocusableInTouchMode = true
    cardType = BaseCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA
    infoVisibility = BaseCardView.CARD_REGION_VISIBLE_ALWAYS
    setBackgroundColor(context.color(R.color.grey_700))
  })

  override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
    val imageCardView: ImageCardView = viewHolder.view as ImageCardView
    val tool = item as Tool
    imageCardView.apply {
      titleText = tool.titleText
      mainImage = tool.mainImage
      setOnClickListener { tool.doAction() }
    }
  }

  override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    // Nothing
  }
}

interface Tool {
  val titleText: String
  val mainImage: Drawable
  fun doAction()
}
