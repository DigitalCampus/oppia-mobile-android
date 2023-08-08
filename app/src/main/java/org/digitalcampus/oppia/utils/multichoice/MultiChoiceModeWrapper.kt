package org.digitalcampus.oppia.utils.multichoice

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import org.digitalcampus.oppia.listener.MultiChoiceModeListener

class MultiChoiceModeWrapper(private val multiChoiceHelper: MultiChoiceHelper, private val wrapped: MultiChoiceModeListener) : MultiChoiceModeListener {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        return wrapped.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return wrapped.onCreateActionMode(mode, menu)
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return wrapped.onActionItemClicked(mode, item)
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        wrapped.onDestroyActionMode(mode)
        multiChoiceHelper.choiceActionMode = null
        multiChoiceHelper.clearChoices()
    }

    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
        wrapped.onItemCheckedStateChanged(mode, position, id, checked)
    }
}