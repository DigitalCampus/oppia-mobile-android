package org.digitalcampus.oppia.utils.storage

import android.os.Environment
import org.digitalcampus.oppia.annotations.Mockable
import java.io.File

@Mockable
class ExternalStorageState {
    fun getState(file: File?): String {
        return Environment.getExternalStorageState(file)
    }

    companion object {
        const val STATE_NOT_WRITABLE = "STATE_NOT_WRITABLE"
        private var externalStorageState = ExternalStorageState()

        @JvmStatic
        fun getExternalStorageState(file: File): String {
            return externalStorageState.getState(file)
        }

        @JvmStatic
        fun setExternalStorageState(state: ExternalStorageState) {
            externalStorageState = state
        }
    }
}