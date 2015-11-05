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

package net.simonvt.cathode.remote.sync.comments;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.simonvt.cathode.api.entity.Comment;
import net.simonvt.cathode.api.entity.CommentItem;
import net.simonvt.cathode.api.entity.Episode;
import net.simonvt.cathode.api.entity.Movie;
import net.simonvt.cathode.api.entity.Profile;
import net.simonvt.cathode.api.entity.Season;
import net.simonvt.cathode.api.enumeration.CommentType;
import net.simonvt.cathode.api.enumeration.ItemTypes;
import net.simonvt.cathode.api.service.UsersService;
import net.simonvt.cathode.jobqueue.JobFailedException;
import net.simonvt.cathode.provider.CommentsHelper;
import net.simonvt.cathode.provider.DatabaseContract;
import net.simonvt.cathode.provider.DatabaseContract.CommentColumns;
import net.simonvt.cathode.provider.EpisodeDatabaseHelper;
import net.simonvt.cathode.provider.MovieWrapper;
import net.simonvt.cathode.provider.ProviderSchematic.Comments;
import net.simonvt.cathode.provider.SeasonDatabaseHelper;
import net.simonvt.cathode.provider.ShowDatabaseHelper;
import net.simonvt.cathode.provider.UserDatabaseHelper;
import net.simonvt.cathode.provider.generated.CathodeProvider;
import net.simonvt.cathode.remote.Flags;
import net.simonvt.cathode.remote.PagedCallJob;
import net.simonvt.cathode.remote.sync.SyncUserProfile;
import net.simonvt.cathode.remote.sync.movies.SyncMovie;
import net.simonvt.cathode.remote.sync.shows.SyncSeason;
import net.simonvt.cathode.remote.sync.shows.SyncShow;
import retrofit.Call;
import timber.log.Timber;

public class SyncUserComments extends PagedCallJob<CommentItem> {

  private static final int LIMIT = 100;

  @Inject transient UsersService usersService;

  @Inject transient ShowDatabaseHelper showHelper;
  @Inject transient SeasonDatabaseHelper seasonHelper;
  @Inject transient EpisodeDatabaseHelper episodeHelper;
  @Inject transient UserDatabaseHelper userHelper;

  private ItemTypes itemTypes;

  public SyncUserComments(ItemTypes itemTypes) {
    super(Flags.REQUIRES_AUTH);
    this.itemTypes = itemTypes;
  }

  @Override public String key() {
    return "SyncUserComments&types=" + itemTypes;
  }

  @Override public int getPriority() {
    return PRIORITY_USER_DATA;
  }

  @Override public Call<List<CommentItem>> getCall(int page) {
    return usersService.getUserComments(CommentType.ALL, itemTypes, page, LIMIT);
  }

