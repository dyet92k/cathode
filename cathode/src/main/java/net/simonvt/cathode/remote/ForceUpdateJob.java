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

package net.simonvt.cathode.remote;

import android.database.Cursor;
import net.simonvt.cathode.jobqueue.Job;
import net.simonvt.cathode.provider.DatabaseContract.MovieColumns;
import net.simonvt.cathode.provider.DatabaseContract.ShowColumns;
import net.simonvt.cathode.provider.ProviderSchematic.Movies;
import net.simonvt.cathode.provider.ProviderSchematic.Shows;
import net.simonvt.cathode.provider.ShowWrapper;
import net.simonvt.cathode.remote.sync.movies.SyncMovie;
import net.simonvt.cathode.remote.sync.shows.SyncShow;

public class ForceUpdateJob extends Job {

  @Override public String key() {
    return "ForceUpdateJob";
  }

  @Override public int getPriority() {
    return PRIORITY_ACTIONS;
  }

  @Override public void perform() {
    Cursor shows = getContentResolver().query(Shows.SHOWS, new String[] {
        ShowColumns.ID, ShowColumns.TRAKT_ID,
    }, null, null, null);

    while (shows.moveToNext()) {
      final long showId = shows.getLong(shows.getColumnIndex(ShowColumns.ID));
      final long traktId = shows.getLong(shows.getColumnIndex(ShowColumns.TRAKT_ID));

      final boolean syncFully = ShowWrapper.shouldSyncFully(getContentResolver(), showId);
      queue(new SyncShow(traktId, syncFully));
    }

    shows.close();

    Cursor movies = getContentResolver().query(Movies.MOVIES, new String[] {
        MovieColumns.TRAKT_ID,
    }, null, null, null);

    while (movies.moveToNext()) {
      final long traktId = movies.getLong(movies.getColumnIndex(MovieColumns.TRAKT_ID));
      queue(new SyncMovie(traktId));
    }

    movies.close();
  }
}
