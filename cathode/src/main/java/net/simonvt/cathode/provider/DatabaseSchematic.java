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
package net.simonvt.cathode.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import net.simonvt.cathode.provider.DatabaseContract.EpisodeColumns;
import net.simonvt.cathode.provider.DatabaseContract.MovieCastColumns;
import net.simonvt.cathode.provider.DatabaseContract.MovieColumns;
import net.simonvt.cathode.provider.DatabaseContract.MovieCrewColumns;
import net.simonvt.cathode.provider.DatabaseContract.MovieGenreColumns;
import net.simonvt.cathode.provider.DatabaseContract.MovieSearchSuggestionsColumns;
import net.simonvt.cathode.provider.DatabaseContract.PersonColumns;
import net.simonvt.cathode.provider.DatabaseContract.SeasonColumns;
import net.simonvt.cathode.provider.DatabaseContract.ShowColumns;
import net.simonvt.cathode.provider.DatabaseContract.ShowGenreColumns;
import net.simonvt.cathode.provider.DatabaseContract.ShowSearchSuggestionsColumns;
import net.simonvt.cathode.provider.generated.CathodeDatabase;
import net.simonvt.cathode.util.DateUtils;
import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.ExecOnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

@Database(className = "CathodeDatabase", fileName = "cathode.db",
    version = DatabaseSchematic.DATABASE_VERSION)
public final class DatabaseSchematic {

  private DatabaseSchematic() {
  }

  static final String DATABASE_NAME = "DB1";

  static final int DATABASE_VERSION = 12;

  public interface Joins {
    String SHOWS_UNWATCHED = "LEFT OUTER JOIN episodes ON episodes._id=(SELECT episodes._id FROM"
        + " episodes WHERE episodes.watched=0 AND episodes.showId=shows._id AND episodes.season<>0"
        + " AND episodes.episodeFirstAired>"
        + DateUtils.YEAR_IN_MILLIS
        // TODO: Find better solution
        + " ORDER BY episodes.season ASC, episodes.episode ASC LIMIT 1)";

    String SHOWS_UPCOMING = "LEFT OUTER JOIN "
        + TABLE_EPISODES
        + " ON "
        + TABLE_EPISODES
        + "."
        + EpisodeColumns.ID
        + "="
        + "("
        + "SELECT _id "
        + "FROM episodes "
        + "JOIN ("
        + "SELECT season, episode "
        + "FROM episodes "
        + "WHERE watched=1 AND showId=shows._id "
        + "ORDER BY season DESC, episode DESC LIMIT 1"
        + ") AS ep2 "
        + "WHERE episodes.watched=0 AND episodes.showId=shows._id AND (episodes.season>ep2.season "
        + "OR (episodes.season=ep2.season AND episodes.episode>ep2.episode)) "
        + "ORDER BY episodes.season ASC, episodes.episode ASC LIMIT 1"
        + ")";

    String SHOWS_UNCOLLECTED = "LEFT OUTER JOIN episodes ON episodes._id=(SELECT episodes._id FROM"
        + " episodes WHERE episodes.inCollection=0 AND episodes.showId=shows._id"
        + " AND episodes.season<>0"
        + " AND episodes.episodeFirstAired>"
        + DateUtils.YEAR_IN_MILLIS
        // TODO: Find better solution
        + " ORDER BY episodes.season ASC, episodes.episode ASC LIMIT 1)";

    String SHOWS_WITH_WATCHING =
        "LEFT OUTER JOIN episodes ON episodes._id=(SELECT episodes._id FROM"
            + " episodes WHERE (episodes.watching=1 OR episodes.checkedIn=1)"
            + " AND episodes.showId=shows._id"
            + " AND episodes.episodeFirstAired>"
            + DateUtils.YEAR_IN_MILLIS
            + " ORDER BY episodes.season ASC, episodes.episode ASC LIMIT 1)";

    String EPISODES_WITH_SHOW_TITLE = "JOIN "
        + Tables.SHOWS
        + " AS "
        + Tables.SHOWS
        + " ON "
        + Tables.SHOWS
        + "."
        + ShowColumns.ID
        + "="
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SHOW_ID;
  }

