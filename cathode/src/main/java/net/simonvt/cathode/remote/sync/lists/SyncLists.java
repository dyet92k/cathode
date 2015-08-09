/*
 * Copyright (C) 2015 Simon Vig Therkildsen
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

package net.simonvt.cathode.remote.sync.lists;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.simonvt.cathode.api.entity.CustomList;
import net.simonvt.cathode.api.service.UsersService;
import net.simonvt.cathode.jobqueue.Job;
import net.simonvt.cathode.provider.DatabaseContract.ListsColumns;
import net.simonvt.cathode.provider.ListWrapper;
import net.simonvt.cathode.provider.ProviderSchematic.Lists;

public class SyncLists extends Job {

  private static final String[] PROJECTION = new String[] {
      ListsColumns.TRAKT_ID,
  };

  @Inject transient UsersService usersService;

  @Override public String key() {
    return "SyncLists";
  }

  @Override public int getPriority() {
    return PRIORITY_USER_DATA;
  }

  @Override public void perform() {
    List<CustomList> lists = usersService.lists();

    List<Long> listIds = new ArrayList<>();
    Cursor listsCursor =
        getContentResolver().query(Lists.LISTS, PROJECTION, ListsColumns.TRAKT_ID + ">=0", null,
            null);
    while (listsCursor.moveToNext()) {
      listIds.add(listsCursor.getLong(listsCursor.getColumnIndex(ListsColumns.TRAKT_ID)));
    }
    listsCursor.close();

    for (CustomList list : lists) {
      final long traktId = list.getIds().getTrakt();
      ListWrapper.updateOrInsert(getContentResolver(), list);
      queue(new SyncList(traktId));
      listIds.remove(traktId);
    }

    for (Long id : listIds) {
      getContentResolver().delete(Lists.withId(id), null, null);
    }
  }
}