package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowMediaDownloadBinding;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public class DownloadMediaAdapter extends MultiChoiceRecyclerViewAdapter<DownloadMediaAdapter.DownloadMediaViewHolder> {


    private final SharedPreferences prefs;
    private List<Media> mediaList;
    private final Context context;
    private ListInnerBtnOnClickListener itemClickListener;

    public DownloadMediaAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public DownloadMediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_media_download, parent, false);
        return new DownloadMediaViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(@NonNull final DownloadMediaViewHolder viewHolder, final int position) {

        updateViewHolder(viewHolder, position);
        final Media m = getItemAtPosition(position);

        StringBuilder courses =  new StringBuilder();
        for (int i = 0; i < m.getCourses().size(); i++) {
            Course c = m.getCourses().get(i);
            String title = c.getTitle(prefs);
            courses.append( i != 0 ? ", " + title : title);
        }

        viewHolder.binding.mediaCourses.setText(courses.toString());
        viewHolder.binding.mediaTitle.setText(m.getFilename());
        viewHolder.binding.mediaPath.setText(m.getDownloadUrl());
        if (m.getFileSize() != 0) {
            viewHolder.binding.mediaFileSize.setText(context.getString(R.string.media_file_size, m.getFileSize() / (1024 * 1024)));
            viewHolder.binding.mediaFileSize.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.mediaFileSize.setVisibility(View.INVISIBLE);
        }

        viewHolder.binding.actionBtn.setVisibility(isMultiChoiceMode ? View.INVISIBLE : View.VISIBLE);

        int actionBtnimagRes;
        if (m.isDownloading()) {
            actionBtnimagRes = R.drawable.ic_action_cancel;

            viewHolder.binding.downloadProgress.setVisibility(View.VISIBLE);
            viewHolder.binding.mediaPath.setVisibility(View.GONE);
            viewHolder.binding.downloadError.setVisibility(View.GONE);
            if (m.getProgress() > 0) {
                viewHolder.binding.downloadProgress.setIndeterminate(false);
                viewHolder.binding.downloadProgress.setProgress(m.getProgress());
            } else {
                viewHolder.binding.downloadProgress.setIndeterminate(true);
            }
        } else {
            actionBtnimagRes = m.hasFailed() ? R.drawable.dialog_ic_action_update : R.drawable.ic_action_download;
            viewHolder.binding.downloadError.setVisibility(m.hasFailed() ? View.VISIBLE : View.GONE);
            viewHolder.binding.downloadProgress.setVisibility(View.GONE);
            viewHolder.binding.mediaPath.setVisibility(View.VISIBLE);
        }
        viewHolder.binding.actionBtn.setImageResource(actionBtnimagRes);
        viewHolder.binding.actionBtn.setTag(actionBtnimagRes);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public Media getItemAtPosition(int position) {
        return mediaList.get(position);
    }

    public void sortByCourse() {
        //Sort the media list by filename
        SharedPreferences prefsCourse = PreferenceManager.getDefaultSharedPreferences(context);
        final String lang = prefsCourse.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        Collections.sort(this.mediaList, (Comparator<Object>) (o1, o2) -> {
            String titleCourse1 = ((Media) o1).getCourses().get(0).getTitle(lang);
            String titleCourse2 = ((Media) o2).getCourses().get(0).getTitle(lang);
            return (titleCourse1.compareTo(titleCourse2));
        });

        notifyDataSetChanged();
    }

    public void sortByFilename() {
        //Sort the media list by filename
        Collections.sort(this.mediaList, (Comparator<Object>) (o1, o2) -> ((Media) o1).getFilename().compareTo(((Media) o2).getFilename()));
        notifyDataSetChanged();
    }


    public class DownloadMediaViewHolder extends MultiChoiceRecyclerViewAdapter.ViewHolder {

        private final RowMediaDownloadBinding binding;

        public DownloadMediaViewHolder(View itemView) {

            super(itemView);
            binding = RowMediaDownloadBinding.bind(itemView);

            binding.actionBtn.setOnClickListener(v -> {
                if (itemClickListener != null)
                    itemClickListener.onClick(getAdapterPosition());
            });
        }
    }

    public void setOnItemClickListener(ListInnerBtnOnClickListener listener) {
        this.itemClickListener = listener;
    }
}


