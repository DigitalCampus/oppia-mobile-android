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
import org.digitalcampus.mobile.learning.databinding.RowCourseDownloadBinding;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.CourseInstallViewAdapter;

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

        viewHolder.binding.courseTitle.setText(c.getTitle(prefLang));
        viewHolder.binding.downloadCourseBtn.setVisibility(isMultiChoiceMode ? View.INVISIBLE : View.VISIBLE);

        if (c.isDraft()){
            viewHolder.binding.courseDraft.setVisibility(View.VISIBLE);
            viewHolder.binding.courseDraft.setText(context.getString(R.string.course_draft));
        } else {
            viewHolder.binding.courseDraft.setVisibility(View.GONE);
        }

        String desc = c.getDescription(prefLang);
        if (desc != null){
            viewHolder.binding.courseDescription.setVisibility(View.VISIBLE);
            viewHolder.binding.courseDescription.setText(desc);
        } else {
            viewHolder.binding.courseDescription.setVisibility(View.GONE);
        }

        String organisation = c.getOrganisationName();
        if (!TextUtils.isEmpty(organisation) && !(c.isDownloading() || c.isInstalling())){
            viewHolder.binding.labelAuthor.setVisibility(View.VISIBLE);
            viewHolder.binding.courseAuthor.setVisibility(View.VISIBLE);
            viewHolder.binding.courseAuthor.setText(organisation);
        }
        else{
            viewHolder.binding.labelAuthor.setVisibility(View.GONE);
            viewHolder.binding.courseAuthor.setVisibility(View.GONE);
        }


        int actionBtnImageRes;
        if (c.isDownloading() || c.isInstalling()){
            actionBtnImageRes =  R.drawable.ic_action_cancel;
            viewHolder.binding.downloadCourseBtn.setContentDescription(cancelDescription);
            viewHolder.binding.downloadCourseBtn.setEnabled(!c.isInstalling());

            viewHolder.binding.downloadProgress.setVisibility(View.VISIBLE);
            if (c.getProgress()>0){
                viewHolder.binding.downloadProgress.setIndeterminate(false);
                viewHolder.binding.downloadProgress.setProgress(c.getProgress());
            }
            else {
                viewHolder.binding.downloadProgress.setIndeterminate(true);
            }
        }
        else{
            viewHolder.binding.downloadProgress.setVisibility(View.GONE);

            if(c.isInstalled()){
                if(c.isToUpdate()){
                    actionBtnImageRes = R.drawable.ic_action_refresh;
                    viewHolder.binding.downloadCourseBtn.setContentDescription(updateDescription);
                    viewHolder.binding.downloadCourseBtn.setEnabled(true);
                } else {
                    actionBtnImageRes = R.drawable.ic_action_accept;
                    viewHolder.binding.downloadCourseBtn.setContentDescription(installedDescription);
                    viewHolder.binding.downloadCourseBtn.setEnabled(false);
                    viewHolder.binding.downloadCourseBtn.setVisibility(View.VISIBLE);
                }
            } else {
                actionBtnImageRes = R.drawable.ic_action_download;
                viewHolder.binding.downloadCourseBtn.setContentDescription(installDescription);
                viewHolder.binding.downloadCourseBtn.setEnabled(true);
            }
        }

        viewHolder.binding.downloadCourseBtn.setImageResource(actionBtnImageRes);
        viewHolder.binding.downloadCourseBtn.setTag(actionBtnImageRes);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public CourseInstallViewAdapter getItemAtPosition(int position) {
        return courses.get(position);
    }


    public class DownloadCoursesViewHolder extends MultiChoiceRecyclerViewAdapter.ViewHolder {


        private final RowCourseDownloadBinding binding;

        public DownloadCoursesViewHolder(View itemView) {

            super(itemView);

            binding = RowCourseDownloadBinding.bind(itemView);

            binding.downloadCourseBtn.setOnClickListener(v -> {
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
 