  @Override public void handleResponse(List<CommentItem> comments) {
    List<Long> existingComments = new ArrayList<>();
    Cursor c = getContentResolver().query(Comments.COMMENTS, new String[] {
        CommentColumns.ID,
    }, CommentColumns.IS_USER_COMMENT + "=1", null, null);
    while (c.moveToNext()) {
      final long id = c.getLong(c.getColumnIndex(CommentColumns.ID));
      Timber.d("Existing id:" + id);
      existingComments.add(id);
    }
    c.close();

    ArrayList<ContentProviderOperation> ops = new ArrayList<>();

    long profileId = -1L;

    for (CommentItem commentItem : comments) {
      Comment comment = commentItem.getComment();
      ContentValues values = CommentsHelper.getValues(comment);
      values.put(CommentColumns.IS_USER_COMMENT, true);

      if (profileId == -1L) {
        Profile profile = commentItem.getComment().getUser();
        UserDatabaseHelper.IdResult result = userHelper.updateOrCreate(profile);
        profileId = result.id;
        queue(new SyncUserProfile());
      }
      values.put(CommentColumns.USER_ID, profileId);

      switch (commentItem.getType()) {
        case SHOW: {
          final long traktId = commentItem.getShow().getIds().getTrakt();
          ShowDatabaseHelper.IdResult result = showHelper.getIdOrCreate(traktId);
          final long showId = result.showId;
          if (result.didCreate) {
            queue(new SyncShow(traktId));
          }

          values.put(CommentColumns.ITEM_TYPE, DatabaseContract.ItemType.SHOW);
          values.put(CommentColumns.ITEM_ID, showId);
          break;
        }

        case SEASON: {
          final long traktId = commentItem.getShow().getIds().getTrakt();
          ShowDatabaseHelper.IdResult showResult = showHelper.getIdOrCreate(traktId);
          final long showId = showResult.showId;
          if (showResult.didCreate) {
            queue(new SyncShow(traktId));
          }

          Season season = commentItem.getSeason();
          final int seasonNumber = season.getNumber();
          SeasonDatabaseHelper.IdResult seasonResult =
              seasonHelper.getIdOrCreate(showId, seasonNumber);
          final long seasonId = seasonResult.id;
          if (seasonResult.didCreate) {
            if (!showResult.didCreate) {
              queue(new SyncShow(traktId));
            }
          }

          values.put(CommentColumns.ITEM_TYPE, DatabaseContract.ItemType.SEASON);
          values.put(CommentColumns.ITEM_ID, seasonId);
          break;
        }

        case EPISODE:
          final long traktId = commentItem.getShow().getIds().getTrakt();
          ShowDatabaseHelper.IdResult showResult = showHelper.getIdOrCreate(traktId);
          final long showId = showResult.showId;
          if (showResult.didCreate) {
            queue(new SyncShow(traktId));
          }

          Episode episode = commentItem.getEpisode();
          final int seasonNumber = episode.getSeason();
          final int episodeNumber = episode.getNumber();

          SeasonDatabaseHelper.IdResult seasonResult =
              seasonHelper.getIdOrCreate(showId, seasonNumber);
          final long seasonId = seasonResult.id;
          if (seasonResult.didCreate) {
            if (!showResult.didCreate) {
              queue(new SyncShow(traktId));
            }
          }

          EpisodeDatabaseHelper.IdResult episodeResult =
              episodeHelper.getIdOrCreate(showId, seasonId, episodeNumber);
          final long episodeId = episodeResult.id;
          if (episodeResult.didCreate) {
            if (!showResult.didCreate && !seasonResult.didCreate) {
              queue(new SyncSeason(traktId, seasonNumber));
            }
          }

          values.put(CommentColumns.ITEM_TYPE, DatabaseContract.ItemType.EPISODE);
          values.put(CommentColumns.ITEM_ID, episodeId);
          break;

        case MOVIE: {
          Movie movie = commentItem.getMovie();
          long movieId = MovieWrapper.getMovieId(getContentResolver(), movie);
          if (movieId == -1L) {
            movieId = MovieWrapper.updateOrInsertMovie(getContentResolver(), movie);
            queue(new SyncMovie(movie.getIds().getTrakt()));
          }

          values.put(CommentColumns.ITEM_TYPE, DatabaseContract.ItemType.MOVIE);
          values.put(CommentColumns.ITEM_ID, movieId);
          break;
        }

        case LIST:
          continue;
      }

      final long id = comment.getId();
      if (existingComments.contains(id)) {
        existingComments.remove(id);
        ContentProviderOperation.Builder op =
            ContentProviderOperation.newUpdate(Comments.withId(id)).withValues(values);
        ops.add(op.build());
      } else {
        ContentProviderOperation.Builder op =
            ContentProviderOperation.newInsert(Comments.COMMENTS).withValues(values);
        ops.add(op.build());
      }
    }

    for (Long id : existingComments) {
      ContentProviderOperation.Builder op = ContentProviderOperation.newDelete(Comments.withId(id));
      ops.add(op.build());
    }

    try {
      getContentResolver().applyBatch(CathodeProvider.AUTHORITY, ops);
    } catch (RemoteException e) {
      Timber.e(e, "Updating comments failed");
      throw new JobFailedException(e);
    } catch (OperationApplicationException e) {
      Timber.e(e, "Updating comments failed");
      throw new JobFailedException(e);
    }
  }
}