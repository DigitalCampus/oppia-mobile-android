package org.digitalcampus.oppia.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowTagBinding
import org.digitalcampus.oppia.adapter.TagsAdapter.TagsViewHolder
import org.digitalcampus.oppia.listener.OnItemClickListener
import org.digitalcampus.oppia.model.Tag

class TagsAdapter(private val context: Context, private val tags: List<Tag>) :
    RecyclerView.Adapter<TagsViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null

    inner class TagsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowTagBinding = RowTagBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(itemView, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_tag, parent, false)
        return TagsViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: TagsViewHolder, position: Int) {
        val t = getItemAtPosition(position)
        viewHolder.binding.tagName.text = t.name
        viewHolder.binding.tagCount.text = t.countAvailable.toString()
        if (t.isHighlight) {
            viewHolder.binding.tagName.setTypeface(null, Typeface.BOLD)
        } else {
            viewHolder.binding.tagName.setTypeface(null, Typeface.NORMAL)
        }
        if (!t.description.isNullOrBlank()) {
            viewHolder.binding.tagDescription.text = t.description
            viewHolder.binding.tagDescription.visibility = View.VISIBLE
        } else {
            viewHolder.binding.tagDescription.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    fun getItemAtPosition(position: Int): Tag {
        return tags[position]
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
}