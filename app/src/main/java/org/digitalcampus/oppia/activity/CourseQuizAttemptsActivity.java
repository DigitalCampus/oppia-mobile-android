package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityCourseQuizAttemptsBinding;
import org.digitalcampus.mobile.learning.databinding.ActivityQuizAttemptBinding;
import org.digitalcampus.oppia.adapter.QuizAttemptAdapter;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;

import java.util.List;

import javax.inject.Inject;

import androidx.recyclerview.widget.RecyclerView;

public class CourseQuizAttemptsActivity extends AppActivity {

    private QuizStats stats;

    @Inject
    QuizAttemptRepository attemptsRepository;
    private ActivityCourseQuizAttemptsBinding binding;

    @Override
    public void onStart() {
        super.onStart();
        initialize(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseQuizAttemptsBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            //There is no quiz?
            return;
        }

        stats = (QuizStats) bundle.getSerializable(QuizStats.TAG);
        setTitle(stats.getSectionTitle() + " > " + stats.getQuizTitle());

        binding.viewQuizStats.highlightAttempted.setText(String.valueOf(stats.getNumAttempts()));

        if (stats.getNumAttempts() == 0){
            binding.retakeQuizBtn.setVisibility(View.GONE);
            binding.attemptsList.setVisibility(View.GONE);
            binding.viewQuizStats.highlightAverage.setText("-");
            binding.viewQuizStats.highlightBest.setText("-");
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.btnTakeQuiz.setOnClickListener(view -> takeQuiz());
        }
        else{
            binding.viewQuizStats.highlightAverage.setText(stats.getAveragePercent() + "%");
            binding.viewQuizStats.highlightBest.setText(stats.getPercent() + "%");
            binding.retakeQuizBtn.setOnClickListener(view -> takeQuiz());
        }

        final List<QuizAttempt> attempts = attemptsRepository.getQuizAttempts(this, stats);
        QuizAttemptAdapter adapter = new QuizAttemptAdapter(this, attempts);
        adapter.setOnItemClickListener((v, position) -> {
            Intent i = new Intent(CourseQuizAttemptsActivity.this, QuizAttemptActivity.class);
            Bundle tb = new Bundle();
            QuizAttempt attempt = attempts.get(position);
            attempt.setSectionTitle(stats.getSectionTitle());
            attempt.setQuizTitle(stats.getQuizTitle());
            tb.putSerializable(QuizAttempt.TAG, attempt);
            i.putExtras(tb);
            startActivity(i);
        });
        binding.attemptsList.setAdapter(adapter);
    }

    private void takeQuiz(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(CourseIndexActivity.JUMPTO_TAG, stats.getDigest());
        setResult(CourseIndexActivity.RESULT_JUMPTO, returnIntent);
        finish();
    }
}
