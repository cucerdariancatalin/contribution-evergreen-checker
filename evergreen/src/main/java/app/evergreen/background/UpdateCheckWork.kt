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

package app.evergreen.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.evergreen.services.AppServices.repo
import app.evergreen.services.log
import java.util.concurrent.TimeUnit

class UpdateCheckWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
  override fun doWork(): Result {
    repo.refreshFromServer()
    return Result.success()
  }
}

object JobManager {
  private const val TAG = "UpdateCheckWork"

  fun scheduleUpdateCheck(context: Context) {
    val constraints = Constraints.Builder()
      .setRequiresBatteryNotLow(true)
      .setRequiresCharging(false)
      .setRequiresStorageNotLow(false)
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      TAG,
      ExistingPeriodicWorkPolicy.REPLACE,
      PeriodicWorkRequest.Builder(UpdateCheckWork::class.java, 24, TimeUnit.HOURS)
        .addTag(TAG)
        .setConstraints(constraints)
        .build()
    ).state.observeForever { state: Operation.State? -> log(TAG, "scheduleUpdateCheck: $state") }
  }
}
