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

package net.simonvt.cathode.actions.movies

import android.content.ContentValues
import android.content.Context
import android.text.format.DateUtils
import net.simonvt.cathode.actions.ErrorHandlerAction
import net.simonvt.cathode.provider.DatabaseContract.MovieColumns
import net.simonvt.cathode.provider.ProviderSchematic.Movies
import javax.inject.Inject

class MarkSyncUserMovies @Inject constructor(
  private val context: Context
) : ErrorHandlerAction<Unit>() {

  override fun key(params: Unit): String = "MarkSyncUserMovies"

  override suspend fun invoke(params: Unit) {
    val syncBefore = System.currentTimeMillis() - SYNC_INTERVAL
    val values = ContentValues()
    values.put(MovieColumns.NEEDS_SYNC, true)
    context.contentResolver.update(
      Movies.MOVIES, values, "(" +
          MovieColumns.WATCHED + " OR " +
          MovieColumns.IN_COLLECTION + " OR " +
          MovieColumns.IN_WATCHLIST + ") AND " +
          MovieColumns.LAST_SYNC + "<?",
      arrayOf(syncBefore.toString())
    )
  }

  companion object {
    const val SYNC_INTERVAL = 30 * DateUtils.DAY_IN_MILLIS
  }
}
