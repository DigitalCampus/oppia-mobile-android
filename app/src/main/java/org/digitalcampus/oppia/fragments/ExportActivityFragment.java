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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ExportActivityFragment extends Fragment {

    public static final String TAG = ExportActivityFragment.class.getSimpleName();

    private FloatingActionButton exportBtn;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
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

        mRecyclerView = (RecyclerView) vv.findViewById(R.id.exported_files_list);
        exportBtn = (FloatingActionButton) vv.findViewById(R.id.export_btn);
        return vv;

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExportActivityTask task = new ExportActivityTask(ExportActivityFragment.this.getActivity());
                task.setListener(new ExportActivityListener() {
                    @Override
                    public void onExportComplete(String filename) {
                        if (filename != null){
                            UIUtils.showAlert(ExportActivityFragment.this.getActivity(),
                                    R.string.export_task_completed,
                                    ExportActivityFragment.this.getString(R.string.export_task_completed_text, filename)
                                    );
                            refreshFileList();
                        }

                    }
                });
                task.execute();
            }
        });



        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ExportedTrackersFileAdapter(files);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        refreshFileList();
    }

    private void refreshFileList(){
        File activityFolder = new File(Storage.getActivityPath(this.getContext()));
        files.clear();
        if (activityFolder.exists()){
            String[] children = activityFolder.list();
            for (String dirFiles : children) {
                File exportedActivity = new File(activityFolder, dirFiles);
                files.add(exportedActivity);
            }
        }
        mAdapter.notifyDataSetChanged();

    }

}
