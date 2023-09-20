package org.digitalcampus.oppia.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowActivityTypeBinding
import org.digitalcampus.oppia.adapter.ActivityTypesAdapter.ActivityTypesViewHolder
import org.digitalcampus.oppia.listener.OnItemClickListener
import org.digitalcampus.oppia.model.ActivityType

class ActivityTypesAdapter(
    private val context: Context,
    private val activityTypes: List<ActivityType>
) : RecyclerView.Adapter<ActivityTypesViewHolder>() {
    private var itemClickListener: OnItemClickListener? = null

    inner class ActivityTypesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowActivityTypeBinding

        init {
            binding = RowActivityTypeBinding.bind(itemView)
            itemView.setOnClickListener {
                val activityType = getItemAtPosition(adapterPosition)
                activityType.isEnabled = !activityType.isEnabled
                notifyDataSetChanged()
                if (itemClickListener != null) {
                    itemClickListener!!.onItemClick(
                        itemView,
                        adapterPosition,
                        activityType.type,
                        activityType.isEnabled
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityTypesViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_activity_type, parent, false)
        return ActivityTypesViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: ActivityTypesViewHolder, position2: Int) {
        val activityType = getItemAtPosition(holder.adapterPosition)

        holder.binding.tvActivityType.text = activityType.name
        holder.binding.tvActivityType.setTextColor(activityType.color)
        holder.binding.imgShowHide.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                activityType.color,
                BlendModeCompat.SRC_OVER
            )
        if (activityType.isEnabled) {
            holder.binding.imgShowHide.setImageResource(R.drawable.ic_eye_show)
            holder.binding.imgShowHide.background.alpha = 255
            holder.binding.imgShowHide.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    Color.WHITE,
                    BlendModeCompat.SRC_ATOP
                )
        } else {
            holder.binding.imgShowHide.setImageResource(R.drawable.ic_eye_hide)
            holder.binding.imgShowHide.background.alpha = 0
            holder.binding.imgShowHide.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    activityType.color,
                    BlendModeCompat.SRC_ATOP
                )
        }
    }

    override fun getItemCount(): Int {
        return activityTypes.size
    }

    fun getItemAtPosition(position: Int): ActivityType {
        return activityTypes[position]
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
}