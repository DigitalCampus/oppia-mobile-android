package org.digitalcampus.oppia.utils.multichoice

import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

internal class AdapterDataSetObserver(private val multiChoiceHelper: MultiChoiceHelper) : AdapterDataObserver() {
    private fun confirmCheckedPositions() {
        if (multiChoiceHelper.checkedItemCount == 0) {
            return
        }
        val adapter = multiChoiceHelper.adapter
        val itemCount = adapter.itemCount
        var checkedCountChanged = false
        val checkStates = multiChoiceHelper.checkStates
        val checkedIdStates = multiChoiceHelper.checkedIdStates
        val choiceActionMode = multiChoiceHelper.choiceActionMode
        if (itemCount == 0) {
            // Optimized path for empty adapter: remove all items.
            checkStates.clear()
            checkedIdStates?.clear()
            multiChoiceHelper.checkedItemCount = 0
            checkedCountChanged = true
        } else if (checkedIdStates != null) {
            // Clear out the positional check states, we'll rebuild it below from IDs.
            checkStates.clear()
            var checkedIndex = 0
            while (checkedIndex < checkedIdStates.size()) {
                val id = checkedIdStates.keyAt(checkedIndex)
                val lastPos = checkedIdStates.valueAt(checkedIndex)
                if (lastPos >= itemCount || id != adapter.getItemId(lastPos)) {
                    // Look around to see if the ID is nearby. If not, uncheck it.
                    val start = Math.max(0, lastPos - MultiChoiceHelper.CHECK_POSITION_SEARCH_DISTANCE)
                    val end = Math.min(lastPos + MultiChoiceHelper.CHECK_POSITION_SEARCH_DISTANCE, itemCount)
                    var found = false
                    for (searchPos in start until end) {
                        val searchId = adapter.getItemId(searchPos)
                        if (id == searchId) {
                            found = true
                            checkStates.put(searchPos, true)
                            checkedIdStates.setValueAt(checkedIndex, searchPos)
                            break
                        }
                    }
                    if (!found) {
                        checkedIdStates.remove(id)
                        checkedIndex--
                        multiChoiceHelper.checkedItemCount = multiChoiceHelper.checkedItemCount - 1
                        checkedCountChanged = true
                        val multiChoiceModeCallback = multiChoiceHelper.multiChoiceModeCallback
                        if (choiceActionMode != null && multiChoiceModeCallback != null) {
                            multiChoiceModeCallback.onItemCheckedStateChanged(choiceActionMode, lastPos, id, false)
                        }
                    }
                } else {
                    checkStates.put(lastPos, true)
                }
                checkedIndex++
            }
        } else {
            // If the total number of items decreased, remove all out-of-range check indexes.
            var i = checkStates.size() - 1
            while (i >= 0 && checkStates.keyAt(i) >= itemCount) {
                if (checkStates.valueAt(i)) {
                    multiChoiceHelper.checkedItemCount = multiChoiceHelper.checkedItemCount - 1
                    checkedCountChanged = true
                }
                checkStates.delete(checkStates.keyAt(i))
                i--
            }
        }
        if (checkedCountChanged && choiceActionMode != null) {
            if (multiChoiceHelper.checkedItemCount == 0) {
                choiceActionMode.finish()
            } else {
                choiceActionMode.invalidate()
            }
        }
    }

    override fun onChanged() {
        confirmCheckedPositions()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        confirmCheckedPositions()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        confirmCheckedPositions()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        confirmCheckedPositions()
    }
}