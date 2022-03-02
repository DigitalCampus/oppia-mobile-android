package org.digitalcampus.oppia.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityActivitylogBinding;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.ActivityLogRepository;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.task.result.BasicResult;
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

public class ActivityLogActivity extends AppActivity implements TrackerServiceListener, ExportActivityListener, ExportedTrackersFileAdapter.OnItemClickListener, APIRequestFinishListener {


    @Inject
    ApiEndpoint apiEndpoint;

    @Inject
    ActivityLogRepository logsRepository;

    private RecyclerView.Adapter<ExportedTrackersFileAdapter.ViewHolder> filesAdapter;
    private ArrayList<File> files = new ArrayList<>();

    private RecyclerView.Adapter<ExportedTrackersFileAdapter.ViewHolder> archivedFilesAdapter;
    private ArrayList<File> archivedFiles = new ArrayList<>();

    private SubmitTrackerMultipleTask omSubmitTrackerMultipleTask;
    private boolean showCompleteExportMessage;
    private ActivityActivitylogBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivitylogBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        // Prevent activity from going to sleep
        getAppComponent().inject(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        binding.submitBtn.setOnClickListener(v -> {
            if (omSubmitTrackerMultipleTask == null) {
                Log.d(TAG, "Sumitting trackers multiple task");
                updateActions(false);
                omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(ActivityLogActivity.this, apiEndpoint);
                omSubmitTrackerMultipleTask.setTrackerServiceListener(ActivityLogActivity.this);
                omSubmitTrackerMultipleTask.execute();
            }
        });

        binding.exportBtn.setOnClickListener(v -> {
            showCompleteExportMessage = true;
            exportActivities();
        });

        binding.exportedFilesList.setLayoutManager(new LinearLayoutManager(this));
        filesAdapter = new ExportedTrackersFileAdapter(files, this);
        binding.exportedFilesList.setAdapter(filesAdapter);
        binding.exportedFilesList.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        binding.archivedFilesList.setLayoutManager(new LinearLayoutManager(this));
        archivedFilesAdapter = new ExportedTrackersFileAdapter(archivedFiles, this, true);
        binding.archivedFilesList.setAdapter(archivedFilesAdapter);
        binding.archivedFilesList.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        updateActions(true);

    }

    private void shareFile(File toShare){
        ExternalResourceOpener.shareFile(this, toShare, "text/json");
    }

    private void exportActivities() {
        //Check the user has permissions to export activity data
        AdminSecurityManager.with(this).checkAdminPermission(R.id.action_export_activity, () -> {
            ExportActivityTask task = new ExportActivityTask(ActivityLogActivity.this);
            task.setListener(ActivityLogActivity.this);
            updateActions(false);
            task.execute(ExportActivityTask.UNEXPORTED_ACTIVITY);
        });
    }

    private void updateActions(boolean enable) {
        if (enable) {
            binding.progressContainer.setVisibility(View.GONE);
            binding.exportActions.setVisibility(View.VISIBLE);
            refreshFileList();
            refreshStats();
        } else {
            binding.progressContainer.setVisibility(View.VISIBLE);
            binding.exportActions.setVisibility(View.GONE);
        }
    }

    private void refreshStats() {

        DbHelper db = DbHelper.getInstance(this);
        int unsentQuizzes = db.getUnsentQuizAttempts().size();
        int unsent = db.getUnsentTrackersCount() + unsentQuizzes;
        int unexported = db.getUnexportedTrackersCount();
        binding.highlightToExport.setText(NumberFormat.getNumberInstance().format(unexported));
        binding.highlightToSubmit.setText(NumberFormat.getNumberInstance().format(unsent));
        binding.highlightSubmitted.setText(NumberFormat.getNumberInstance().format(db.getSentActivityCount()));

        Log.d(TAG, "files " + files.size());
        binding.submitBtn.setEnabled((unsent > 0) || !files.isEmpty());
        binding.exportBtn.setEnabled((unexported > 0));

    }

    private void refreshFileList() {
        files.clear();
        files.addAll(logsRepository.getExportedActivityLogs(this));
        filesAdapter.notifyDataSetChanged();

        archivedFiles.clear();
        archivedFiles.addAll(logsRepository.getArchivedActivityLogs(this));
        archivedFilesAdapter.notifyDataSetChanged();
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

        if (!failures.isEmpty()){
            msg += "\nErrors: \n";
            msg += TextUtils.join("\n", failures);
        }

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        DbHelper db = DbHelper.getInstance(getApplicationContext());
        List<QuizAttempt> unsent = db.getUnsentQuizAttempts();

        if (unsent.isEmpty()) {
            updateActions(true);
        } else {
            SubmitQuizAttemptsTask omSubmitQuizAttemptsTask = new SubmitQuizAttemptsTask(getApplicationContext());
            omSubmitQuizAttemptsTask.setAPIRequestFinishListener(this, "SubmitQuizAttemptsTask");
            omSubmitQuizAttemptsTask.execute(unsent);
        }

        omSubmitTrackerMultipleTask = null;
    }

    @Override
    public void trackerProgressUpdate() {
        // no need to show progress update in this activity
    }

    @Override
    public void onRequestFinish(String nameRequest) {
        if ("SubmitQuizAttemptsTask".equals(nameRequest)){
            updateActions(true);
        }
    }

    @Override
    public void onExportComplete(BasicResult result) {

        if (showCompleteExportMessage) {
            if (result.isSuccess()) {
                UIUtils.showAlert(this,
                        R.string.export_task_completed,
                        getString(R.string.export_task_completed_text, result.getResultMessage())
                );
            } else {
                toast(result.getResultMessage());
            }

        }
        updateActions(true);
    }


    @Override
    public void onItemShareClick(File fileToShare) {
        shareFile(fileToShare);
    }

    @Override
    public void onItemToDelete(final File fileToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle);
        builder.setTitle(R.string.activitylog_delete);
        builder.setMessage(R.string.activitylog_delete_confirm);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
           boolean success = fileToDelete.delete();
           if (!success){
               Toast.makeText(ActivityLogActivity.this,
                       R.string.activitylog_delete_failed, Toast.LENGTH_SHORT).show();
           }
           refreshFileList();
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
}
