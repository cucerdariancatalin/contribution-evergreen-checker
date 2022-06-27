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

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.net.Uri
import app.evergreen.extensions.safeStartActivity

class Opener(private val context: Context) {
  fun openPlayStore(packageId: String): Boolean =
    context.safeStartActivity(
      Intent(ACTION_VIEW, Uri.parse("market://details?id=$packageId"))
        .addFlags(FLAG_ACTIVITY_NO_HISTORY or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_MULTIPLE_TASK)
    )
}
