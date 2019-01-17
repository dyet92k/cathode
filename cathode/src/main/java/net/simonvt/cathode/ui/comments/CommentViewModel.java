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

package net.simonvt.cathode.ui.comments;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class CommentViewModel extends AndroidViewModel {

  private long commentId = -1L;

  private CommentAndRepliesLiveData commentAndReplies;

  public CommentViewModel(@NonNull Application application) {
    super(application);
  }

  public void setCommentId(long commentId) {
    if (this.commentId == -1L) {
      this.commentId = commentId;
      commentAndReplies = new CommentAndRepliesLiveData(getApplication(), commentId);
    }
  }

  public CommentAndRepliesLiveData getCommentAndReplies() {
    return commentAndReplies;
  }
}