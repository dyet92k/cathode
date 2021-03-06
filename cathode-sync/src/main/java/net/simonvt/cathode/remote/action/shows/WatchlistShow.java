/*
 * Copyright (C) 2013 Simon Vig Therkildsen
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
import net.simonvt.cathode.remote.CallJob;
import net.simonvt.cathode.remote.sync.SyncUserActivity;
import retrofit2.Call;

public class WatchlistShow extends CallJob<SyncResponse> {

  @Inject transient SyncService syncService;

  private long traktId;
  private boolean inWatchlist;
  private String listedAt;

  public WatchlistShow(long traktId, boolean inWatchlist, String listedAt) {

    this.traktId = traktId;
    this.inWatchlist = inWatchlist;
    this.listedAt = listedAt;
  }

  @Override public String key() {
    return "WatchlistShow"
        + "&traktId="
        + traktId
        + "&inWatchlist="
        + inWatchlist
        + "&listedAt="
        + listedAt;
  }

  @Override public Call<SyncResponse> getCall() {
    if (inWatchlist) {
      return syncService.watchlist(
          new SyncItems.Builder().show(traktId, null, null, listedAt).build());
    } else {
      return syncService.unwatchlist(
          new SyncItems.Builder().show(traktId, null, null, null).build());
    }
  }

  @Override public boolean handleResponse(SyncResponse response) {
    queue(new SyncUserActivity());
    return true;
  }
}
