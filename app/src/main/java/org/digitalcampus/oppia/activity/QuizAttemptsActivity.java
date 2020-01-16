package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.QuizAttemptAdapter;
import org.digitalcampus.oppia.adapter.TagsAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.model.Tag;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.recyclerview.widget.RecyclerView;

public class QuizAttemptsActivity extends AppActivity {

    private View loadingView;
    private RecyclerView attemptsList;

    private List<QuizAttempt> attempts;
    private QuizAttemptAdapter adapter;
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
        initializeDagger();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            //There is no quiz?
            return;
        }

        stats = (QuizStats) bundle.getSerializable(QuizStats.TAG);

        loadingView = findViewById(R.id.loading_attempts);
        TextView average = findViewById(R.id.highlight_average);
        TextView best = findViewById(R.id.highlight_best);
        TextView numAttempts = findViewById(R.id.highlight_attempted);

        setTitle(stats.getSectionTitle() + " > " + stats.getQuizTitle());
        average.setText(stats.getAveragePercent() + "%");
        best.setText(stats.getPercent() + "%");
        numAttempts.setText(String.valueOf(stats.getNumAttempts()));

        Button retakeQuizBtn = findViewById(R.id.retake_quiz_btn);
        retakeQuizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(CourseIndexActivity.JUMPTO_TAG, stats.getDigest());
                setResult(CourseIndexActivity.RESULT_JUMPTO, returnIntent);
                finish();
            }
        });

        attempts = attemptsRepository.getQuizAttempts(this, stats);
        adapter = new QuizAttemptAdapter(this.getBaseContext(), attempts);
        attemptsList = findViewById(R.id.attempts_list);
        attemptsList.setAdapter(adapter);
        loadingView.setVisibility(View.GONE);
    }

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getApplication();
        app.getComponent().inject(this);
    }

}
