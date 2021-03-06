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

package net.simonvt.cathode.actions.user

import net.simonvt.cathode.actions.CallAction
import net.simonvt.cathode.api.entity.Profile
import net.simonvt.cathode.api.enumeration.Extended
import net.simonvt.cathode.api.service.UsersService
import net.simonvt.cathode.provider.helper.UserDatabaseHelper
import retrofit2.Call
import javax.inject.Inject

class SyncUserProfile @Inject constructor(
  private val userHelper: UserDatabaseHelper,
  private val usersService: UsersService
) : CallAction<Unit, Profile>() {

  override fun key(params: Unit): String = "SyncUserProfile"

  override fun getCall(params: Unit): Call<Profile> = usersService.getProfile(Extended.FULL_IMAGES)

  override suspend fun handleResponse(params: Unit, response: Profile) {
    userHelper.updateOrCreate(response)
  }
}
