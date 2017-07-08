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

package net.simonvt.cathode.remote.sync.shows;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.format.DateUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.simonvt.cathode.api.entity.Season;
import net.simonvt.cathode.api.entity.Show;
import net.simonvt.cathode.api.enumeration.Extended;
import net.simonvt.cathode.api.service.SeasonService;
import net.simonvt.cathode.api.service.ShowsService;
import net.simonvt.cathode.jobqueue.JobPriority;
import net.simonvt.cathode.jobscheduler.Jobs;
import net.simonvt.cathode.jobscheduler.SchedulerService;
import net.simonvt.cathode.provider.DatabaseContract.SeasonColumns;
import net.simonvt.cathode.provider.DatabaseContract.ShowColumns;
import net.simonvt.cathode.provider.ProviderSchematic.Seasons;
import net.simonvt.cathode.provider.ProviderSchematic.Shows;
import net.simonvt.cathode.provider.SeasonDatabaseHelper;
import net.simonvt.cathode.provider.ShowDatabaseHelper;
import net.simonvt.cathode.remote.ErrorHandlerJob;
import net.simonvt.schematic.Cursors;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class SyncPendingShows extends ErrorHandlerJob {

  public static final int ID = 103;

  @Inject transient ShowsService showsService;
  @Inject transient SeasonService seasonService;

  @Inject transient ShowDatabaseHelper showHelper;
  @Inject transient SeasonDatabaseHelper seasonHelper;

  @RequiresApi(api = Build.VERSION_CODES.N) public static void schedule(Context context) {
    JobInfo jobInfo = new JobInfo.Builder(ID,
        new ComponentName(context, SchedulerService.class)).setRequiredNetworkType(
        JobInfo.NETWORK_TYPE_ANY)
        .setRequiresCharging(true)
        .setBackoffCriteria(DateUtils.MINUTE_IN_MILLIS, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
        .setPersisted(true)
        .build();
    Jobs.schedule(context, jobInfo);
  }

  @Override public String key() {
    return "SyncPendingShows";
  }

  @Override public int getPriority() {
    return JobPriority.SHOWS;
  }

  @Override public boolean perform() {
    final String where = ShowColumns.NEEDS_SYNC
        + "=1 AND ("
        + ShowColumns.WATCHED_COUNT
        + ">0 OR "
        + ShowColumns.IN_COLLECTION_COUNT
        + ">0 OR "
        + ShowColumns.IN_WATCHLIST_COUNT
        + ">0 OR "
        + ShowColumns.IN_WATCHLIST
        + "=1)";
    Cursor shows = getContentResolver().query(Shows.SHOWS, new String[] {
        ShowColumns.ID, ShowColumns.TRAKT_ID, ShowColumns.WATCHED_COUNT,
        ShowColumns.IN_COLLECTION_COUNT, ShowColumns.IN_WATCHLIST_COUNT, ShowColumns.IN_WATCHLIST,
    }, where, null, null);

    try {
      while (shows.moveToNext() && !isStopped()) {
        final long showId = Cursors.getLong(shows, ShowColumns.ID);
        final long traktId = Cursors.getLong(shows, ShowColumns.TRAKT_ID);
        final int watchedCount = Cursors.getInt(shows, ShowColumns.WATCHED_COUNT);
        final int collectedCount = Cursors.getInt(shows, ShowColumns.IN_COLLECTION_COUNT);
        final int watchlistCount = Cursors.getInt(shows, ShowColumns.IN_WATCHLIST_COUNT);
        final boolean inWatchlist = Cursors.getBoolean(shows, ShowColumns.IN_WATCHLIST);

        if (watchedCount == 0 && collectedCount == 0 && watchlistCount == 0 && !inWatchlist) {
          continue;
        }

        Timber.d("Syncing pending show %d", traktId);

        Call<Show> showCall = showsService.getSummary(traktId, Extended.FULL);
        Response<Show> showResponse = showCall.execute();

        Call<List<Season>> seasonsCall = seasonService.getSummary(traktId, Extended.FULL);
        Response<List<Season>> seasonsResponse = seasonsCall.execute();

        if (showResponse.isSuccessful() && seasonsResponse.isSuccessful()) {
          Show show = showResponse.body();
          List<Season> seasons = seasonsResponse.body();

          List<Long> seasonIds = new ArrayList<>();
          Cursor currentSeasons =
              getContentResolver().query(Seasons.fromShow(showId), new String[] {
                  SeasonColumns.ID,
              }, null, null, null);
          while (currentSeasons.moveToNext()) {
            final long id = Cursors.getLong(currentSeasons, SeasonColumns.ID);
            seasonIds.add(id);
          }
          currentSeasons.close();

          for (Season season : seasons) {
            SeasonDatabaseHelper.IdResult result =
                seasonHelper.getIdOrCreate(showId, season.getNumber());
            seasonHelper.updateSeason(showId, season);
            seasonIds.remove(result.id);

            ContentValues values = new ContentValues();
            values.put(SeasonColumns.NEEDS_SYNC, true);
            getContentResolver().update(Seasons.withId(result.id), values, null, null);
          }

          for (Long seasonId : seasonIds) {
            getContentResolver().delete(Seasons.withId(seasonId), null, null);
          }

          showHelper.fullUpdate(show);
        } else {
          boolean showError = isError(showResponse);
          boolean seasonsError = isError(seasonsResponse);
          if (showError || seasonsError) {
            return false;
          }
        }
      }

      if (isStopped()) {
        return false;
      }

      if (Jobs.usesScheduler()) {
        SyncPendingSeasons.schedule(getContext());
      } else {
        queue(new SyncPendingSeasons());
      }

      return true;
    } catch (IOException e) {
      Timber.d(e);
      return false;
    } finally {
      if (shows != null) {
        shows.close();
      }
    }
  }
}
