package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.LeaderboardAdapter;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.LeaderboardPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class LeaderboardFragment extends Fragment {

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    RecyclerView leaderboard_view;
    TextView rankingPosition;
    TextView totalPoints;
    ProgressBar loadingSpinner;

    LeaderboardAdapter adapter;
    List<LeaderboardPosition> leaderboard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        leaderboard_view = (RecyclerView) vv.findViewById(R.id.list_leaderboard);
        rankingPosition = (TextView) vv.findViewById(R.id.tv_ranking);
        totalPoints= (TextView) vv.findViewById(R.id.tv_total_points);

        loadingSpinner = (ProgressBar) vv.findViewById(R.id.loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);

        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        leaderboard = new ArrayList<>();

        adapter = new LeaderboardAdapter(this.getContext(), leaderboard);
        leaderboard_view.setLayoutManager( new LinearLayoutManager(this.getContext()));
        leaderboard_view.setAdapter(adapter);

        updateLeaderboard();
    }

    private void updateLeaderboard(){

        leaderboard.clear();

        leaderboard.add(new LeaderboardPosition("aaa", "Nombre usuario", 2235));
        leaderboard.add(new LeaderboardPosition("aaaaaa", "Nombre usuario2", 535));
        leaderboard.add(new LeaderboardPosition("bbbb", "Nombre usuario3", 35));
        leaderboard.add(new LeaderboardPosition("jjoseba", "Joseba S.", 195));
        leaderboard.add(new LeaderboardPosition("bbbb", "Nombre usuario3", 535));
        leaderboard.add(new LeaderboardPosition("bbbb", "Nombre usuario3", 2));
        leaderboard.add(new LeaderboardPosition("bbbb", "Nombre usuario3", 0));
        String username = SessionManager.getUsername(this.getContext());
        Collections.sort(leaderboard);

        LeaderboardPosition userPos = null;
        int i = 0;
        for (i=0; i<leaderboard.size(); i++){
            LeaderboardPosition pos = leaderboard.get(i);
            if (pos.getUsername().equals(username)){
                pos.setUser(true);
                userPos = pos;
                break;
            }
        }

        if ( userPos!= null){
            rankingPosition.setText(String.format(Locale.getDefault(), "%dº", (i+1)));
            totalPoints.setText(getString(R.string.leaderboard_points, String.format(Locale.getDefault(), "%d", userPos.getPoints())));
        }

        adapter.notifyDataSetChanged();

        loadingSpinner.setVisibility(View.GONE);
        leaderboard_view.setVisibility(View.VISIBLE);
    }
}
