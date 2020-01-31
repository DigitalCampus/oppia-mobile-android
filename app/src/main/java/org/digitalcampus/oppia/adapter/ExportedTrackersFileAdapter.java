package org.digitalcampus.oppia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;

import java.io.File;
import java.util.List;

public class ExportedTrackersFileAdapter extends RecyclerView.Adapter<ExportedTrackersFileAdapter.EtfaViewHolder> {

    public static final String TAG = ExportedTrackersFileAdapter.class.getSimpleName();
    private List<File> fileList;
    private ListInnerBtnOnClickListener listener;

    public class EtfaViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView fileName;
        private TextView fileSize;
        private ImageButton btnShare;

        public EtfaViewHolder(View v) {
            super(v);
            fileName = v.findViewById(R.id.file_name);
            fileSize = v.findViewById(R.id.file_size);
            btnShare = v.findViewById(R.id.share_btn);

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }


    public ExportedTrackersFileAdapter(List<File> fileList, ListInnerBtnOnClickListener listener){
        this.fileList = fileList;
        this.listener = listener;
    }

    @Override
    public EtfaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_activitylog, parent, false);
        return new EtfaViewHolder(v);

    }

    @Override
    public void onBindViewHolder(EtfaViewHolder holder, int position) {
        File current = fileList.get(position);
        holder.fileName.setText(current.getName());
        holder.fileSize.setText( org.apache.commons.io.FileUtils.byteCountToDisplaySize(current.length()));

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


}
