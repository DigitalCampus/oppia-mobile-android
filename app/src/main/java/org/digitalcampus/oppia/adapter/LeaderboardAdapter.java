package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.LeaderboardPosition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardPosition> leaderboard;
    private int highlightBgColor;
    private int normalBgColor;
    private int highlightTextColor;
    private int normalTextColor;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView points;
        TextView pos;
        TextView username;
        TextView fullname;
        CardView userCard;


        ViewHolder(View v) {
            super(v);
            points = v.findViewById(R.id.leaderboard_points);
            pos = v.findViewById(R.id.leaderboard_position);
            username = v.findViewById(R.id.leaderboard_username);
            fullname = v.findViewById(R.id.leaderboard_fullname);
            userCard = v.findViewById(R.id.user_card);
        }
    }


    public LeaderboardAdapter(Context ctx, List<LeaderboardPosition> leaderboard){
        this.leaderboard = leaderboard;
        normalBgColor = ContextCompat.getColor(ctx, R.color.text_light);
        highlightBgColor = ContextCompat.getColor(ctx, R.color.highlight_mid);
        normalTextColor = ContextCompat.getColor(ctx, R.color.text_dark);
        highlightTextColor = ContextCompat.getColor(ctx, R.color.text_light);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderboardPosition pos = leaderboard.get(position);
        holder.fullname.setText(pos.getFullname());
        holder.username.setText(pos.getUsername());
        holder.points.setText(String.format(Locale.getDefault(), "%d", pos.getPoints()));
        holder.pos.setText(String.format(Locale.getDefault(), "%d", (position + 1)));

        if (pos.isUser()){
            holder.userCard.setCardBackgroundColor(highlightBgColor);
            holder.fullname.setTextColor(highlightTextColor);
        }
        else{
            holder.userCard.setCardBackgroundColor(normalBgColor);
            holder.fullname.setTextColor(normalTextColor);
        }
    }

    @Override
    public int getItemCount() {
        return leaderboard.size();
    }


}
