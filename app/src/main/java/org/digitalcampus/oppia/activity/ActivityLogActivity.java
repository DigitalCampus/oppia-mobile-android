package org.digitalcampus.oppia.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.adapter.TransferCourseListAdapter;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.fragments.ExportActivityFragment;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.service.bluetooth.BluetoothBroadcastReceiver;
import org.digitalcampus.oppia.service.bluetooth.BluetoothConnectionManager;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferServiceDelegate;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.FetchCourseTransferableFilesTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityLogActivity extends AppActivity implements TrackerServiceListener, ExportActivityListener {

    // Intent request codes
    private Button exportBtn;
    private Button submitBtn;
    private View progressContainer;
    private View actionsContainer;

    private TextView unsentTrackers;
    private TextView submittedTrackers;
    private TextView unexportedTrackers;

    private RecyclerView exportedFilesRecyclerView;
    private RecyclerView.Adapter filesAdapter;
    private ArrayList<File> files = new ArrayList<>();
    private SubmitTrackerMultipleTask omSubmitTrackerMultipleTask;
    private boolean showCompleteExportMessage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitylog);
        // Prevent activity from going to sleep

        exportedFilesRecyclerView = findViewById(R.id.exported_files_list);
        exportBtn = findViewById(R.id.export_btn);
        submitBtn = findViewById(R.id.submit_btn);
        progressContainer = findViewById(R.id.progress_container);
        actionsContainer = findViewById(R.id.export_actions);

        unsentTrackers = findViewById(R.id.highlight_to_submit);
        unexportedTrackers = findViewById(R.id.highlight_to_export);
        submittedTrackers = findViewById(R.id.highlight_submitted);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (omSubmitTrackerMultipleTask == null) {
                    Log.d(TAG, "Sumitting trackers multiple task");
                    updateActions(false);
                    omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(ActivityLogActivity.this);
                    omSubmitTrackerMultipleTask.setTrackerServiceListener(ActivityLogActivity.this);
                    omSubmitTrackerMultipleTask.execute();
                }
            }
        });

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCompleteExportMessage = true;
                exportActivities();

            }
        });

        exportedFilesRecyclerView.setHasFixedSize(true);
        exportedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesAdapter = new ExportedTrackersFileAdapter(files, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                Context ctx = ActivityLogActivity.this;
                File toShare = files.get(position);
                Intent share = ExternalResourceOpener.constructShareFileIntent(ctx, toShare);
                ctx.startActivity(share);
            }
        });
        exportedFilesRecyclerView.setAdapter(filesAdapter);
        exportedFilesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        updateActions(true);

    }

    private void exportActivities() {
        //Check the user has permissions to export activity data
        AdminSecurityManager.with(this).checkAdminPermission(R.id.action_export_activity, new AdminSecurityManager.AuthListener() {
            public void onPermissionGranted() {
                ExportActivityTask task = new ExportActivityTask(ActivityLogActivity.this);
                task.setListener(ActivityLogActivity.this);
                updateActions(false);
                task.execute();
            }
        });
    }

    private void updateActions(boolean enable) {
        if (enable) {
            progressContainer.setVisibility(View.GONE);
            actionsContainer.setVisibility(View.VISIBLE);
            refreshFileList();
            refreshStats();
        } else {
            progressContainer.setVisibility(View.VISIBLE);
            actionsContainer.setVisibility(View.GONE);
        }
    }

    private void refreshStats() {

        DbHelper db = DbHelper.getInstance(this);
        int unsent = db.getUnsentTrackersCount();
        int unexported = db.getUnexportedTrackersCount();
        unexportedTrackers.setText(NumberFormat.getNumberInstance().format(unexported));
        unsentTrackers.setText(NumberFormat.getNumberInstance().format(unsent));
        submittedTrackers.setText(NumberFormat.getNumberInstance().format(db.getSentTrackersCount()));

        Log.d(TAG, "files " + files.size());
        submitBtn.setEnabled((unsent > 0) || files.size() > 0);
        exportBtn.setEnabled((unexported > 0));

    }

    private void refreshFileList() {
        File activityFolder = new File(Storage.getActivityPath(this));
        if (activityFolder.exists()) {
            files.clear();
            String[] children = activityFolder.list();
            for (String dirFiles : children) {
                File exportedActivity = new File(activityFolder, dirFiles);
                files.add(exportedActivity);
            }
            filesAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void trackerComplete(boolean success, String message, List<String> failures) {

        String msg;
        if (message != null && message.length() > 0) {
            msg = message;
            Toast.makeText(this,
                    message,
                    Toast.LENGTH_LONG).show();
        } else {
            msg = getString(success ?  R.string.submit_trackers_success : R.string.error_connection);
        }

        if (failures.size() > 0){
            msg += "\nErrors: \n";
            msg += TextUtils.join("\n", failures);
        }

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        updateActions(true);
    }

    @Override
    public void trackerProgressUpdate() {
        // no need to show progress update in this activity
    }

    @Override
    public void onExportComplete(String filename) {

        if (showCompleteExportMessage) {

            if (filename != null) {
                UIUtils.showAlert(this,
                        R.string.export_task_completed,
                        getString(R.string.export_task_completed_text, filename)
                );
            } else {
                Toast.makeText(this,
                        R.string.export_task_no_activities,
                        Toast.LENGTH_LONG).show();
            }

        }
        updateActions(true);
    }


}
