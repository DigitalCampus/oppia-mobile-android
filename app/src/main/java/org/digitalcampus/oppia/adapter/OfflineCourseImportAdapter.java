package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.OnRemoveButtonClickListener;
import org.digitalcampus.oppia.model.OfflineCourseFile;

import java.util.ArrayList;

public class OfflineCourseImportAdapter extends RecyclerView.Adapter<OfflineCourseImportAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<OfflineCourseFile> fileList;
    private OnRemoveButtonClickListener listener;

    public OfflineCourseImportAdapter(Context context, ArrayList<OfflineCourseFile> fileList, OnRemoveButtonClickListener onRemoveButtonClickListener) {
        this.context = context;
        this.fileList = fileList;
        this.listener = onRemoveButtonClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fileTitle;
        public TextView fileType;
        private ProgressBar progressBar;
        public ImageButton rowBtn;
        private boolean importing = false;

        public ViewHolder(View itemView) {
            super(itemView);
            fileTitle = itemView.findViewById(R.id.file_title);
            fileType = itemView.findViewById(R.id.type_badge);
            progressBar = itemView.findViewById(R.id.import_progress);
            rowBtn = itemView.findViewById(R.id.row_btn);
            rowBtn.setOnClickListener(v -> {
                if(!importing) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRemoveButtonClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_offline_course_import_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OfflineCourseFile fileItem = fileList.get(position);
        holder.fileTitle.setText(fileItem.getFile().getName());
        holder.fileType.setText(fileItem.getType().toString());

        int badgeColor = getColorByFileType(fileItem.getType());
        GradientDrawable badgeDrawable = (GradientDrawable) holder.fileType.getBackground();
        badgeDrawable.setTint(badgeColor);
        holder.fileType.setBackground(badgeDrawable);

        int iconResource = getIconByFileStatus(fileItem.getStatus());
        holder.rowBtn.setImageResource(iconResource);

        if (fileItem.getStatus() == OfflineCourseFile.Status.IMPORTING) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.rowBtn.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.rowBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private int getColorByFileType(OfflineCourseFile.FileType fileType) {
        int colorResId = R.color.grey_dark;
        switch (fileType) {
            case COURSE:
                colorResId = R.color.bg_badge_status_read_only;
                break;
            case MEDIA:
                colorResId = R.color.bg_badge_status_draft;
                break;
        }
        return ContextCompat.getColor(context, colorResId);
    }

    private int getIconByFileStatus(OfflineCourseFile.Status status) {
        int iconResId = R.drawable.ic_action_cancel;
        if (status == OfflineCourseFile.Status.IMPORTED) {
            iconResId = R.drawable.ic_action_accept;
        }
        return iconResId;
    }
}