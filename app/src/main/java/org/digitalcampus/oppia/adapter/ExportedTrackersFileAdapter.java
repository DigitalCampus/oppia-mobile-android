package org.digitalcampus.oppia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowActivitylogBinding;
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
        
        private final RowActivitylogBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = RowActivitylogBinding.bind(v);

            binding.shareBtn.setOnClickListener(v1 -> {
                if (listener != null){
                    listener.onItemShareClick(fileList.get(getAdapterPosition()));
                }
            });

            if (showDeleteButton){
                binding.deleteBtn.setOnClickListener(view -> {
                    if (listener != null){
                        listener.onItemToDelete(fileList.get(getAdapterPosition()));
                    }
                });
            }
            else{
                binding.deleteBtn.setVisibility(View.GONE);
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

        holder.binding.fileName.setText(username);
        holder.binding.fileSize.setText( org.apache.commons.io.FileUtils.byteCountToDisplaySize(current.length()));
        holder.binding.fileDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


}
