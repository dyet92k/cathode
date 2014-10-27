/*
 * Copyright (C) 2014 Simon Vig Therkildsen
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
import net.simonvt.cathode.api.service.SyncService;
import net.simonvt.cathode.api.util.TimeUtils;
import net.simonvt.cathode.provider.SeasonWrapper;
import net.simonvt.cathode.remote.TraktTask;

public class SeasonCollectionTask extends TraktTask {

  @Inject transient SyncService syncService;

  private long traktId;

  private int season;

  private boolean inCollection;

  private String collectedAt;

  public SeasonCollectionTask(long traktId, int season, boolean inCollection, String collectedAt) {
    this.traktId = traktId;
    this.season = season;
    this.inCollection = inCollection;
    this.collectedAt = collectedAt;
  }

  @Override protected void doTask() {
    if (inCollection) {
      SyncItems items = new SyncItems();
      items.show(traktId).season(season).collectedAt(collectedAt);
      syncService.collect(items);
    } else {
      SyncItems items = new SyncItems();
      items.show(traktId).season(season);
      syncService.uncollect(items);
    }

    SeasonWrapper.setIsInCollection(getContentResolver(), traktId, season, inCollection,
        TimeUtils.getMillis(collectedAt));
    postOnSuccess();
  }
}