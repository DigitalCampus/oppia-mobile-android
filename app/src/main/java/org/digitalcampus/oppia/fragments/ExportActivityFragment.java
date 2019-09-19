/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ExportActivityFragment extends AppFragment implements TrackerServiceListener, ExportActivityListener {

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

    public static ExportActivityFragment newInstance() {
        return new ExportActivityFragment();
    }

    public ExportActivityFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_export_activity, container, false);

        exportedFilesRecyclerView = vv.findViewById(R.id.exported_files_list);
        exportBtn = vv.findViewById(R.id.export_btn);
        submitBtn = vv.findViewById(R.id.submit_btn);
        progressContainer = vv.findViewById(R.id.progress_container);
        actionsContainer = vv.findViewById(R.id.export_actions);

        unsentTrackers = vv.findViewById(R.id.highlight_to_submit);
        unexportedTrackers = vv.findViewById(R.id.highlight_to_export);
        submittedTrackers = vv.findViewById(R.id.highlight_submitted);

        return vv;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity parent = ExportActivityFragment.this.getActivity();
//                MobileLearning app = (MobileLearning) parent.getApplication();
                if(omSubmitTrackerMultipleTask == null){
                    Log.d(TAG,"Sumitting trackers multiple task");
                    updateActions(false);
                    omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(parent);
                    omSubmitTrackerMultipleTask.setTrackerServiceListener(ExportActivityFragment.this);
                    omSubmitTrackerMultipleTask.execute();
                }
            }
        });

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check the user has permissions to export activity data
                AdminSecurityManager.checkAdminPermission(getActivity(), R.id.action_export_activity, new AdminSecurityManager.AuthListener() {
                    public void onPermissionGranted() {
                        ExportActivityTask task = new ExportActivityTask(ExportActivityFragment.this.getActivity());
                        task.setListener(ExportActivityFragment.this);
                        updateActions(false);
                        task.execute();
                    }
                });

            }
        });

        exportedFilesRecyclerView.setHasFixedSize(true);
        exportedFilesRecyclerView.setLayoutManager( new LinearLayoutManager(this.getContext()));
        filesAdapter = new ExportedTrackersFileAdapter(files, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                Context ctx = ExportActivityFragment.this.getActivity();
                File toShare = files.get(position);
                Intent share = ExternalResourceOpener.constructShareFileIntent(ctx, toShare);
                ctx.startActivity(share);
            }
        });
        exportedFilesRecyclerView.setAdapter(filesAdapter);
        exportedFilesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));

        updateActions(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if( (getView() != null) && isVisibleToUser){
            // Your fragment is visible
            refreshFileList();
            refreshStats();

        }
    }

    private void updateActions(boolean enable){
        if (enable){
            progressContainer.setVisibility(View.GONE);
            actionsContainer.setVisibility(View.VISIBLE);
            refreshFileList();
            refreshStats();
        }
        else{
            progressContainer.setVisibility(View.VISIBLE);
            actionsContainer.setVisibility(View.GONE);
        }
    }

    private void refreshStats() {

        DbHelper db = DbHelper.getInstance(this.getActivity());
        int unsent = db.getUnsentTrackersCount();
        int unexported = db.getUnexportedTrackersCount();
        unexportedTrackers.setText(NumberFormat.getNumberInstance().format(unexported));
        unsentTrackers.setText(NumberFormat.getNumberInstance().format(unsent));
        submittedTrackers.setText(NumberFormat.getNumberInstance().format(db.getSentTrackersCount()));

        Log.d(TAG, "files " + files.size());
        submitBtn.setEnabled( (unsent > 0) || files.size()>0 );
        exportBtn.setEnabled( (unexported > 0) );

    }

    private void refreshFileList(){
        File activityFolder = new File(Storage.getActivityPath(this.getContext()));
        if (activityFolder.exists()){
            files.clear();
            String[] children = activityFolder.list();
            for (String dirFiles : children) {
                File exportedActivity = new File(activityFolder, dirFiles);
                files.add(exportedActivity);
            }
        }
        filesAdapter.notifyDataSetChanged();

    }

    @Override
    public void trackerComplete(boolean success, String message) {

        if (message != null && message.length()>0){
            Toast.makeText(getContext(),
                    message,
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getContext(),
                    success ? R.string.submit_trackers_success : R.string.error_connection,
                    Toast.LENGTH_LONG).show();
        }
        updateActions(true);
    }

    @Override
    public void trackerProgressUpdate() {
        // no need to show progress update in this activity
    }

    @Override
    public void onExportComplete(String filename) {
        if (filename != null){
            UIUtils.showAlert(getActivity(),
                    R.string.export_task_completed,
                    getString(R.string.export_task_completed_text, filename)
            );
        }
        else{
            Toast.makeText(getContext(),
                    R.string.export_task_no_activities,
                    Toast.LENGTH_LONG).show();
        }
        updateActions(true);
    }
}
