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
package net.simonvt.cathode.ui.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.View;
import net.simonvt.cathode.CathodeApp;
import net.simonvt.cathode.R;
import net.simonvt.cathode.database.SimpleCursor;
import net.simonvt.cathode.database.SimpleCursorLoader;
import net.simonvt.cathode.provider.DatabaseContract.EpisodeColumns;
import net.simonvt.cathode.provider.DatabaseContract.SeasonColumns;
import net.simonvt.cathode.provider.DatabaseContract.ShowColumns;
import net.simonvt.cathode.provider.ProviderSchematic.Episodes;
import net.simonvt.cathode.provider.ProviderSchematic.Seasons;
import net.simonvt.cathode.provider.ProviderSchematic.Shows;
import net.simonvt.cathode.ui.LibraryType;
import net.simonvt.cathode.ui.Loaders;
import net.simonvt.cathode.ui.ShowsNavigationListener;
import net.simonvt.cathode.ui.adapter.SeasonAdapter;

public class SeasonFragment extends ToolbarGridFragment<SeasonAdapter.ViewHolder>
    implements SeasonAdapter.EpisodeClickListener {

  private static final String ARG_SHOW_ID = "net.simonvt.cathode.ui.fragment.SeasonFragment.showId";
  private static final String ARG_SEASONID =
      "net.simonvt.cathode.ui.fragment.SeasonFragment.seasonId";
  private static final String ARG_SHOW_TITLE =
      "net.simonvt.cathode.ui.fragment.SeasonFragment.showTitle";
  private static final String ARG_SEASON_NUMBER =
      "net.simonvt.cathode.ui.fragment.SeasonFragment.seasonNumber";
  private static final String ARG_TYPE = "net.simonvt.cathode.ui.fragment.SeasonFragment.type";

  private long showId;

  private long seasonId;

  private LibraryType type;

  private String title;

  private int seasonNumber = -1;

  private SeasonAdapter episodeAdapter;

  private ShowsNavigationListener navigationListener;

  private int columnCount;

  public static Bundle getArgs(long showId, long seasonId, String showTitle, int seasonNumber,
      LibraryType type) {
    Bundle args = new Bundle();
    args.putLong(ARG_SHOW_ID, showId);
    args.putLong(ARG_SEASONID, seasonId);
    args.putString(ARG_SHOW_TITLE, showTitle);
    args.putInt(ARG_SEASON_NUMBER, seasonNumber);
    args.putSerializable(ARG_TYPE, type);
    return args;
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      navigationListener = (ShowsNavigationListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement ShowsNavigationListener");
    }
  }

  @Override public void onCreate(Bundle inState) {
    super.onCreate(inState);
    CathodeApp.inject(getActivity(), this);

    Bundle args = getArguments();
    showId = args.getLong(ARG_SHOW_ID);
    seasonId = args.getLong(ARG_SEASONID);
    title = args.getString(ARG_SHOW_TITLE);
    seasonNumber = args.getInt(ARG_SEASON_NUMBER);
    type = (LibraryType) args.getSerializable(ARG_TYPE);

    setTitle(title);
    updateSubtitle();

    getLoaderManager().initLoader(Loaders.LOADER_SEASON, null, episodesLoader);

    if (title == null) {
      SimpleCursorLoader loader =
          new SimpleCursorLoader(getActivity(), Shows.withId(showId), new String[] {
              ShowColumns.TITLE,
          }, null, null, null);
      loader.registerListener(0, new Loader.OnLoadCompleteListener<SimpleCursor>() {
        @Override public void onLoadComplete(Loader<SimpleCursor> cursorLoader,
            SimpleCursor cursor) {
          cursor.moveToFirst();
          title = cursor.getString(cursor.getColumnIndex(ShowColumns.TITLE));
          setTitle(title);

          cursorLoader.stopLoading();
        }
      });
      loader.startLoading();
    }

    if (seasonNumber == -1) {
      new Thread(new Runnable() {
        @Override public void run() {
          Cursor c =
              getActivity().getContentResolver().query(Seasons.withId(seasonId), new String[] {
                  SeasonColumns.SEASON,
              }, null, null, null);

          if (c.moveToFirst()) {
            seasonNumber = c.getInt(c.getColumnIndex(SeasonColumns.SEASON));
            updateSubtitle();
          }
          c.close();
        }
      }).start();
    }

    columnCount = getResources().getInteger(R.integer.episodesColumns);
  }

  public String updateSubtitle() {
    if (seasonNumber == 0) {
      return getResources().getString(R.string.season_special);
    } else {
      return getResources().getString(R.string.season_x, seasonNumber);
    }
  }

  @Override protected int getColumnCount() {
    return columnCount;
  }

  @Override public boolean displaysMenuIcon() {
    return false;
  }

  @Override public void onViewCreated(View view, Bundle inState) {
    super.onViewCreated(view, inState);
    toolbar.setNavigationOnClickListener(navigationClickListener);
  }

  private View.OnClickListener navigationClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      navigationListener.onHomeClicked();
    }
  };

  @Override public void onEpisodeClick(View view, int position, long id) {
    navigationListener.onDisplayEpisode(id, title);
  }

  private void setCursor(Cursor cursor) {
    if (episodeAdapter == null) {
      episodeAdapter = new SeasonAdapter(getActivity(), this, cursor, type);
      setAdapter(episodeAdapter);
      return;
    }

    episodeAdapter.changeCursor(cursor);
  }

  private LoaderManager.LoaderCallbacks<SimpleCursor> episodesLoader =
      new LoaderManager.LoaderCallbacks<SimpleCursor>() {
        @Override public Loader<SimpleCursor> onCreateLoader(int id, Bundle args) {
          SimpleCursorLoader cl =
              new SimpleCursorLoader(getActivity(), Episodes.fromSeason(seasonId), null,
                  EpisodeColumns.NEEDS_SYNC + "=0", null, EpisodeColumns.EPISODE + " ASC");
          cl.setUpdateThrottle(2 * DateUtils.SECOND_IN_MILLIS);
          return cl;
        }

        @Override public void onLoadFinished(Loader<SimpleCursor> cursorLoader, SimpleCursor data) {
          setCursor(data);
        }

        @Override public void onLoaderReset(Loader<SimpleCursor> cursorLoader) {
          setCursor(null);
        }
      };
}
