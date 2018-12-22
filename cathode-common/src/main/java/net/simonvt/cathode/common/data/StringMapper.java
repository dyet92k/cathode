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

package net.simonvt.cathode.common.data;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import net.simonvt.cathode.common.database.Cursors;

public class StringMapper implements MappedCursorLiveData.CursorMapper<List<String>> {

  private final String column;

  public StringMapper(String column) {
    this.column = column;
  }

  @Override public List<String> map(Cursor cursor) {
    List<String> strings = new ArrayList<>();
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      strings.add(Cursors.getString(cursor, column));
    }
    return strings;
  }
}
