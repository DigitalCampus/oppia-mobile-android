package org.digitalcampus.oppia.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.oppia.listener.OnItemClickListener

abstract class RecyclerViewClickableAdapter<H : RecyclerViewClickableAdapter<H>.ViewHolder> :
    RecyclerView.Adapter<H>() {

    private var itemClickListener: OnItemClickListener? = null

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(itemView, adapterPosition)
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
}