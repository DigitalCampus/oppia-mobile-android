package org.digitalcampus.oppia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExportedTrackersFileAdapter extends RecyclerView.Adapter<ExportedTrackersFileAdapter.ViewHolder> {

    public static final String TAG = ExportedTrackersFileAdapter.class.getSimpleName();
    private List<File> fileList;
    private OnItemClickListener listener;
    private boolean showDeleteButton;

    public interface OnItemClickListener {
        void onItemShareClick(File fileToShare);
        void onItemToDelete(File fileToDelete);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView fileName;
        private TextView fileSize;
        private TextView fileDate;

        public ViewHolder(View v) {
            super(v);
            fileName = v.findViewById(R.id.file_name);
            fileSize = v.findViewById(R.id.file_size);
            fileDate = v.findViewById(R.id.file_date);

            ImageButton btnShare = v.findViewById(R.id.share_btn);
            ImageButton btnDelete = v.findViewById(R.id.delete_btn);

            btnShare.setOnClickListener(v1 -> {
                if (listener != null){
                    listener.onItemShareClick(fileList.get(getAdapterPosition()));
                }
            });
            if (showDeleteButton){
                btnDelete.setOnClickListener(view -> {
                    if (listener != null){
                        listener.onItemToDelete(fileList.get(getAdapterPosition()));
                    }
                });
            }
            else{
                btnDelete.setVisibility(View.GONE);
            }

        }
    }


    public ExportedTrackersFileAdapter(List<File> fileList, OnItemClickListener listener){
        this(fileList, listener, false);
    }

    public ExportedTrackersFileAdapter(List<File> fileList, OnItemClickListener listener, boolean showDeleteButton){
        this.fileList = fileList;
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_activitylog, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File current = fileList.get(position);
        String filename = current.getName();
        String username = filename.substring(0, filename.indexOf('_'));
        if (username.equals("activity")){
            username = "Multiple users";
        }
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime dateTime = f.parseDateTime(filename.substring(filename.lastIndexOf('_')+1, filename.lastIndexOf('.')));
        String date = DateUtils.DISPLAY_DATETIME_FORMAT.print(dateTime);

        holder.fileName.setText(username);
        holder.fileSize.setText( org.apache.commons.io.FileUtils.byteCountToDisplaySize(current.length()));
        holder.fileDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


}
