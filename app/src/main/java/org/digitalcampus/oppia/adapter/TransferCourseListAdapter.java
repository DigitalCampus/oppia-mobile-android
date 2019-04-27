package org.digitalcampus.oppia.adapter;

import android.support.annotation.NonNull;
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

public class TransferCourseListAdapter extends RecyclerView.Adapter<TransferCourseListAdapter.TclaViewHolder> {

    public static final String TAG = TransferCourseListAdapter.class.getSimpleName();
    private ArrayList<CourseTransferableFile> transferableFiles;
    private ArrayList<CourseTransferableFile> courseFiles = new ArrayList<>();
    private final ListInnerBtnOnClickListener listener;

    public static class TclaViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView courseTitle;
        TextView courseFilesize;
        TextView courseDescription;
        ImageButton actionBtn;
        ImageView icon;

        public ListInnerBtnOnClickListener listener;

        public TclaViewHolder(View v) {
            super(v);
            courseTitle = v.findViewById(R.id.course_title);
            courseFilesize = v.findViewById(R.id.course_filesize);
            courseDescription = v.findViewById(R.id.course_description);
            actionBtn = v.findViewById(R.id.download_course_btn);
            icon = v.findViewById(R.id.elem_icon);

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

    private void filterCourses(){
        courseFiles.clear();
        for (CourseTransferableFile file : transferableFiles){
            if (CourseTransferableFile.TYPE_COURSE_BACKUP.equals(file.getType())){
                courseFiles.add(file);
            }
        }
        for (CourseTransferableFile course : courseFiles){
            long relatedSize = 0;
            for (CourseTransferableFile file : transferableFiles){
                if (course.getRelatedMedia().contains(file.getFilename())){
                    relatedSize += file.getFileSize();
                }
            }
            course.setRelatedFilesize(relatedSize);
        }

    }


    public TransferCourseListAdapter(ArrayList<CourseTransferableFile> files, ListInnerBtnOnClickListener listener){
        this.transferableFiles = files;
        this.listener = listener;
        filterCourses();

        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                filterCourses();
                super.onChanged();
            }
        });
    }

    @NonNull
    @Override
    public TclaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_transfer_row, parent, false);
        return new TclaViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull TclaViewHolder holder, int position) {
        CourseTransferableFile current = courseFiles.get(position);

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

        holder.courseFilesize.setText( current.getDisplayFileSize() );
        if (listener!=null){
            holder.listener = this.listener;
        }
    }

    @Override
    public int getItemCount() {
        return courseFiles.size();
    }


}
