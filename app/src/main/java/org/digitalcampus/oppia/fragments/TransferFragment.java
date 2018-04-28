package org.digitalcampus.oppia.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter;
import org.digitalcampus.oppia.adapter.TransferCourseListAdapter;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseBackup;
import org.digitalcampus.oppia.task.FetchCourseBackupsTask;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends Fragment {

    public static final String TAG = TransferFragment.class.getSimpleName();

    private RecyclerView coursesRecyclerView;
    private RecyclerView.Adapter coursesAdapter;
    private ArrayList<CourseBackup> courses = new ArrayList<>();

    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vv = inflater.inflate(R.layout.fragment_transfer, container, false);
        coursesRecyclerView = (RecyclerView) vv.findViewById(R.id.course_backups_list);
        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        coursesRecyclerView.setHasFixedSize(true);
        coursesRecyclerView.setLayoutManager( new LinearLayoutManager(this.getContext()));
        coursesAdapter = new TransferCourseListAdapter(courses, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                Context ctx = TransferFragment.this.getActivity().getApplicationContext();
                CourseBackup toShare = courses.get(position);
            }
        });
        coursesRecyclerView.setAdapter(coursesAdapter);
        coursesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        refreshFileList();
    }


    private void refreshFileList(){

        FetchCourseBackupsTask task = new FetchCourseBackupsTask(this.getActivity());
        task.setListener(new FetchCourseBackupsTask.FetchBackupsListener() {
            @Override
            public void onFetchComplete(List<CourseBackup> backups) {
                courses.clear();
                courses.addAll(backups);
                coursesAdapter.notifyDataSetChanged();
            }
        });
        task.execute();

    }


}
