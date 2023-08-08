package org.digitalcampus.oppia.utils.multichoice

import android.content.Context
import android.util.SparseBooleanArray
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.collection.LongSparseArray
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.oppia.listener.MultiChoiceModeListener

/**
 * Helper class to reproduce ListView's modal MultiChoice mode with a RecyclerView.
 * Compatible with API 7+.
 * Declare and use this class from inside your Adapter.
 *
 * https://medium.com/@BladeCoder/implementing-a-modal-selection-helper-for-recyclerview-1e888b4cd5b9
 *
 * @author Christophe Beyls
 */
class MultiChoiceHelper(private val activity: AppCompatActivity, val adapter: RecyclerView.Adapter<*>) {
    val checkStates: SparseBooleanArray
    var checkedIdStates: LongSparseArray<Int>? = null
    var checkedItemCount = 0
    var multiChoiceModeCallback: MultiChoiceModeWrapper? = null
        private set
    var choiceActionMode: ActionMode? = null

    /**
     * Make sure this constructor is called before setting the adapter on the RecyclerView
     * so this class will be notified before the RecyclerView in case of data set changes.
     */
    init {
        adapter.registerAdapterDataObserver(AdapterDataSetObserver(this))
        checkStates = SparseBooleanArray(0)
        if (adapter.hasStableIds()) {
            checkedIdStates = LongSparseArray(0)
        }
    }

    val context: Context
        get() = activity

    fun setMultiChoiceModeListener(listener: MultiChoiceModeListener?) {
        multiChoiceModeCallback = listener?.let { MultiChoiceModeWrapper(this, it) }
    }

    fun isItemChecked(position: Int): Boolean {
        return checkStates[position]
    }

    fun clearChoices() {
        if (checkedItemCount > 0) {
            val start = checkStates.keyAt(0)
            val end = checkStates.keyAt(checkStates.size() - 1)
            checkStates.clear()
            if (checkedIdStates != null) {
                checkedIdStates?.clear()
            }
            checkedItemCount = 0
            adapter.notifyItemRangeChanged(start, end - start + 1)
            if (choiceActionMode != null) {
                choiceActionMode!!.finish()
            }
        }
    }

    fun setItemChecked(position: Int, value: Boolean, notifyChanged: Boolean) {
        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value) {
            startSupportActionModeIfNeeded()
        }
        val oldValue = checkStates[position]
        checkStates.put(position, value)
        if (oldValue != value) {
            val id = adapter.getItemId(position)
            if (checkedIdStates != null) {
                if (value) {
                    checkedIdStates?.put(id, position)
                } else {
                    checkedIdStates?.remove(id)
                }
            }
            if (value) {
                checkedItemCount++
            } else {
                checkedItemCount--
            }
            if (notifyChanged) {
                adapter.notifyItemChanged(position)
            }
            if (choiceActionMode != null) {
                multiChoiceModeCallback!!.onItemCheckedStateChanged(choiceActionMode!!, position, id, value)
                if (checkedItemCount == 0 && choiceActionMode != null) {
                    choiceActionMode!!.finish()
                }
            }
        }
    }

    fun toggleItemChecked(position: Int, notifyChanged: Boolean) {
        setItemChecked(position, !isItemChecked(position), notifyChanged)
    }

    private fun startSupportActionModeIfNeeded() {
        if (choiceActionMode == null) {
            checkNotNull(multiChoiceModeCallback) { "No callback set" }
            choiceActionMode = activity.startSupportActionMode(multiChoiceModeCallback!!)
        }
    }

    companion object {
        /**
         * A handy ViewHolder base class which works with the MultiChoiceHelper
         * and reproduces the default behavior of a ListView.
         */
        const val CHECK_POSITION_SEARCH_DISTANCE = 20
    }
}