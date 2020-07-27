package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.digitalcampus.mobile.learning.R;
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

    public static GlobalQuizAttemptsFragment newInstance() {
        return new GlobalQuizAttemptsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_global_quiz_attempts, container, false);
        getAppComponent().inject(this);

        List<QuizAttempt> attempts = attemptsRepository.getGlobalQuizAttempts(this.getContext());
        GlobalQuizAttemptsAdapter adapter = new GlobalQuizAttemptsAdapter(this.getContext(), attempts);
        RecyclerView attemptsList = layout.findViewById(R.id.attempts_list);
        attemptsList.setAdapter(adapter);
        adapter.setOnItemClickListener((v, position) -> {
            Intent i = new Intent(getActivity(), QuizAttemptActivity.class);
            Bundle tb = new Bundle();
            QuizAttempt attempt = attempts.get(position);
            tb.putSerializable(QuizAttempt.TAG, attempt);
            i.putExtras(tb);
            startActivity(i);
        });

        layout.findViewById(R.id.empty_state).setVisibility(attempts.isEmpty() ? View.VISIBLE : View.GONE);

        return layout;
    }

}
