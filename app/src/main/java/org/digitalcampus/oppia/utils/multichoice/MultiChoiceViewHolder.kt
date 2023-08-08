package org.digitalcampus.oppia.utils.multichoice

import android.view.View
import android.widget.Checkable
import androidx.recyclerview.widget.RecyclerView

abstract class MultiChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var clickListener: View.OnClickListener? = null
    var multiChoiceHelper: MultiChoiceHelper? = null

    init {
        itemView.setOnClickListener { view: View? ->
            if (isMultiChoiceActive) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    multiChoiceHelper?.toggleItemChecked(position, false)
                    updateCheckedState(position)
                }
            } else {
                clickListener?.onClick(view)
            }
        }
        itemView.setOnLongClickListener { view: View? ->
            if (multiChoiceHelper == null || isMultiChoiceActive) {
                return@setOnLongClickListener false
            }
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                multiChoiceHelper?.setItemChecked(position, true, false)
                updateCheckedState(position)
            }
            true
        }
    }

    fun updateCheckedState(position: Int) {
        val isChecked = multiChoiceHelper?.isItemChecked(position) ?: false
        itemView.apply {
            if (this is Checkable) {
                this.isChecked = isChecked
            } else {
                this.isActivated = isChecked
            }
        }
    }

    fun setOnClickListener(clickListener: View.OnClickListener?) {
        this.clickListener = clickListener
    }

    fun bind(multiChoiceHelper: MultiChoiceHelper?, position: Int) {
        this.multiChoiceHelper = multiChoiceHelper
        if (multiChoiceHelper != null) {
            updateCheckedState(position)
        }
    }

    private val isMultiChoiceActive: Boolean
        get() = multiChoiceHelper?.let { it.checkedItemCount > 0 } ?: false
}