package org.digitalcampus.oppia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseTransferableFile;

import java.util.ArrayList;
import java.util.List;

public class TransferableFileListAdapter extends RecyclerView.Adapter<TransferableFileListAdapter.TclaViewHolder> {

    public static final String TAG = TransferableFileListAdapter.class.getSimpleName();
    private List<CourseTransferableFile> transferableFiles;
    private List<CourseTransferableFile> courseFiles = new ArrayList<>();
    private final ListInnerBtnOnClickListener listener;

    public class TclaViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView fileTitle;
        private TextView fileSubtitle;
        private TextView fileAside;
        private ImageButton actionBtn;
        private ImageView icon;

        public TclaViewHolder(View v) {
            super(v);
            fileTitle = v.findViewById(R.id.file_title);
            fileSubtitle = v.findViewById(R.id.file_subtitle);
            fileAside = v.findViewById(R.id.file_aside);
            actionBtn = v.findViewById(R.id.download_course_btn);
            icon = v.findViewById(R.id.elem_icon);

            actionBtn.setOnClickListener(v1 -> {
                if (listener != null){
                    listener.onClick(getAdapterPosition());
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


    public TransferableFileListAdapter(List<CourseTransferableFile> files, ListInnerBtnOnClickListener listener){
        this(files, listener, false);
    }

    public TransferableFileListAdapter(List<CourseTransferableFile> files, ListInnerBtnOnClickListener listener, boolean filterCourses){

        this.listener = listener;

        if (filterCourses){
            this.transferableFiles = files;
            filterCourses();

            this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    filterCourses();
                    super.onChanged();
                }
            });
        }
        else{
            this.courseFiles = files;
        }

    }

    @NonNull
    @Override
    public TclaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_transferable_file, parent, false);
        return new TclaViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull TclaViewHolder holder, int position) {
        CourseTransferableFile current = courseFiles.get(position);

        if (current.getTitle() != null){
            holder.fileTitle.setVisibility(View.VISIBLE);
            holder.fileTitle.setText(current.getTitle());
        }
        else{
            holder.fileTitle.setVisibility(View.GONE);
        }

        if (current.getType().equals(CourseTransferableFile.TYPE_ACTIVITY_LOG)){
            holder.fileSubtitle.setText(current.getDisplayDateTimeFromFilename());
            holder.fileAside.setVisibility(View.VISIBLE);
            holder.fileAside.setText(current.getDisplayFileSize());
            holder.icon.setImageResource(R.drawable.ic_file_account);
        }
        else{

            holder.icon.setImageResource(
                    current.getType().equals(CourseTransferableFile.TYPE_COURSE_BACKUP)?
                            R.drawable.ic_notification : R.drawable.default_icon_video);
            holder.fileSubtitle.setText( current.getDisplayFileSize() );
            holder.fileAside.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return courseFiles.size();
    }


}
