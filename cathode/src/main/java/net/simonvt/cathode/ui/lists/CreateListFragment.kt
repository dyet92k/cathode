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
package net.simonvt.cathode.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import net.simonvt.cathode.R
import net.simonvt.cathode.api.enumeration.Privacy
import net.simonvt.cathode.api.enumeration.SortBy
import net.simonvt.cathode.api.enumeration.SortOrientation
import net.simonvt.cathode.databinding.DialogListCreateBinding
import net.simonvt.cathode.settings.TraktLinkSettings
import net.simonvt.cathode.sync.scheduler.ListsTaskScheduler
import javax.inject.Inject

class CreateListFragment @Inject constructor(
  private val listsTaskScheduler: ListsTaskScheduler
) : DialogFragment() {

  private var _binding: DialogListCreateBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    if (showsDialog) {
      setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    inState: Bundle?
  ): View? {
    _binding = DialogListCreateBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, inState: Bundle?) {
    super.onViewCreated(view, inState)

    binding.toolbarInclude.toolbar.setTitle(R.string.action_list_create)
    binding.toolbarInclude.toolbar.inflateMenu(R.menu.fragment_list_create)
    binding.toolbarInclude.toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.menu_create -> {
          val privacyValue = when (binding.privacy.selectedItemPosition) {
            1 -> Privacy.FRIENDS
            2 -> Privacy.PUBLIC
            else -> Privacy.PRIVATE
          }
          val sortByValue = when (binding.sortBy.selectedItemPosition) {
            2 -> SortBy.ADDED
            3 -> SortBy.TITLE
            4 -> SortBy.RELEASED
            5 -> SortBy.RUNTIME
            6 -> SortBy.POPULARITY
            7 -> SortBy.PERCENTAGE
            8 -> SortBy.VOTES
            9 -> SortBy.MY_RATING
            10 -> SortBy.RANDOM
            else -> SortBy.RANK
          }
          val sortOrientationValue = when (binding.sortOrientation.selectedItemPosition) {
            1 -> SortOrientation.DESC
            else -> SortOrientation.ASC
          }

          listsTaskScheduler.createList(
            binding.name.text.toString(),
            binding.description.text.toString(),
            privacyValue,
            binding.displayNumbers.isChecked,
            binding.allowComments.isChecked,
            sortByValue,
            sortOrientationValue
          )

          dismiss()
          return@OnMenuItemClickListener true
        }
      }

      false
    })

    if (TraktLinkSettings.isLinked(requireContext())) {
      val privacyAdapter = ArrayAdapter.createFromResource(
        requireContext(),
        R.array.list_privacy,
        android.R.layout.simple_spinner_item
      )
      privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
      binding.privacy.adapter = privacyAdapter
    } else {
      binding.privacy.visibility = View.GONE
      binding.allowComments.visibility = View.GONE
    }

    val sortByAdapter = ArrayAdapter.createFromResource(
      requireContext(),
      R.array.list_sort_by,
      android.R.layout.simple_spinner_item
    )
    sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    binding.sortBy.adapter = sortByAdapter

    val sortOrientationAdapter = ArrayAdapter.createFromResource(
      requireContext(),
      R.array.list_sort_orientation,
      android.R.layout.simple_spinner_item
    )
    sortOrientationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    binding.sortOrientation.adapter = sortOrientationAdapter
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }
}
