package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.ActivityLogRepository;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PrivacyActivity extends AppActivity {

    // Intent request codes
    private Button exportBtn;
    private Button submitBtn;
    private View progressContainer;
    private View actionsContainer;

    private TextView unsentTrackers;
    private TextView submittedTrackers;
    private TextView unexportedTrackers;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        // Prevent activity from going to sleep
        getAppComponent().inject(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();


    }

}
