package org.digitalcampus.oppia.listener;

import androidx.appcompat.view.ActionMode;

public interface MultiChoiceModeListener extends ActionMode.Callback {
    /**
     * Called when an item is checked or unchecked during selection mode.
     *
     * @param mode     The {@link ActionMode} providing the selection startSupportActionModemode
     * @param position Adapter position of the item that was checked or unchecked
     * @param id       Adapter ID of the item that was checked or unchecked
     * @param checked  <code>true</code> if the item is now checked, <code>false</code>
     *                 if the item is now unchecked.
     */
    void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked);
}
