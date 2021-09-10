package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentGlobalQuizAttemptsBinding;
import org.digitalcampus.oppia.activity.QuizAttemptActivity;
import org.digitalcampus.oppia.adapter.GlobalQuizAttemptsAdapter;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizAttemptRepository;

import java.util.List;

import javax.inject.Inject;

import androidx.recyclerview.widget.RecyclerView;

public class GlobalQuizAttemptsFragment extends AppFragment {


    @Inject
    QuizAttemptRepository attemptsRepository;
    private FragmentGlobalQuizAttemptsBinding binding;

    public static GlobalQuizAttemptsFragment newInstance() {
        return new GlobalQuizAttemptsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGlobalQuizAttemptsBinding.inflate(inflater, container, false);
        getAppComponent().inject(this);

        List<QuizAttempt> attempts = attemptsRepository.getGlobalQuizAttempts(this.getContext());
        GlobalQuizAttemptsAdapter adapter = new GlobalQuizAttemptsAdapter(this.getContext(), attempts);
        binding.attemptsList.setAdapter(adapter);
        adapter.setOnItemClickListener((v, position) -> {
            Intent i = new Intent(getActivity(), QuizAttemptActivity.class);
            Bundle tb = new Bundle();
            QuizAttempt attempt = attempts.get(position);
            tb.putSerializable(QuizAttempt.TAG, attempt);
            tb.putBoolean(QuizAttemptActivity.SHOW_ATTEMPT_BUTTON, true);
            i.putExtras(tb);
            startActivity(i);
        });

        binding.emptyState.setVisibility(attempts.isEmpty() ? View.VISIBLE : View.GONE);

        return binding.getRoot();
    }

}
