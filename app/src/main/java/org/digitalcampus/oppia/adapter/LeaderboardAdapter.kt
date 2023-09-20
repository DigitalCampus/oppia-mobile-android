package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowLeaderboardBinding
import org.digitalcampus.oppia.model.db_model.Leaderboard
import java.util.Locale

class LeaderboardAdapter(context: Context, private val leaderboard: List<Leaderboard>) :
    RecyclerView.Adapter<LeaderboardAdapter.GlobalQuizAttemptsViewHolder>() {
    
    private val highlightBgColor: Int
    private val normalBgColor: Int
    private val highlightTextColor: Int
    private val normalTextColor: Int

    inner class GlobalQuizAttemptsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowLeaderboardBinding = RowLeaderboardBinding.bind(itemView)
    }

    init {
        normalBgColor = ContextCompat.getColor(context, R.color.text_light)
        highlightBgColor = ContextCompat.getColor(context, R.color.theme_secondary_light)
        normalTextColor = ContextCompat.getColor(context, R.color.text_dark)
        highlightTextColor = ContextCompat.getColor(context, R.color.text_light)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlobalQuizAttemptsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_leaderboard, parent, false)
        return GlobalQuizAttemptsViewHolder(v)
    }

    override fun onBindViewHolder(holder: GlobalQuizAttemptsViewHolder, position: Int) {
        val leaderboardItem = leaderboard[position]
        holder.binding.leaderboardFullname.text = leaderboardItem.fullname
        holder.binding.leaderboardUsername.text = leaderboardItem.username
        holder.binding.leaderboardPoints.text = String.format(Locale.getDefault(), "%d", leaderboardItem.points)
        holder.binding.leaderboardPosition.text = String.format(Locale.getDefault(), "%d", leaderboardItem.position)

        if (leaderboardItem.isUser) {
            holder.binding.userCard.setCardBackgroundColor(highlightBgColor)
            holder.binding.leaderboardFullname.setTextColor(highlightTextColor)
        } else {
            holder.binding.userCard.setCardBackgroundColor(normalBgColor)
            holder.binding.leaderboardFullname.setTextColor(normalTextColor)
        }
    }

    override fun getItemCount(): Int {
        return leaderboard.size
    }
}