  public interface Tables {

    String SHOWS = "shows";
    String SHOW_GENRES = "showGenres";
    String SEASONS = "seasons";
    String EPISODES = "episodes";
    String SHOW_CHARACTERS = "showCharacters";

    String MOVIES = "movies";
    String MOVIE_GENRES = "movieGenres";

    String MOVIE_CAST = "movieCast";
    String MOVIE_CREW = "movieCrew";

    String PEOPLE = "people";

    String SHOW_SEARCH_SUGGESTIONS = "showSearchSuggestions";
    String MOVIE_SEARCH_SUGGESTIONS = "movieSearchSuggestions";
  }

  interface References {

    String SHOW_ID = "REFERENCES " + Tables.SHOWS + "(" + ShowColumns.ID + ")";
    String SEASON_ID = "REFERENCES " + Tables.SEASONS + "(" + SeasonColumns.ID + ")";
    String MOVIE_ID = "REFERENCES " + Tables.MOVIES + "(" + MovieColumns.ID + ")";
  }

  interface Trigger {
    String EPISODE_UPDATE_AIRED_NAME = "episodeUpdateAired";
    String EPISODE_UPDATE_WATCHED_NAME = "episodeUpdateWatched";
    String EPISODE_UPDATE_COLLECTED_NAME = "episodeUpdateCollected";
    String EPISODE_INSERT_NAME = "episodeInsert";

    String SEASONS_UPDATE_WATCHED = "UPDATE "
        + Tables.SEASONS
        + " SET "
        + SeasonColumns.WATCHED_COUNT
        + "=(SELECT COUNT(*) FROM "
        + Tables.EPISODES
        + " WHERE "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON_ID
        + "=NEW."
        + EpisodeColumns.SEASON_ID
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.WATCHED
        + "=1 AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON
        + ">0)"
        + " WHERE "
        + Tables.SEASONS
        + "."
        + SeasonColumns.ID
        + "=NEW."
        + EpisodeColumns.SEASON_ID
        + ";";

    String SEASONS_UPDATE_COLLECTED = "UPDATE "
        + Tables.SEASONS
        + " SET "
        + SeasonColumns.IN_COLLECTION_COUNT
        + "=(SELECT COUNT(*) FROM "
        + Tables.EPISODES
        + " WHERE "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON_ID
        + "=NEW."
        + EpisodeColumns.SEASON_ID
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.IN_COLLECTION
        + "=1 AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON
        + ">0)"
        + " WHERE "
        + Tables.SEASONS
        + "."
        + SeasonColumns.ID
        + "=NEW."
        + EpisodeColumns.SEASON_ID
        + ";";

    String SEASONS_UPDATE_AIRDATE = "UPDATE "
        + Tables.SEASONS
        + " SET "
        + SeasonColumns.AIRDATE_COUNT
        + "=(SELECT COUNT(*) FROM "
        + Tables.EPISODES
        + " WHERE "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON_ID
        + "=NEW."
        + EpisodeColumns.SEASON_ID
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.FIRST_AIRED
        + ">"
        + DateUtils.YEAR_IN_MILLIS
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON
        + ">0)"
        + " WHERE "
        + Tables.SEASONS
        + "."
        + SeasonColumns.ID
        + "=NEW."
        + EpisodeColumns.SEASON_ID
        + ";";

    String SHOWS_UPDATE_WATCHED = "UPDATE "
        + Tables.SHOWS
        + " SET "
        + ShowColumns.WATCHED_COUNT
        + "=(SELECT COUNT(*) FROM "
        + Tables.EPISODES
        + " WHERE "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SHOW_ID
        + "=NEW."
        + EpisodeColumns.SHOW_ID
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.WATCHED
        + "=1 AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON
        + ">0)"
        + " WHERE "
        + Tables.SHOWS
        + "."
        + ShowColumns.ID
        + "=NEW."
        + EpisodeColumns.SHOW_ID
        + ";";

