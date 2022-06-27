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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import app.evergreen.R
import app.evergreen.services.log
import coil.load
import java.net.URLEncoder.encode

class QrCodeFragment : DialogFragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
    inflater.inflate(R.layout.fragment_qr_code, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    arguments?.let { args ->
      if (args.containsKey(EXTRA_TEXT)) {
        setQrCodeText(args.getString(EXTRA_TEXT) ?: "")
      }
    }
  }

  private fun setQrCodeText(text: String) {
    view?.findViewById<TextView>(R.id.qr_code_issue_text)?.text = text
    val qrCodeImageView = view?.findViewById<ImageView>(R.id.qr_code_image)
    val qrCodeUrl = "https://chart.googleapis.com/chart?cht=qr&chs=500x500&chl=${encode(text, "UTF-8")}"
    log(TAG, "qrCodeUrl: $qrCodeUrl")
    qrCodeImageView?.load(qrCodeUrl) {
      placeholder(R.drawable.dots_horizontal)
    }
  }

  companion object {
    const val TAG = "QrCodeFragment"

    private const val EXTRA_TEXT = "text"

    fun withText(text: String) = QrCodeFragment().apply {
      arguments = Bundle().apply {
        putString(EXTRA_TEXT, text)
      }
    }
  }
}
