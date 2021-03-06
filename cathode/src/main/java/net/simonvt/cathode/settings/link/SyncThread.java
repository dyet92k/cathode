/*
 * Copyright (C) 2016 Simon Vig Therkildsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simonvt.cathode.settings.link;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.work.WorkManager;
import java.util.List;
import net.simonvt.cathode.jobqueue.Job;
import net.simonvt.cathode.jobqueue.JobManager;
import net.simonvt.cathode.settings.Settings;
import net.simonvt.cathode.settings.TraktLinkSettings;
import net.simonvt.cathode.settings.TraktTimestamps;
import net.simonvt.cathode.work.WorkManagerUtils;
import net.simonvt.cathode.work.user.PeriodicSyncWorker;
import net.simonvt.cathode.work.user.SyncUserSettingsWorker;

public class SyncThread extends Thread {

  private Context context;
  private WorkManager workManager;
  private JobManager jobManager;
  private List<Job> jobs;

  public SyncThread(Context context, WorkManager workManager, JobManager jobManager,
      List<Job> jobs) {
    this.context = context;
    this.workManager = workManager;
    this.jobManager = jobManager;
    this.jobs = jobs;
  }

  @SuppressLint("ApplySharedPref") @Override public void run() {
    jobManager.clear();
    TraktTimestamps.clear(context);

    Settings.get(context)
        .edit()
        .putBoolean(TraktLinkSettings.TRAKT_LINKED, true)
        .putBoolean(TraktLinkSettings.TRAKT_LINK_PROMPTED, true)
        .putBoolean(TraktLinkSettings.TRAKT_AUTH_FAILED, false)
        .commit();

    for (Job job : jobs) {
      jobManager.addJobNow(job);
    }

    WorkManagerUtils.enqueueNow(workManager, SyncUserSettingsWorker.class);
    WorkManagerUtils.enqueueNow(workManager, PeriodicSyncWorker.class);
  }
}
