package org.digitalcampus.oppia.adapter;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView points;
        TextView pos;
        TextView username;
        TextView fullname;

        public ViewHolder(View v) {
            super(v);
            points = (TextView) v.findViewById(R.id.leaderboard_points);
            pos = (TextView) v.findViewById(R.id.leaderboard_position);
            username = (TextView) v.findViewById(R.id.leaderboard_username);
            fullname = (TextView) v.findViewById(R.id.leaderboard_fullname);

        }
    }


    public LeaderboardAdapter(List<LeaderboardPosition> leaderboard){
        this.leaderboard = leaderboard;

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
    }

    @Override
    public int getItemCount() {
        return leaderboard.size();
    }


}
