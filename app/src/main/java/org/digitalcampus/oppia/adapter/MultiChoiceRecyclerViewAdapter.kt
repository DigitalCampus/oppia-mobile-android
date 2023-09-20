package org.digitalcampus.oppia.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.oppia.utils.multichoice.MultiChoiceHelper
import org.digitalcampus.oppia.utils.multichoice.MultiChoiceViewHolder

abstract class MultiChoiceRecyclerViewAdapter<H : MultiChoiceRecyclerViewAdapter<H>.ViewHolder> :
    RecyclerView.Adapter<H>() {

    var multiChoiceHelper: MultiChoiceHelper? = null
    protected var isMultiChoiceMode = false

    fun updateViewHolder(viewHolder: MultiChoiceViewHolder, position: Int) {
        viewHolder.updateCheckedState(position)
    }

    fun setEnterOnMultiChoiceMode(multiChoiceModeActive: Boolean) {
        isMultiChoiceMode = multiChoiceModeActive
    }

    abstract inner class ViewHolder(itemView: View) : MultiChoiceViewHolder(itemView) {
        init {
            bind(multiChoiceHelper, adapterPosition)
        }
    }
}