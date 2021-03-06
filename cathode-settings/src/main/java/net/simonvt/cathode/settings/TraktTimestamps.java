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
package net.simonvt.cathode.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import net.simonvt.cathode.api.entity.LastActivity;

public final class TraktTimestamps {

  private static final String SETTINGS_FILE = "trakt_timestamps";

  public static final String SHOW_RATING = "showRating";
  public static final String SHOW_WATCHLIST = "showWatchlist";
  public static final String SHOW_COMMENT = "showComment";
  public static final String SHOW_HIDE = "showHide";

  public static final String SEASON_RATING = "seasonRating";
  public static final String SEASON_COMMENT = "seasonComment";

  public static final String EPISODE_WATCHED = "episodeWatched";
  public static final String EPISODE_COLLECTION = "episodeCollection";
  public static final String EPISODE_RATING = "episodeRating";
  public static final String EPISODE_WATCHLIST = "episodeWatchlist";
  public static final String EPISODE_COMMENT = "episodeComment";

  public static final String MOVIE_WATCHED = "movieWatched";
  public static final String MOVIE_COLLECTION = "movieCollection";
  public static final String MOVIE_RATING = "movieRating";
  public static final String MOVIE_WATCHLIST = "movieWatchlist";
  public static final String MOVIE_COMMENT = "movieComment";
  public static final String MOVIE_HIDE = "movieHide";

  public static final String COMMENT_LIKED_AT = "commentLikedAt";

  public static final String LIST_UPDATED_AT = "listUpdatedAt";

  private TraktTimestamps() {
  }

  public static SharedPreferences getSettings(Context context) {
    return context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
  }

  public static boolean episodeWatchedNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(EPISODE_WATCHED, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean episodeCollectedNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(EPISODE_COLLECTION, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean episodeWatchlistNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(EPISODE_WATCHLIST, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean episodeRatingsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(EPISODE_RATING, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean episodeCommentsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(EPISODE_COMMENT, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean seasonRatingsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(SEASON_RATING, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean seasonCommentsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(SEASON_COMMENT, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean showWatchlistNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(SHOW_WATCHLIST, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean showRatingsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(SEASON_RATING, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean showCommentsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(SHOW_COMMENT, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean showHideNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(SHOW_HIDE, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean movieWatchedNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(MOVIE_WATCHED, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean movieCollectedNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(MOVIE_COLLECTION, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean movieWatchlistNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(MOVIE_WATCHLIST, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean movieRatingsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(MOVIE_RATING, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean movieCommentsNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(MOVIE_COMMENT, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean movieHideNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(MOVIE_HIDE, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean commentLikedNeedsUpdate(Context context, long lastLiked) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(COMMENT_LIKED_AT, -1);
    return lastActivity == -1 || lastLiked > lastActivity;
  }

  public static boolean listNeedsUpdate(Context context, long lastUpdated) {
    SharedPreferences settings = getSettings(context);

    long lastActivity = settings.getLong(LIST_UPDATED_AT, -1);
    return lastActivity == -1 || lastUpdated > lastActivity;
  }

  public static boolean shouldSyncComments(long lastSync) {
    return System.currentTimeMillis() > lastSync + 3 * DateUtils.HOUR_IN_MILLIS;
  }

  public static void update(Context context, LastActivity lastActivity) {
    SharedPreferences settings = getSettings(context);
    SharedPreferences.Editor editor = settings.edit();

    editor.putLong(SHOW_RATING, lastActivity.getShows().getRated_at().getTimeInMillis());
    editor.putLong(SHOW_WATCHLIST, lastActivity.getShows().getWatchlisted_at().getTimeInMillis());
    editor.putLong(SHOW_COMMENT, lastActivity.getShows().getCommented_at().getTimeInMillis());
    editor.putLong(SHOW_HIDE, lastActivity.getShows().getHidden_at().getTimeInMillis());

    editor.putLong(SEASON_COMMENT, lastActivity.getSeasons().getCommented_at().getTimeInMillis());
    editor.putLong(SEASON_RATING, lastActivity.getSeasons().getRated_at().getTimeInMillis());

    editor.putLong(EPISODE_WATCHED, lastActivity.getEpisodes().getWatched_at().getTimeInMillis());
    editor.putLong(EPISODE_COLLECTION,
        lastActivity.getEpisodes().getCollected_at().getTimeInMillis());
    editor.putLong(EPISODE_RATING, lastActivity.getEpisodes().getRated_at().getTimeInMillis());
    editor.putLong(EPISODE_WATCHLIST,
        lastActivity.getEpisodes().getWatchlisted_at().getTimeInMillis());
    editor.putLong(EPISODE_COMMENT, lastActivity.getEpisodes().getCommented_at().getTimeInMillis());

    editor.putLong(MOVIE_WATCHED, lastActivity.getMovies().getWatched_at().getTimeInMillis());
    editor.putLong(MOVIE_COLLECTION, lastActivity.getMovies().getCollected_at().getTimeInMillis());
    editor.putLong(MOVIE_RATING, lastActivity.getMovies().getRated_at().getTimeInMillis());
    editor.putLong(MOVIE_WATCHLIST, lastActivity.getMovies().getWatchlisted_at().getTimeInMillis());
    editor.putLong(MOVIE_COMMENT, lastActivity.getMovies().getCommented_at().getTimeInMillis());
    editor.putLong(MOVIE_HIDE, lastActivity.getMovies().getHidden_at().getTimeInMillis());

    editor.putLong(COMMENT_LIKED_AT, lastActivity.getComments().getLiked_at().getTimeInMillis());

    editor.putLong(LIST_UPDATED_AT, lastActivity.getLists().getUpdated_at().getTimeInMillis());

    editor.apply();
  }

  public static void clear(Context context) {
    getSettings(context).edit().clear().apply();
  }
}
