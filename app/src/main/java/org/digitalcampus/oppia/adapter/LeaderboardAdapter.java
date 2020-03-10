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
        private TextView points;
        private TextView pos;
        private TextView username;
        private TextView fullname;
        private CardView userCard;


        public GlobalQuizAttemptsViewHolder(View v) {
            super(v);
            points = v.findViewById(R.id.leaderboard_points);
            pos = v.findViewById(R.id.leaderboard_position);
            username = v.findViewById(R.id.leaderboard_username);
            fullname = v.findViewById(R.id.leaderboard_fullname);
            userCard = v.findViewById(R.id.user_card);
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
        Leaderboard pos = leaderboard.get(position);
        holder.fullname.setText(pos.getFullname());
        holder.username.setText(pos.getUsername());
        holder.points.setText(String.format(Locale.getDefault(), "%d", pos.getPoints()));
        holder.pos.setText(String.format(Locale.getDefault(), "%d", (position + 1)));

        if (pos.isUser()) {
            holder.userCard.setCardBackgroundColor(highlightBgColor);
            holder.fullname.setTextColor(highlightTextColor);
        } else {
            holder.userCard.setCardBackgroundColor(normalBgColor);
            holder.fullname.setTextColor(normalTextColor);
        }
    }

    @Override
    public int getItemCount() {
        return leaderboard.size();
    }


}
