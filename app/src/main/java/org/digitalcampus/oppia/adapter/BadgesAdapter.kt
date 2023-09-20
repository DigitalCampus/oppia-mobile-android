package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowFragmentBadgesListBinding
import org.digitalcampus.oppia.adapter.BadgesAdapter.BadgesViewHolder
import org.digitalcampus.oppia.listener.OnItemClickListener
import org.digitalcampus.oppia.model.Badge
import org.digitalcampus.oppia.utils.TextUtilsJava

class BadgesAdapter(private val context: Context, private val badges: List<Badge>) :
    RecyclerView.Adapter<BadgesViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class BadgesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowFragmentBadgesListBinding

        init {
            binding = RowFragmentBadgesListBinding.bind(itemView)
            binding.btnDownloadCertificate.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener?.onItemClick(itemView, adapterPosition, "", true)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgesViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_fragment_badges_list, parent, false)
        return BadgesViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: BadgesViewHolder, position: Int) {
        val badge = getItemAtPosition(position)
        viewHolder.binding.badgesDescription.text = badge.description
        viewHolder.binding.badgesDate.text = badge.getDateAsString()
        viewHolder.binding.btnDownloadCertificate.visibility =
            if (TextUtilsJava.isEmpty(badge.certificatePdf)) View.INVISIBLE else View.VISIBLE
    }

    override fun getItemCount(): Int {
        return badges.size
    }

    fun getItemAtPosition(position: Int): Badge {
        return badges[position]
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

}