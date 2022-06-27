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

package app.evergreen.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.ErrorSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import app.evergreen.R
import app.evergreen.extensions.color
import app.evergreen.extensions.drawable
import app.evergreen.services.AppServices.repo
import app.evergreen.ui.tools.ToolsObjectAdapter
import app.evergreen.ui.updates.UpdatesObjectAdapter

class EvergreenFragment : BrowseSupportFragment() {
  private val rowsAdapter: ArrayObjectAdapter = ArrayObjectAdapter(ListRowPresenter())

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    showTitle(true)
    adapter = rowsAdapter
    brandColor = requireContext().color(R.color.grey_900)
    badgeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.evergreen)
  }

  override fun onStart() {
    super.onStart()

    val toolsRowExists = rowsAdapter.unmodifiableList<ListRow>().any { it.adapter is ToolsObjectAdapter }
    if (!toolsRowExists) {
      rowsAdapter.add(ListRow(HeaderItem(requireContext().getString(R.string.tools)),
        ToolsObjectAdapter(requireContext()) { dialogFragment, tag ->
          dialogFragment.show(requireFragmentManager(), tag)
        }
      ))
    }

    repo.evergreenConfig.observe(this) { evergreenConfig ->
      val existingUpdatesRow =
        rowsAdapter.unmodifiableList<ListRow>().indexOfFirst { it.adapter is UpdatesObjectAdapter }
      if (existingUpdatesRow != -1) {
        rowsAdapter.removeItems(existingUpdatesRow, 1)
      }

      // Adding to a specific row index does not seem to work correctly.
      rowsAdapter.add(
        ListRow(HeaderItem(requireContext().getString(R.string.updates)),
          UpdatesObjectAdapter(evergreenConfig) { dialogFragment, tag ->
            dialogFragment.show(requireFragmentManager(), tag)
          })
      )
    }

    repo.errors.observe(this) { fetchError ->
      requireFragmentManager().beginTransaction()
        .replace(android.R.id.content, ErrorSupportFragment().apply {
          val context = this@EvergreenFragment.requireContext()
          imageDrawable = context.drawable(R.drawable.evergreen)
          message = fetchError.message
          buttonText = context.getString(R.string.qr_code)
          buttonClickListener = View.OnClickListener {
            QrCodeFragment.withText(
              fetchError.deviceUniqueId + "\n" + repo.getConfigUrl(fetchError.deviceUniqueId)
            ).show(this@EvergreenFragment.requireFragmentManager(), QrCodeFragment.TAG)
          }
        })
        .commit()
    }
  }
}

const val MAIN_IMAGE_SIZE_DP = 300
