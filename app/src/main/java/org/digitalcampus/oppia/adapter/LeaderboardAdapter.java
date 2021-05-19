package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowLeaderboardBinding;
import org.digitalcampus.oppia.model.db_model.Leaderboard;

import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.GlobalQuizAttemptsViewHolder> {

    private List<Leaderboard> leaderboard;
    private int highlightBgColor;
    private int normalBgColor;
    private int highlightTextColor;
    private int normalTextColor;

    public class GlobalQuizAttemptsViewHolder extends RecyclerView.ViewHolder {

        private final RowLeaderboardBinding binding;

        public GlobalQuizAttemptsViewHolder(View v) {
            super(v);
            binding = RowLeaderboardBinding.bind(v);

        }
    }


    public LeaderboardAdapter(Context ctx, List<Leaderboard> leaderboard) {
        this.leaderboard = leaderboard;
        normalBgColor = ContextCompat.getColor(ctx, R.color.text_light);
        highlightBgColor = ContextCompat.getColor(ctx, R.color.theme_secondary_light);
        normalTextColor = ContextCompat.getColor(ctx, R.color.text_dark);
        highlightTextColor = ContextCompat.getColor(ctx, R.color.text_light);
    }

    @Override
    public GlobalQuizAttemptsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_leaderboard, parent, false);
        return new GlobalQuizAttemptsViewHolder(v);

    }

    @Override
    public void onBindViewHolder(GlobalQuizAttemptsViewHolder holder, int position) {
        Leaderboard leaderboardItem = leaderboard.get(position);
        holder.binding.leaderboardFullname.setText(leaderboardItem.getFullname());
        holder.binding.leaderboardUsername.setText(leaderboardItem.getUsername());
        holder.binding.leaderboardPoints.setText(String.format(Locale.getDefault(), "%d", leaderboardItem.getPoints()));
        holder.binding.leaderboardPosition.setText(String.format(Locale.getDefault(), "%d", (position + 1)));

        if (leaderboardItem.isUser()) {
            holder.binding.userCard.setCardBackgroundColor(highlightBgColor);
            holder.binding.leaderboardFullname.setTextColor(highlightTextColor);
        } else {
            holder.binding.userCard.setCardBackgroundColor(normalBgColor);
            holder.binding.leaderboardFullname.setTextColor(normalTextColor);
        }
    }

    @Override
    public int getItemCount() {
        return leaderboard.size();
    }


}
