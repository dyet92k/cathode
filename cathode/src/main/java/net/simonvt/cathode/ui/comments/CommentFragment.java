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
package net.simonvt.cathode.ui.comments;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.AndroidSupportInjection;
import java.util.List;
import javax.inject.Inject;
import net.simonvt.cathode.R;
import net.simonvt.cathode.api.enumeration.ItemType;
import net.simonvt.cathode.common.entity.Comment;
import net.simonvt.cathode.common.ui.fragment.ToolbarGridFragment;
import net.simonvt.cathode.common.util.guava.Preconditions;
import net.simonvt.cathode.settings.TraktLinkSettings;
import net.simonvt.cathode.sync.scheduler.CommentsTaskScheduler;

public class CommentFragment extends ToolbarGridFragment<CommentsAdapter.ViewHolder> {

  public static final String TAG = "net.simonvt.cathode.ui.comments.CommentFragment";

  private static final String ARG_COMMENT_ID =
      "net.simonvt.cathode.ui.comments.CommentFragment.commentId";

  private static final String DIALOG_COMMENT_ADD =
      "net.simonvt.cathode.ui.comments.CommentFragment.addCommentDialog";
  private static final String DIALOG_COMMENT_UPDATE =
      "net.simonvt.cathode.ui.comments.CommentFragment.updateCommentDialog";

  private static final String STATE_ADAPTER =
      "net.simonvt.cathode.ui.comments.CommentFragment.adapterState";

  @Inject CommentsTaskScheduler commentsScheduler;

  private long commentId;

  private int columnCount;

  private CommentViewModel viewModel;

  private CommentsAdapter adapter;

  private Bundle adapterState;

  public static Bundle getArgs(long commentId) {
    Preconditions.checkArgument(commentId >= 0, "commentId must be >= 0");

    Bundle args = new Bundle();
    args.putLong(ARG_COMMENT_ID, commentId);
    return args;
  }

  @Override public void onCreate(Bundle inState) {
    super.onCreate(inState);
    AndroidSupportInjection.inject(this);

    Bundle args = getArguments();
    commentId = args.getLong(ARG_COMMENT_ID);

    //columnCount = getResources().getInteger(R.integer.listColumns);
    columnCount = 1;

    if (inState != null) {
      adapterState = inState.getBundle(STATE_ADAPTER);
    }

    setTitle(R.string.title_comments);

    viewModel = ViewModelProviders.of(this).get(CommentViewModel.class);
    viewModel.setCommentId(commentId);
    viewModel.getCommentAndReplies().observe(this, new Observer<List<Comment>>() {
      @Override public void onChanged(List<Comment> comments) {
        setComments(comments);
      }
    });
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBundle(STATE_ADAPTER, adapter.saveState());
  }

  @Override protected int getColumnCount() {
    return columnCount;
  }

  @Override public void createMenu(Toolbar toolbar) {
    super.createMenu(toolbar);
    if (TraktLinkSettings.isLinked(requireContext())) {
      toolbar.inflateMenu(R.menu.fragment_comment);
    }
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_reply:
        AddCommentDialog.newInstance(ItemType.COMMENT, commentId)
            .show(getFragmentManager(), DIALOG_COMMENT_ADD);
        return true;

      default:
        return super.onMenuItemClick(item);
    }
  }

  private CommentsAdapter.CommentCallbacks commentClickListener =
      new CommentsAdapter.CommentCallbacks() {
        @Override public void onCommentClick(long commentId, String comment, boolean spoiler,
            boolean isUserComment) {
          if (isUserComment) {
            UpdateCommentDialog.newInstance(commentId, comment, spoiler)
                .show(getFragmentManager(), DIALOG_COMMENT_UPDATE);
          }
        }

        @Override public void onLikeComment(long commentId) {
          commentsScheduler.like(commentId);
        }

        @Override public void onUnlikeComment(long commentId) {
          commentsScheduler.unlike(commentId);
        }
      };

  private void setComments(List<Comment> comments) {
    if (adapter == null) {
      adapter = new CommentsAdapter(requireContext(), true, commentClickListener);
      if (adapterState != null) {
        adapter.restoreState(adapterState);
        adapterState = null;
      }
      setAdapter(adapter);
    }

    adapter.setList(comments);
  }
}
