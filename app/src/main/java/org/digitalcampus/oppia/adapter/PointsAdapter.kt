package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowFragmentPointsListBinding
import org.digitalcampus.oppia.adapter.PointsAdapter.PointsViewHolder
import org.digitalcampus.oppia.model.Points

class PointsAdapter(private val context: Context, private val points: List<Points>) :
    RecyclerView.Adapter<PointsViewHolder>() {

    inner class PointsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowFragmentPointsListBinding = RowFragmentPointsListBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointsViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_fragment_points_list, parent, false)
        return PointsViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: PointsViewHolder, position: Int) {
        val point = getItemAtPosition(position)
        viewHolder.binding.pointsDescription.text = point.getDescriptionPrettified()
        viewHolder.binding.pointsTime.text = point.getTimeHoursMinutes()
        viewHolder.binding.pointsDate.text = point.getDateDayMonth()
        viewHolder.binding.pointsPoints.text = point.pointsAwarded.toString()
    }

    override fun getItemCount(): Int {
        return points.size
    }

    fun getItemAtPosition(position: Int): Points {
        return points[position]
    }
}