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
package net.simonvt.cathode.ui.lists;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import javax.inject.Inject;
import net.simonvt.cathode.R;
import net.simonvt.cathode.sync.scheduler.ListsTaskScheduler;
import net.simonvt.cathode.ui.NavigationListener;

public class DeleteListDialog extends DialogFragment {

  private static final String ARG_LIST_ID = "net.simonvt.cathode.ui.lists.DeleteDialog.listId";

  private ListsTaskScheduler listScheduler;

  private NavigationListener navigationListener;

  public static Bundle getArgs(long listId) {
    Bundle args = new Bundle();
    args.putLong(ARG_LIST_ID, listId);
    return args;
  }

  @Inject public DeleteListDialog(ListsTaskScheduler listScheduler) {
    this.listScheduler = listScheduler;
  }

  @Override public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    navigationListener = (NavigationListener) requireActivity();
  }

  @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle inState) {
    final long listId = getArguments().getLong(ARG_LIST_ID);

    return new AlertDialog.Builder(requireContext()).setTitle(R.string.list_delete_title)
        .setMessage(R.string.list_delete_message)
        .setPositiveButton(R.string.list_dialog_delete, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            listScheduler.deleteList(listId);
            navigationListener.onListDeleted(listId);
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .create();
  }
}
