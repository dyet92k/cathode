/*
 * Copyright (C) 2017 Simon Vig Therkildsen
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
package net.simonvt.cathode.remote.action.shows;

import javax.inject.Inject;
import net.simonvt.cathode.api.body.SyncItems;
import net.simonvt.cathode.api.entity.SyncResponse;
import net.simonvt.cathode.api.service.SyncService;
import net.simonvt.cathode.api.util.TimeUtils;
import net.simonvt.cathode.provider.ShowDatabaseHelper;
import net.simonvt.cathode.remote.CallJob;
import net.simonvt.cathode.remote.Flags;
import retrofit2.Call;

public class AddShowToHistory extends CallJob<SyncResponse> {

  @Inject transient SyncService syncService;

  @Inject transient ShowDatabaseHelper showHelper;

  private long traktId;

  private String watchedAt;

  public AddShowToHistory(long traktId, String watchedAt) {
    super(Flags.REQUIRES_AUTH);
    this.traktId = traktId;
    this.watchedAt = watchedAt;
  }

  @Override public String key() {
    return "AddShowToHistory" + "&traktId=" + traktId + "&watchedAt=" + watchedAt;
  }

  @Override public int getPriority() {
    return PRIORITY_ACTIONS;
  }

  @Override public boolean allowDuplicates() {
    return true;
  }

  @Override public Call<SyncResponse> getCall() {
    SyncItems items = new SyncItems();
    items.show(traktId).watchedAt(watchedAt);
    return syncService.watched(items);
  }

  @Override public boolean handleResponse(SyncResponse response) {
    final long showId = showHelper.getId(traktId);

    if (SyncItems.TIME_RELEASED.equals(watchedAt)) {
      showHelper.addToHistory(showId, ShowDatabaseHelper.WATCHED_RELEASE);
    } else {
      showHelper.addToHistory(showId, TimeUtils.getMillis(watchedAt));
    }

    return true;
  }
}