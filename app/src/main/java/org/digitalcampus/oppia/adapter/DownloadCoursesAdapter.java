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
import org.digitalcampus.oppia.model.Course;
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

    private final String updateDescription;
    private final String installDescription;
    private final String installedDescription;
    private final String cancelDescription;


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

        viewHolder.binding.downloadCourseBtn.setVisibility(isMultiChoiceMode ? View.INVISIBLE : View.VISIBLE);
        updateCourseDescription(viewHolder, c);
        updateActionButton(viewHolder, c);
    }

    private void updateCourseDescription(DownloadCoursesViewHolder viewHolder, CourseInstallViewAdapter course) {
        viewHolder.binding.courseTitle.setText(course.getTitle(prefLang));

        viewHolder.binding.viewCourseStatus.setCourseStatus(course.getStatus());

        String desc = course.getDescription(prefLang);
        if (desc != null) {
            viewHolder.binding.courseDescription.setVisibility(View.VISIBLE);
            viewHolder.binding.courseDescription.setText(desc);
        } else {
            viewHolder.binding.courseDescription.setVisibility(View.GONE);
        }

        String organisation = course.getOrganisationName();
        if (!TextUtils.isEmpty(organisation) && !(course.isDownloading() || course.isInstalling())) {
            viewHolder.binding.labelAuthor.setVisibility(View.VISIBLE);
            viewHolder.binding.courseAuthor.setVisibility(View.VISIBLE);
            viewHolder.binding.courseAuthor.setText(organisation);
        } else {
            viewHolder.binding.labelAuthor.setVisibility(View.GONE);
            viewHolder.binding.courseAuthor.setVisibility(View.GONE);
        }
    }

    private void updateActionButton(DownloadCoursesViewHolder viewHolder, CourseInstallViewAdapter course) {
        int actionBtnImageRes;
        if (course.isDownloading() || course.isInstalling()) {
            actionBtnImageRes = R.drawable.ic_action_cancel;
            viewHolder.binding.downloadCourseBtn.setContentDescription(cancelDescription);
            viewHolder.binding.downloadCourseBtn.setEnabled(!course.isInstalling());

            viewHolder.binding.downloadProgress.setVisibility(View.VISIBLE);
            if (course.getProgress() > 0) {
                viewHolder.binding.downloadProgress.setIndeterminate(false);
                viewHolder.binding.downloadProgress.setProgress(course.getProgress());
            } else {
                viewHolder.binding.downloadProgress.setIndeterminate(true);
            }
        } else {
            viewHolder.binding.downloadProgress.setVisibility(View.GONE);

            if (course.isInstalled()) {
                if (course.isToUpdate()) {
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
 

