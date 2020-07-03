package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
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

    @Override
    public void onStart() {
        super.onStart();
        initialize(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_quiz_attempts);
        getAppComponent().inject(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            //There is no quiz?
            return;
        }

        stats = (QuizStats) bundle.getSerializable(QuizStats.TAG);
        setTitle(stats.getSectionTitle() + " > " + stats.getQuizTitle());

        TextView average = findViewById(R.id.highlight_average);
        TextView best = findViewById(R.id.highlight_best);
        TextView numAttempts = findViewById(R.id.highlight_attempted);
        Button retakeQuizBtn = findViewById(R.id.retake_quiz_btn);
        RecyclerView attemptsList = findViewById(R.id.attempts_list);
        numAttempts.setText(String.valueOf(stats.getNumAttempts()));

        if (stats.getNumAttempts() == 0){
            retakeQuizBtn.setVisibility(View.GONE);
            attemptsList.setVisibility(View.GONE);
            average.setText("-");
            best.setText("-");
            findViewById(R.id.empty_state).setVisibility(View.VISIBLE);
            Button takeQuizBtn = findViewById(R.id.btn_take_quiz);
            takeQuizBtn.setOnClickListener(view -> takeQuiz());
        }
        else{
            average.setText(stats.getAveragePercent() + "%");
            best.setText(stats.getPercent() + "%");
            retakeQuizBtn.setOnClickListener(view -> takeQuiz());
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
        attemptsList.setAdapter(adapter);
    }

    private void takeQuiz(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(CourseIndexActivity.JUMPTO_TAG, stats.getDigest());
        setResult(CourseIndexActivity.RESULT_JUMPTO, returnIntent);
        finish();
    }
}
