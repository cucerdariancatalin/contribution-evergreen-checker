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

package app.evergreen.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import app.evergreen.BuildConfig.DEBUG
import app.evergreen.R
import java.lang.Thread.currentThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Context.toast(message: String) {
  if (DEBUG) {
    Log.e("Toast", message)
  }
  if (currentThread() === Looper.getMainLooper().thread) {
    // Toasts can only be shown from the main thread.
    Toast.makeText(applicationContext, message, LENGTH_LONG).show()
  } else {
    CoroutineScope(Dispatchers.Main).launch {
      Toast.makeText(applicationContext, message, LENGTH_LONG).show()
    }
  }
}

fun Context.toast(@StringRes message: Int) = toast(getString(message))

fun Context.safeStartActivity(intent: Intent): Boolean = try {
  startActivity(intent)
  true

} catch (e: ActivityNotFoundException) {
  // If there was no matching app to handle this intent, then try the browser_fallback_url.
  val browserFallbackUrl = intent.getStringExtra("browser_fallback_url")
  if (browserFallbackUrl != null) {
    safeStartActivity(Intent.parseUri(browserFallbackUrl, 0 /* flags */))
  } else {
    // [Intent.createChooser] will show a bottom-sheet with a “No app can handle this action” message.
    startActivity(Intent.createChooser(intent, null))
    false
  }

} catch (e: SecurityException) {
  toast(e.localizedMessage ?: e.message ?: getString(R.string.unknown))
  false

} catch (e: RuntimeException) {
  // If the first attempt failed because this Context is not an Activity, then try again with the explicit flag to
  // create a new task, but only if that flag doesn’t already exist, otherwise this will cause a stack overflow.
  if (intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != Intent.FLAG_ACTIVITY_NEW_TASK) {
    safeStartActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  } else {
    false
  }
}

fun Context.color(@ColorRes color: Int): Int = ContextCompat.getColor(this, color)

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? =
  ResourcesCompat.getDrawable(resources, drawableRes, null)
