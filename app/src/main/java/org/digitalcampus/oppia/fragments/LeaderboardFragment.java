package org.digitalcampus.oppia.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.adapter.LeaderboardAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.LeaderboardUtils;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.db_model.Leaderboard;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.UpdateLeaderboardFromServerTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


public class LeaderboardFragment extends AppFragment implements SubmitListener {

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    private RecyclerView leaderboardView;
    private TextView rankingPosition;
    private TextView totalPoints;
    private ProgressBar loadingSpinner;

    @Inject
    ApiEndpoint apiEndpoint;

    private LeaderboardAdapter adapter;
    private List<Leaderboard> leaderboard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        leaderboardView = vv.findViewById(R.id.list_leaderboard);
        rankingPosition = vv.findViewById(R.id.tv_ranking);
        totalPoints= vv.findViewById(R.id.tv_total_points);

        loadingSpinner = vv.findViewById(R.id.loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);

        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAppComponent().inject(this);
        leaderboard = new ArrayList<>();

        adapter = new LeaderboardAdapter(this.getContext(), leaderboard);
        leaderboardView.setLayoutManager( new LinearLayoutManager(this.getContext()));
        leaderboardView.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (LeaderboardUtils.shouldFetchLeaderboard(prefs)){
            Payload p = new Payload();
            UpdateLeaderboardFromServerTask task = new UpdateLeaderboardFromServerTask(super.getActivity(), apiEndpoint);
            task.seListener(this);
            task.execute(p);
        }
        else {
            updateLeaderboard();
        }

    }

    private void updateLeaderboard(){

        leaderboard.clear();
        leaderboard.addAll(App.getDb().leaderboardDao().getAll());

        String username = SessionManager.getUsername(this.getContext());
        Collections.sort(leaderboard);

        Leaderboard userPos = null;
        int i;
        for (i=0; i<leaderboard.size(); i++){
            Leaderboard leaderboardItem = leaderboard.get(i);
            if (leaderboardItem.getUsername().equals(username)){
                leaderboardItem.setUser(true);
                userPos = leaderboardItem;
                break;
            }
        }

        if ( userPos != null){
            rankingPosition.setText(String.format(Locale.getDefault(), "%d", (i+1)));
            totalPoints.setText(getString(R.string.leaderboard_points, String.format(Locale.getDefault(), "%d", userPos.getPoints())));
        }

        adapter.notifyDataSetChanged();

        loadingSpinner.setVisibility(View.GONE);
        leaderboardView.setVisibility(View.VISIBLE);
    }

    @Override
    public void submitComplete(Payload response) {
        updateLeaderboard();
    }

    @Override
    public void apiKeyInvalidated() {
        ((AppActivity)this.getActivity()).apiKeyInvalidated();
    }
}
