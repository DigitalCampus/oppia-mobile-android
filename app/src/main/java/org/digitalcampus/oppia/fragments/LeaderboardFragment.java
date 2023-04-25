package org.digitalcampus.oppia.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentLeaderboardBinding;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.adapter.LeaderboardAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.LeaderboardUtils;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.db_model.Leaderboard;
import org.digitalcampus.oppia.task.UpdateLeaderboardFromServerTask;
import org.digitalcampus.oppia.task.result.BasicResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


public class LeaderboardFragment extends AppFragment implements SubmitListener {

    private FragmentLeaderboardBinding binding;

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    @Inject
    ApiEndpoint apiEndpoint;

    private LeaderboardAdapter adapter;
    private List<Leaderboard> leaderboard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);

        binding.loadingSpinner.setVisibility(View.VISIBLE);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAppComponent().inject(this);
        leaderboard = new ArrayList<>();

        adapter = new LeaderboardAdapter(this.getContext(), leaderboard);
        binding.listLeaderboard.setLayoutManager( new LinearLayoutManager(this.getContext()));
        binding.listLeaderboard.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (LeaderboardUtils.shouldFetchLeaderboard(prefs)){
            UpdateLeaderboardFromServerTask task = new UpdateLeaderboardFromServerTask(super.getActivity(), apiEndpoint);
            task.setListener(this);
            task.execute();
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
            binding.tvRanking.setText(String.format(Locale.getDefault(), "%d", userPos.getPosition()));
            binding.tvTotalPoints.setText(getString(R.string.leaderboard_points, String.format(Locale.getDefault(), "%d", userPos.getPoints())));
        }

        adapter.notifyDataSetChanged();

        binding.loadingSpinner.setVisibility(View.GONE);
        binding.listLeaderboard.setVisibility(View.VISIBLE);
    }

    @Override
    public void submitComplete(BasicResult result) {
        if (result.isSuccess()){
            updateLeaderboard();
        }
        else{
            binding.loadingSpinner.setVisibility(View.GONE);
            binding.listLeaderboard.setVisibility(View.GONE);
            binding.errorState.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void apiKeyInvalidated() {
        ((AppActivity)this.getActivity()).apiKeyInvalidated();
    }
}
