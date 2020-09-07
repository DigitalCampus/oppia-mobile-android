package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public class DownloadCoursesAdapter extends MultiChoiceRecyclerViewAdapter<DownloadCoursesAdapter.DownloadCoursesViewHolder> {

    private List<CourseInstallViewAdapter> courses;
    private Context context;
    private OnItemClickListener itemClickListener;
    private String prefLang;

    private String updateDescription;
    private String installDescription;
    private String installedDescription;
    private String cancelDescription;


    public DownloadCoursesAdapter(Context context, List<CourseInstallViewAdapter> courses) {
        this.context = context;
        this.courses = courses;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        updateDescription = context.getString(R.string.update);
        installDescription = context.getString(R.string.install);
        installedDescription = context.getString(R.string.installed);
        cancelDescription = context.getString(R.string.cancel);
    }

    @Override
    public DownloadCoursesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_course_download, parent, false);
        return new DownloadCoursesViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(@NonNull final DownloadCoursesViewHolder viewHolder, final int position) {

        updateViewHolder(viewHolder, position);
        final CourseInstallViewAdapter c = getItemAtPosition(position);

        viewHolder.courseTitle.setText(c.getTitle(prefLang));
        viewHolder.actionBtn.setVisibility(isMultiChoiceMode ? View.INVISIBLE : View.VISIBLE);

        if (c.isDraft()){
            viewHolder.courseDraft.setVisibility(View.VISIBLE);
            viewHolder.courseDraft.setText(context.getString(R.string.course_draft));
        } else {
            viewHolder.courseDraft.setVisibility(View.GONE);
        }

        String desc = c.getDescription(prefLang);
        if (desc != null){
            viewHolder.courseDescription.setVisibility(View.VISIBLE);
            viewHolder.courseDescription.setText(desc);
        } else {
            viewHolder.courseDescription.setVisibility(View.GONE);
        }

        String organisation = c.getOrganisationName();
        if (!TextUtils.isEmpty(organisation) && !(c.isDownloading() || c.isInstalling())){
            viewHolder.labelAuthor.setVisibility(View.VISIBLE);
            viewHolder.courseAuthor.setVisibility(View.VISIBLE);
            viewHolder.courseAuthor.setText(organisation);
        }
        else{
            viewHolder.labelAuthor.setVisibility(View.GONE);
            viewHolder.courseAuthor.setVisibility(View.GONE);
        }

        if (c.isDownloading() || c.isInstalling()){
            viewHolder.actionBtn.setImageResource(R.drawable.ic_action_cancel);
            viewHolder.actionBtn.setContentDescription(cancelDescription);
            viewHolder.actionBtn.setEnabled(!c.isInstalling());

            viewHolder.actionProgress.setVisibility(View.VISIBLE);
            if (c.getProgress()>0){
                viewHolder.actionProgress.setIndeterminate(false);
                viewHolder.actionProgress.setProgress(c.getProgress());
            }
            else {
                viewHolder.actionProgress.setIndeterminate(true);
            }
        }
        else{
            viewHolder.actionProgress.setVisibility(View.GONE);
            if(c.isInstalled()){
                if(c.isToUpdate()){
                    viewHolder.actionBtn.setImageResource(R.drawable.ic_action_refresh);
                    viewHolder.actionBtn.setContentDescription(updateDescription);
                    viewHolder.actionBtn.setEnabled(true);
                } else {
                    viewHolder.actionBtn.setImageResource(R.drawable.ic_action_accept);
                    viewHolder.actionBtn.setContentDescription(installedDescription);
                    viewHolder.actionBtn.setEnabled(false);
                    viewHolder.actionBtn.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.actionBtn.setImageResource(R.drawable.ic_action_download);
                viewHolder.actionBtn.setContentDescription(installDescription);
                viewHolder.actionBtn.setEnabled(true);
            }
        }

        viewHolder.actionBtn.setTag(position); //For passing the list item index
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public CourseInstallViewAdapter getItemAtPosition(int position) {
        return courses.get(position);
    }


    public class DownloadCoursesViewHolder extends MultiChoiceRecyclerViewAdapter.ViewHolder {

        private TextView courseTitle;
        private TextView courseDraft;
        private TextView courseDescription;
        private ImageButton actionBtn;
        private ProgressBar actionProgress;
        private TextView courseAuthor;
        private TextView labelAuthor;

        public DownloadCoursesViewHolder(View itemView) {

            super(itemView);

            courseTitle = itemView.findViewById(R.id.course_title);
            courseDraft = itemView.findViewById(R.id.course_draft);
            courseDescription = itemView.findViewById(R.id.course_description);
            actionBtn = itemView.findViewById(R.id.download_course_btn);
            actionProgress = itemView.findViewById(R.id.download_progress);
            courseAuthor = itemView.findViewById(R.id.course_author);
            labelAuthor = itemView.findViewById(R.id.label_author);

            actionBtn.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onDownloadButtonClick(v, getAdapterPosition());
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onDownloadButtonClick(View view, int position);
    }
}
 

