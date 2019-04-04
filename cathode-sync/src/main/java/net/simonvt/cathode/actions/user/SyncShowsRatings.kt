/*
 * Copyright (C) 2014 Simon Vig Therkildsen
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

package net.simonvt.cathode.actions.user

import android.content.ContentProviderOperation
import android.content.Context
import net.simonvt.cathode.actions.CallAction
import net.simonvt.cathode.actions.user.SyncShowsRatings.Params
import net.simonvt.cathode.api.entity.RatingItem
import net.simonvt.cathode.api.service.SyncService
import net.simonvt.cathode.common.database.forEach
import net.simonvt.cathode.common.database.getLong
import net.simonvt.cathode.provider.DatabaseContract.ShowColumns
import net.simonvt.cathode.provider.ProviderSchematic.Shows
import net.simonvt.cathode.provider.batch
import net.simonvt.cathode.provider.helper.ShowDatabaseHelper
import net.simonvt.cathode.provider.query
import net.simonvt.cathode.settings.TraktTimestamps
import retrofit2.Call
import javax.inject.Inject

class SyncShowsRatings @Inject constructor(
  private val context: Context,
  private val showHelper: ShowDatabaseHelper,
  private val syncService: SyncService
) : CallAction<Params, List<RatingItem>>() {

  override fun key(params: Params): String = "SyncShowsRatings"

  override fun getCall(params: Params): Call<List<RatingItem>> = syncService.getShowRatings()

  override suspend fun handleResponse(params: Params, response: List<RatingItem>) {
    val ops = arrayListOf<ContentProviderOperation>()
    val showIds = mutableListOf<Long>()
    val shows = context.contentResolver.query(
      Shows.SHOWS,
      arrayOf(ShowColumns.ID),
      ShowColumns.RATED_AT + ">0"
    )
    shows.forEach { cursor -> showIds.add(cursor.getLong(ShowColumns.ID)) }
    shows.close()

    for (rating in response) {
      val traktId = rating.show!!.ids.trakt!!
      val showResult = showHelper.getIdOrCreate(traktId)
      val showId = showResult.showId
      showIds.remove(showId)

      val op = ContentProviderOperation.newUpdate(Shows.withId(showId))
        .withValue(ShowColumns.USER_RATING, rating.rating)
        .withValue(ShowColumns.RATED_AT, rating.rated_at.timeInMillis)
        .build()
      ops.add(op)
    }

    for (showId in showIds) {
      val op = ContentProviderOperation.newUpdate(Shows.withId(showId))
        .withValue(ShowColumns.USER_RATING, 0)
        .withValue(ShowColumns.RATED_AT, 0)
        .build()
      ops.add(op)
    }

    context.contentResolver.batch(ops)

    if (params.userActivityTime > 0L) {
      TraktTimestamps.getSettings(context)
        .edit()
        .putLong(TraktTimestamps.SHOW_RATING, params.userActivityTime)
        .apply()
    }
  }

  data class Params(val userActivityTime: Long = 0L)
}
