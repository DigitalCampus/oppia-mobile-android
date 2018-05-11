package org.digitalcampus.oppia.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;

import java.io.File;
import java.util.ArrayList;

public class ExportedTrackersFileAdapter extends RecyclerView.Adapter<ExportedTrackersFileAdapter.ViewHolder> {

    private ArrayList<File> fileList;
    private final ListInnerBtnOnClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView file_name;
        TextView file_size;
        ImageButton btn_share;
        public ListInnerBtnOnClickListener listener;

        public ViewHolder(View v) {
            super(v);
            file_name = (TextView) v.findViewById(R.id.file_name);
            file_size = (TextView) v.findViewById(R.id.file_size);
            btn_share = (ImageButton) v.findViewById(R.id.share_btn);

            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }


    public ExportedTrackersFileAdapter(ArrayList<File> fileList, ListInnerBtnOnClickListener listener){
        this.fileList = fileList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exported_activity_item, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File current = fileList.get(position);
        holder.file_name.setText(current.getName());
        holder.file_size.setText( org.apache.commons.io.FileUtils.byteCountToDisplaySize(current.length()));
        if (listener!=null){
            holder.listener = this.listener;
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


}
