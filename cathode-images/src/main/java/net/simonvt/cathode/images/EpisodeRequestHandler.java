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

package net.simonvt.cathode.images;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.squareup.picasso.Request;
import com.uwetrottmann.tmdb2.entities.Image;
import com.uwetrottmann.tmdb2.entities.Images;
import com.uwetrottmann.tmdb2.services.TvEpisodesService;
import java.io.IOException;
import javax.inject.Inject;
import net.simonvt.cathode.common.tmdb.TmdbRateLimiter;
import net.simonvt.cathode.common.util.Closeables;
import net.simonvt.cathode.provider.DatabaseContract.EpisodeColumns;
import net.simonvt.cathode.provider.EpisodeDatabaseHelper;
import net.simonvt.cathode.provider.ProviderSchematic.Episodes;
import net.simonvt.cathode.provider.ShowDatabaseHelper;
import net.simonvt.schematic.Cursors;
import retrofit2.Response;

public class EpisodeRequestHandler extends ItemRequestHandler {

  @Inject TvEpisodesService tvEpisodesService;

  @Inject ShowDatabaseHelper showHelper;
  @Inject EpisodeDatabaseHelper episodeHelper;

  public EpisodeRequestHandler(Context context, ImageDownloader downloader) {
    super(context, downloader);
  }

  @Override public boolean canHandleRequest(Request data) {
    return ImageUri.ITEM_EPISODE.equals(data.uri.getScheme());
  }

  @Override protected int getTmdbId(long id) {
    return episodeHelper.getTmdbId(id);
  }

  @Override protected String getCachedPath(ImageType imageType, long id) {
    Cursor c = null;

    try {
      c = context.getContentResolver().query(Episodes.withId(id), new String[] {
          EpisodeColumns.IMAGES_LAST_UPDATE, EpisodeColumns.SCREENSHOT,
      }, null, null, null);
      c.moveToFirst();

      long lastUpdate = Cursors.getLong(c, EpisodeColumns.IMAGES_LAST_UPDATE);
      final boolean needsUpdate =
          lastUpdate + DateUtils.WEEK_IN_MILLIS < System.currentTimeMillis();

      if (imageType == ImageType.STILL) {
        String path = Cursors.getString(c, EpisodeColumns.SCREENSHOT);
        if (!needsUpdate && !TextUtils.isEmpty(path)) {
          return path;
        }
      } else {
        throw new IllegalArgumentException("Unsupported image type: " + imageType.toString());
      }
    } finally {
      Closeables.closeQuietly(c);
    }

    return null;
  }

  protected void clearCachedPaths(long id) {
    ContentValues values = new ContentValues();
    values.put(EpisodeColumns.IMAGES_LAST_UPDATE, 0L);
    values.putNull(EpisodeColumns.SCREENSHOT);
    context.getContentResolver().update(Episodes.withId(id), values, null, null);
  }

  @Override protected String queryPath(ImageType imageType, long id, int tmdbId)
      throws IOException {
    String path = null;

    Cursor c = null;
    int showTmdbId = -1;
    int season = -1;
    int episode = -1;
    try {
      c = context.getContentResolver().query(Episodes.withId(id), new String[] {
          EpisodeColumns.SHOW_ID, EpisodeColumns.SEASON, EpisodeColumns.EPISODE
      }, null, null, null);
      c.moveToFirst();

      long showId = Cursors.getLong(c, EpisodeColumns.SHOW_ID);
      showTmdbId = showHelper.getTmdbId(showId);
      season = Cursors.getInt(c, EpisodeColumns.SEASON);
      episode = Cursors.getInt(c, EpisodeColumns.EPISODE);
    } finally {
      Closeables.closeQuietly(c);
    }

    TmdbRateLimiter.acquire();
    Response<Images> response = tvEpisodesService.images(showTmdbId, season, episode).execute();

    if (response.isSuccessful()) {
      Images images = response.body();
      path = retainImages(context, id, images);
    }

    return path;
  }

  public static String retainImages(Context context, long id, Images images) {
    ContentValues values = new ContentValues();
    values.put(EpisodeColumns.IMAGES_LAST_UPDATE, System.currentTimeMillis());
    Image screenshot = selectBest(images.stills);

    String path = null;

    if (screenshot != null) {
      final String screenshotPath = ImageUri.create(ImageType.BACKDROP, screenshot.file_path);
      values.put(EpisodeColumns.SCREENSHOT, screenshotPath);
      path = screenshotPath;
    } else {
      values.putNull(EpisodeColumns.SCREENSHOT);
    }

    context.getContentResolver().update(Episodes.withId(id), values, null, null);

    return path;
  }
}
