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
package net.simonvt.cathode.ui.suggestions.movies;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import net.simonvt.cathode.R;
import net.simonvt.cathode.common.ui.FragmentsUtils;
import net.simonvt.cathode.common.ui.fragment.BaseFragment;
import net.simonvt.cathode.common.util.ViewPagerUtils;
import net.simonvt.cathode.settings.TraktLinkSettings;

public class MovieSuggestionsPagerAdapter extends FragmentPagerAdapter {

  private Context context;
  private FragmentManager fragmentManager;

  final boolean traktLinked;

  private MovieRecommendationsFragment recommendationsFragment;
  private TrendingMoviesFragment trendingFragment;
  private AnticipatedMoviesFragment anticipatedFragment;

  public MovieSuggestionsPagerAdapter(Context context, FragmentManager fm) {
    super(fm);
    this.context = context;
    this.fragmentManager = fm;

    traktLinked = TraktLinkSettings.isLinked(context);

    recommendationsFragment =
        (MovieRecommendationsFragment) fm.findFragmentByTag(makeFragmentName(0));
    if (recommendationsFragment == null) {
      recommendationsFragment =
          FragmentsUtils.instantiate(fragmentManager, MovieRecommendationsFragment.class);
    }

    trendingFragment = (TrendingMoviesFragment) fm.findFragmentByTag(makeFragmentName(1));
    if (trendingFragment == null) {
      trendingFragment = FragmentsUtils.instantiate(fragmentManager, TrendingMoviesFragment.class);
    }

    anticipatedFragment = (AnticipatedMoviesFragment) fm.findFragmentByTag(makeFragmentName(2));
    if (anticipatedFragment == null) {
      anticipatedFragment =
          FragmentsUtils.instantiate(fragmentManager, AnticipatedMoviesFragment.class);
    }
  }

  private static String makeFragmentName(long id) {
    return ViewPagerUtils.makeFragmentName(R.id.pager, id);
  }

  @Override public BaseFragment getItem(int position) {
    if (traktLinked) {
      if (position == 0) {
        return recommendationsFragment;
      } else if (position == 1) {
        return trendingFragment;
      } else {
        return anticipatedFragment;
      }
    } else {
      if (position == 0) {
        return trendingFragment;
      } else {
        return anticipatedFragment;
      }
    }
  }

  @Override public int getCount() {
    return traktLinked ? 3 : 2;
  }

  @Override public CharSequence getPageTitle(int position) {
    if (traktLinked) {
      if (position == 0) {
        return context.getString(R.string.title_recommended);
      } else if (position == 1) {
        return context.getString(R.string.title_trending);
      } else {
        return context.getString(R.string.title_anticipated);
      }
    } else {
      if (position == 0) {
        return context.getString(R.string.title_trending);
      } else {
        return context.getString(R.string.title_anticipated);
      }
    }
  }
}
