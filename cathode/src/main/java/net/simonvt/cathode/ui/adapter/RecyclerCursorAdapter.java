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

package net.simonvt.cathode.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerCursorAdapter<T extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<T> {

  private Context context;

  private Cursor cursor;

  private int idIndex = -1;

  private List<Long> itemIds;

  protected RecyclerCursorAdapter(Context context) {
    this(context, null);
  }

  protected RecyclerCursorAdapter(Context context, Cursor cursor) {
    this.context = context;
    changeCursor(cursor);
    setHasStableIds(true);
  }

  protected Context getContext() {
    return context;
  }

  public Cursor getCursor(int position) {
    if (cursor != null) {
      cursor.moveToPosition(position);
      return cursor;
    }

    return null;
  }

  public void changeCursor(Cursor cursor) {
    if (cursor == this.cursor) {
      return;
    }

    if (this.cursor != null) {
      this.cursor.close();
    }

    this.cursor = cursor;

    if (cursor != null) {
      idIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
    } else {
      idIndex = -1;
    }

    notifyChanged();
  }

  public void notifyChanged() {
    List<Long> oldItemIds = itemIds;
    final int itemCount = getItemCount();
    List<Long> newItemIds = new ArrayList<Long>();
    for (int i = 0; i < itemCount; i++) {
      newItemIds.add(getItemId(i));
    }

    if (oldItemIds == null) {
      notifyItemRangeInserted(0, itemCount);
    } else {
      final int oldItemCount = oldItemIds.size();

      for (int i = 0; i < itemCount; i++) {
        final long id = newItemIds.get(i);
        final int oldPosition = oldItemIds.indexOf(id);

        if (oldPosition < 0) {
          notifyItemInserted(i);
          oldItemIds.add(i, -1L);
        } else {
          if (i > oldPosition) {
            throw new RuntimeException(
                "Something is wrong, old position should always be >= current position");
          }

          final int removeCount = oldPosition - i;
          if (removeCount > 0) {
            notifyItemRangeRemoved(i, removeCount);
            for (int j = i + removeCount - 1; j >= i; j--) {
              oldItemIds.remove(j);
            }
          }
        }
      }

      if (oldItemCount > itemCount) {
        final int removeCount = oldItemCount - itemCount;
        notifyItemRangeRemoved(itemCount, removeCount);
      }
    }

    itemIds = newItemIds;
  }

  @Override public long getItemId(int position) {
    long id = getCursor(position).getLong(idIndex);
    return id;
  }

  @Override public final void onBindViewHolder(T holder, int position) {
    onBindViewHolder(holder, getCursor(position), position);
  }

  protected abstract void onBindViewHolder(T holder, Cursor cursor, int position);

  @Override public int getItemCount() {
    return cursor != null ? cursor.getCount() : 0;
  }
}