    String SHOWS_UPDATE_COLLECTED = "UPDATE "
        + Tables.SHOWS
        + " SET "
        + ShowColumns.IN_COLLECTION_COUNT
        + "=(SELECT COUNT(*) FROM "
        + Tables.EPISODES
        + " WHERE "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SHOW_ID
        + "=NEW."
        + EpisodeColumns.SHOW_ID
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.IN_COLLECTION
        + "=1 AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON
        + ">0)"
        + " WHERE "
        + Tables.SHOWS
        + "."
        + ShowColumns.ID
        + "=NEW."
        + EpisodeColumns.SHOW_ID
        + ";";

    String SHOWS_UPDATE_AIRDATE = "UPDATE "
        + Tables.SHOWS
        + " SET "
        + ShowColumns.AIRDATE_COUNT
        + "=(SELECT COUNT(*) FROM "
        + Tables.EPISODES
        + " WHERE "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SHOW_ID
        + "=NEW."
        + EpisodeColumns.SHOW_ID
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.FIRST_AIRED
        + ">"
        + DateUtils.YEAR_IN_MILLIS
        + " AND "
        + Tables.EPISODES
        + "."
        + EpisodeColumns.SEASON
        + ">0)"
        + " WHERE "
        + Tables.SHOWS
        + "."
        + ShowColumns.ID
        + "=NEW."
        + EpisodeColumns.SHOW_ID
        + ";";
  }

  @Table(ShowColumns.class) public static final String TABLE_SHOWS = Tables.SHOWS;

  @Table(ShowGenreColumns.class) public static final String TABLE_SHOW_GENRES = Tables.SHOW_GENRES;

  @Table(SeasonColumns.class) public static final String TABLE_SEASONS = Tables.SEASONS;

  @Table(EpisodeColumns.class) public static final String TABLE_EPISODES = Tables.EPISODES;

  @Table(DatabaseContract.ShowCharacterColumns.class) public static final String
      TABLE_SHOW_CHARACTERS = Tables.SHOW_CHARACTERS;

  @Table(MovieColumns.class) public static final String TABLE_MOVIES = Tables.MOVIES;

  @Table(MovieGenreColumns.class) public static final String TABLE_MOVIE_GENRES =
      Tables.MOVIE_GENRES;

  @Table(MovieCastColumns.class) public static final String TABLE_MOVIE_CAST = Tables.MOVIE_CAST;

  @Table(MovieCrewColumns.class) public static final String TABLE_MOVIE_CREW = Tables.MOVIE_CREW;

  @Table(PersonColumns.class) public static final String TABLE_PEOPLE = Tables.PEOPLE;

  @Table(ShowSearchSuggestionsColumns.class) public static final String
      TABLE_SHOW_SEARCH_SUGGESTIONS = Tables.SHOW_SEARCH_SUGGESTIONS;

  @Table(MovieSearchSuggestionsColumns.class) public static final String
      TABLE_MOVIE_SEARCH_SUGGESTIONS = Tables.MOVIE_SEARCH_SUGGESTIONS;

  @ExecOnCreate
  public static final String TRIGGER_EPISODE_UPDATE_AIRED = "CREATE TRIGGER "
      + Trigger.EPISODE_UPDATE_AIRED_NAME
      + " AFTER UPDATE OF "
      + EpisodeColumns.FIRST_AIRED
      + " ON "
      + TABLE_EPISODES
      + " BEGIN "
      + Trigger.SEASONS_UPDATE_AIRDATE
      + Trigger.SHOWS_UPDATE_AIRDATE
      + " END;";

  @ExecOnCreate
  public static final String TRIGGER_EPISODE_UPDATE_WATCHED = "CREATE TRIGGER "
      + Trigger.EPISODE_UPDATE_WATCHED_NAME
      + " AFTER UPDATE OF "
      + EpisodeColumns.WATCHED
      + " ON "
      + TABLE_EPISODES
      + " BEGIN "
      + Trigger.SEASONS_UPDATE_WATCHED
      + Trigger.SHOWS_UPDATE_WATCHED
      + " END;";

