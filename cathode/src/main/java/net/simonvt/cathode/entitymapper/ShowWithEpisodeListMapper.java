/*
 * Copyright (C) 2018 Simon Vig Therkildsen
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

package net.simonvt.cathode.entitymapper;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import net.simonvt.cathode.common.data.MappedCursorLiveData;
import net.simonvt.cathode.common.entity.ShowWithEpisode;

public class ShowWithEpisodeListMapper
    implements MappedCursorLiveData.CursorMapper<List<ShowWithEpisode>> {

  @Override public List<ShowWithEpisode> map(Cursor cursor) {
    List<ShowWithEpisode> shows = new ArrayList<>();

    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      shows.add(ShowWithEpisodeMapper.mapShowAndEpisode(cursor));
    }

    return shows;
  }
}
