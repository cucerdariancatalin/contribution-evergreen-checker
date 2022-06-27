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

import android.graphics.Bitmap
import android.graphics.Canvas

fun Bitmap.toTargetSize(targetWidth: Int, targetHeight: Int): Bitmap {
  val targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, config)
  val canvas = Canvas(targetBitmap)

  val boundingBoxWidth: Int = (targetWidth * 0.6).toInt()
  val boundingBoxHeight: Int = (targetHeight * 0.6).toInt()

  val left: Float = (targetWidth * 0.2).toFloat()
  val top: Float = (targetHeight * 0.2).toFloat()

  val scaledSourceBitmap = Bitmap.createScaledBitmap(this, boundingBoxWidth, boundingBoxHeight, true)
  canvas.drawBitmap(scaledSourceBitmap, left, top, null)
  return targetBitmap
}
