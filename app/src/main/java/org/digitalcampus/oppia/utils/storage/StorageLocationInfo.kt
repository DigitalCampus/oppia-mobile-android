package org.digitalcampus.oppia.utils.storage

import android.content.Context
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.utils.TextUtilsJava

class StorageLocationInfo internal constructor(val type: String, val path: String, val readonly: Boolean, val removable: Boolean, val number: Int) {
    fun getDisplayName(ctx: Context): String {
        return ctx.getString(if (TextUtilsJava.equals(type, PrefsActivity.STORAGE_OPTION_INTERNAL)) R.string.prefStorageOptionInternal else R.string.prefStorageOptionExternal)
    }
}