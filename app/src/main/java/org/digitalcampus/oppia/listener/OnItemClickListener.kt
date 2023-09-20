package org.digitalcampus.oppia.listener
import android.view.View

interface OnItemClickListener {
    fun onItemClick(view: View? = null, position: Int, type: String? = null, enabled: Boolean = true)
}