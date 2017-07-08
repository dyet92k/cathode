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

package net.simonvt.cathode.jobqueue;

public interface JobPriority {

  int CONFIGURATION = 11;
  int ACTIONS = 10;
  int USER_DATA = 9;
  int IMAGES = 7;
  int SEASONS = 6;
  int SHOWS = 5;
  int MOVIES = 4;
  int SUGGESTIONS = 3;
  int EXTRAS = 2;
  int UPDATED = 1;
}
