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
package net.simonvt.cathode.common.event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import net.simonvt.cathode.common.util.MainHandler;

public final class ItemsUpdatedEvent {

  public interface OnItemsUpdatedListener {

    void onItemsUpdated();
  }

  private static final List<WeakReference<OnItemsUpdatedListener>> LISTENERS = new ArrayList<>();

  public static void registerListener(OnItemsUpdatedListener listener) {
    synchronized (LISTENERS) {
      LISTENERS.add(new WeakReference<>(listener));
    }
  }

  public static void unregisterListener(OnItemsUpdatedListener listener) {
    synchronized (LISTENERS) {
      for (int i = LISTENERS.size() - 1; i >= 0; i--) {
        WeakReference<OnItemsUpdatedListener> ref = LISTENERS.get(i);
        OnItemsUpdatedListener l = ref.get();
        if (l == null || l == listener) {
          LISTENERS.remove(ref);
        }
      }
    }
  }

  public static void post() {
    MainHandler.post(new Runnable() {
      @Override public void run() {
        synchronized (LISTENERS) {
          for (int i = LISTENERS.size() - 1; i >= 0; i--) {
            WeakReference<OnItemsUpdatedListener> ref = LISTENERS.get(i);
            OnItemsUpdatedListener l = ref.get();
            if (l == null) {
              LISTENERS.remove(ref);
              continue;
            }

            l.onItemsUpdated();
          }
        }
      }
    });
  }

  private ItemsUpdatedEvent() {
  }
}