  @ExecOnCreate
  public static final String TRIGGER_EPISODE_UPDATE_COLLECTED = "CREATE TRIGGER "
      + Trigger.EPISODE_UPDATE_COLLECTED_NAME
      + " AFTER UPDATE OF "
      + EpisodeColumns.IN_COLLECTION
      + " ON "
      + TABLE_EPISODES
      + " BEGIN "
      + Trigger.SEASONS_UPDATE_COLLECTED
      + Trigger.SHOWS_UPDATE_COLLECTED
      + " END;";

  @ExecOnCreate
  public static final String TRIGGER_EPISODE_INSERT = "CREATE TRIGGER "
      + Trigger.EPISODE_INSERT_NAME
      + " AFTER INSERT ON "
      + TABLE_EPISODES
      + " BEGIN "
      + Trigger.SEASONS_UPDATE_WATCHED
      + Trigger.SEASONS_UPDATE_AIRDATE
      + Trigger.SEASONS_UPDATE_COLLECTED
      + Trigger.SHOWS_UPDATE_WATCHED
      + Trigger.SHOWS_UPDATE_AIRDATE
      + Trigger.SHOWS_UPDATE_COLLECTED
      + " END;";

  @OnUpgrade public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 12) {

    }
  }

  private static void createTriggers(SQLiteDatabase db) {
    db.execSQL(TRIGGER_EPISODE_INSERT);
    db.execSQL(TRIGGER_EPISODE_UPDATE_AIRED);
    db.execSQL(TRIGGER_EPISODE_UPDATE_WATCHED);
    db.execSQL(TRIGGER_EPISODE_UPDATE_COLLECTED);
  }

  private static void dropTriggers(SQLiteDatabase db) {
    db.execSQL("DROP TRIGGER " + Trigger.EPISODE_UPDATE_AIRED_NAME);
    db.execSQL("DROP TRIGGER " + Trigger.EPISODE_UPDATE_WATCHED_NAME);
    db.execSQL("DROP TRIGGER " + Trigger.EPISODE_UPDATE_COLLECTED_NAME);
    db.execSQL("DROP TRIGGER " + Trigger.EPISODE_INSERT_NAME);
  }

  public static void clearUserData(Context context) {
    SQLiteOpenHelper oh = CathodeDatabase.getInstance(context);
    SQLiteDatabase db = oh.getWritableDatabase();
    db.beginTransaction();
    dropTriggers(db);

    ContentValues cv;

    cv = new ContentValues();
    cv.put(ShowColumns.WATCHED_COUNT, 0);
    cv.put(ShowColumns.IN_COLLECTION_COUNT, 0);
    cv.put(ShowColumns.IN_WATCHLIST_COUNT, 0);
    cv.put(ShowColumns.IN_WATCHLIST, false);
    cv.put(ShowColumns.HIDDEN, false);
    db.update(Tables.SHOWS, cv, null, null);

    cv = new ContentValues();
    cv.put(SeasonColumns.WATCHED_COUNT, 0);
    cv.put(SeasonColumns.IN_COLLECTION_COUNT, 0);
    cv.put(SeasonColumns.IN_WATCHLIST_COUNT, 0);
    db.update(Tables.SEASONS, cv, null, null);

    cv = new ContentValues();
    cv.put(EpisodeColumns.WATCHED, 0);
    cv.put(EpisodeColumns.PLAYS, 0);
    cv.put(EpisodeColumns.IN_WATCHLIST, 0);
    cv.put(EpisodeColumns.IN_COLLECTION, 0);
    db.update(Tables.EPISODES, cv, null, null);

    cv = new ContentValues();
    cv.put(MovieColumns.WATCHED, 0);
    cv.put(MovieColumns.IN_COLLECTION, 0);
    cv.put(MovieColumns.IN_WATCHLIST, 0);
    db.update(Tables.MOVIES, cv, null, null);

    createTriggers(db);

    db.setTransactionSuccessful();
    db.endTransaction();
  }
}