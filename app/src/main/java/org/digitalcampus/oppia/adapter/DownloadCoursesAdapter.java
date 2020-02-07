package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import java.util.List;
import java.util.Locale;

public class DownloadCoursesAdapter extends RecyclerView.Adapter<DownloadCoursesAdapter.ViewHolder> {


    private List<CourseIntallViewAdapter> courses;
    private Context context;
    private OnItemClickListener itemClickListener;
    private String prefLang;

    private String updateDescription;
    private String updateSchedDescription;
    private String installDescription;
    private String installedDescription;
    private String cancelDescription;


    public DownloadCoursesAdapter(Context context, List<CourseIntallViewAdapter> courses) {
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_course_download, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final CourseIntallViewAdapter c = getItemAtPosition(position);

        viewHolder.courseTitle.setText(c.getTitle(prefLang));

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

    public CourseIntallViewAdapter getItemAtPosition(int position) {
        return courses.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        private TextView courseTitle;
        private TextView courseDraft;
        private TextView courseDescription;
        private ImageButton actionBtn;
        private ProgressBar actionProgress;
        private TextView courseAuthor;
        private TextView labelAuthor;

        public ViewHolder(View itemView) {

            super(itemView);

            courseTitle = itemView.findViewById(R.id.course_title);
            courseDraft = itemView.findViewById(R.id.course_draft);
            courseDescription = itemView.findViewById(R.id.course_description);
            actionBtn = itemView.findViewById(R.id.download_course_btn);
            actionProgress = itemView.findViewById(R.id.download_progress);
            courseAuthor = itemView.findViewById(R.id.course_author);
            labelAuthor = itemView.findViewById(R.id.label_author);
            rootView = itemView;

            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onDownloadButtonClick(v, getAdapterPosition());
                    }
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onDownloadButtonClick(View view, int position);
    }
}
 

