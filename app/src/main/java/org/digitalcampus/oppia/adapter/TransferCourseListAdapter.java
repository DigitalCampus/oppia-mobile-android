package org.digitalcampus.oppia.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseTransferableFile;

import java.util.ArrayList;

public class TransferCourseListAdapter extends RecyclerView.Adapter<TransferCourseListAdapter.ViewHolder> {

    private ArrayList<CourseTransferableFile> courses;
    private final ListInnerBtnOnClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView courseTitle;
        TextView courseFilesize;
        TextView courseDescription;
        ImageButton actionBtn;
        ImageView icon;

        public ListInnerBtnOnClickListener listener;

        public ViewHolder(View v) {
            super(v);
            courseTitle = (TextView) v.findViewById(R.id.course_title);
            courseFilesize = (TextView) v.findViewById(R.id.course_filesize);
            courseDescription = (TextView) v.findViewById(R.id.course_description);
            actionBtn = (ImageButton) v.findViewById(R.id.download_course_btn);
            icon = (ImageView) v.findViewById(R.id.elem_icon);

            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }


    public TransferCourseListAdapter(ArrayList<CourseTransferableFile> courses, ListInnerBtnOnClickListener listener){
        this.courses = courses;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_transfer_row, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CourseTransferableFile current = courses.get(position);

        if (current.getTitle() != null){
            holder.courseTitle.setVisibility(View.VISIBLE);
            holder.courseTitle.setText(current.getTitle());
        }
        else{
            holder.courseTitle.setVisibility(View.GONE);
        }

        holder.courseDescription.setText(current.getFilename());
        holder.icon.setImageResource(
                current.getType().equals(CourseTransferableFile.TYPE_COURSE_BACKUP)?
                        R.drawable.ic_notification : R.drawable.default_icon_video);

        holder.courseFilesize.setText( org.apache.commons.io.FileUtils.byteCountToDisplaySize(current.getFileSize()));
        if (listener!=null){
            holder.listener = this.listener;
        }
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }


}
