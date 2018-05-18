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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;

public class ExportActivityFragment extends Fragment implements TrackerServiceListener, ExportActivityListener {

    public static final String TAG = ExportActivityFragment.class.getSimpleName();

    private Button exportBtn;
    private Button submitBtn;
    private View progressContainer;
    private View actionsContainer;

    private RecyclerView exportedFilesRecyclerView;
    private RecyclerView.Adapter filesAdapter;
    private ArrayList<File> files = new ArrayList<>();

    public static ExportActivityFragment newInstance() {
        return new ExportActivityFragment();
    }

    public ExportActivityFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_export_activity, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        vv.setLayoutParams(lp);

        exportedFilesRecyclerView = (RecyclerView) vv.findViewById(R.id.exported_files_list);
        exportBtn = (Button) vv.findViewById(R.id.export_btn);
        submitBtn = (Button) vv.findViewById(R.id.submit_btn);
        progressContainer = vv.findViewById(R.id.progress_container);
        actionsContainer = vv.findViewById(R.id.export_actions);
        return vv;

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity parent = ExportActivityFragment.this.getActivity();
                MobileLearning app = (MobileLearning) parent.getApplication();
                if(app.omSubmitTrackerMultipleTask == null){
                    Log.d(TAG,"Sumitting trackers multiple task");
                    updateActions(false);
                    app.omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(parent);
                    app.omSubmitTrackerMultipleTask.setTrackerServiceListener(ExportActivityFragment.this);
                    app.omSubmitTrackerMultipleTask.execute();
                }
            }
        });

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExportActivityTask task = new ExportActivityTask(ExportActivityFragment.this.getActivity());
                task.setListener(ExportActivityFragment.this);
                updateActions(false);
                task.execute();
            }
        });

        exportedFilesRecyclerView.setHasFixedSize(true);
        exportedFilesRecyclerView.setLayoutManager( new LinearLayoutManager(this.getContext()));
        filesAdapter = new ExportedTrackersFileAdapter(files, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                Context ctx = ExportActivityFragment.this.getActivity().getApplicationContext();
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

    private void updateActions(boolean enable){
        if (enable){
            progressContainer.setVisibility(View.GONE);
            actionsContainer.setVisibility(View.VISIBLE);
            refreshStats();
            refreshFileList();
        }
        else{
            progressContainer.setVisibility(View.VISIBLE);
            actionsContainer.setVisibility(View.GONE);
        }
    }

    private void refreshStats() {

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
    public void trackerComplete() {
        updateActions(true);
    }

    @Override
    public void trackerProgressUpdate() {

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
