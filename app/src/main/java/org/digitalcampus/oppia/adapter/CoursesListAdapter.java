package org.digitalcampus.oppia.adapter;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.transition.Hold;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowCourseListBinding;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.CircleTransform;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

public class CoursesListAdapter extends RecyclerView.Adapter<CoursesListAdapter.CourseListViewHolder> {


    private final SharedPreferences prefs;
    private List<Course> courses;
    private Context context;
    private OnItemClickListener itemClickListener;

    private Dialog contextMenuDialog;
    private int currentSelectedItem;
    private int selectedOption;


    public CoursesListAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
        createDialog();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public CourseListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_course_list, parent, false);

        // Return a new holder instance
        return new CourseListViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final CourseListViewHolder viewHolder, final int position) {

        final Course course = getItemAtPosition(position);

        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        viewHolder.binding.courseTitle.setText(course.getTitle(lang));
        String description = course.getDescription(lang);
        if (!TextUtils.isEmpty(description) && prefs.getBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, BuildConfig.SHOW_COURSE_DESCRIPTION)) {
            viewHolder.binding.courseDescription.setText(description);
            viewHolder.binding.courseDescription.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.courseDescription.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(PrefsActivity.PREF_SHOW_PROGRESS_BAR, App.DEFAULT_DISPLAY_PROGRESS_BAR)) {
            int courseProgress = (int) course.getProgressPercent();
            viewHolder.binding.circularProgressBar.setVisibility(View.VISIBLE);
            viewHolder.binding.circularProgressBar.setProgressWithAnimation(courseProgress, 1000L);
        } else {
            viewHolder.binding.circularProgressBar.setVisibility(View.GONE);
        }

        // set image
        if (course.getImageFile() != null) {
            String image = course.getImageFileFromRoot();
            Picasso.get().load(new File(image))
                    .placeholder(R.drawable.course_icon_placeholder)
                    .transform(new CircleTransform())
                    .into(viewHolder.binding.courseImage);
        } else {
            viewHolder.binding.courseImage.setImageResource(R.drawable.course_icon_placeholder);
        }

        if (course.isToUpdate()) {
            viewHolder.binding.imgSyncStatus.setVisibility(View.VISIBLE);
            viewHolder.binding.imgSyncStatus.setImageResource(R.drawable.ic_action_refresh);
        } else if (course.isToDelete()) {
            viewHolder.binding.imgSyncStatus.setVisibility(View.VISIBLE);
            viewHolder.binding.imgSyncStatus.setImageResource(R.drawable.dialog_ic_action_delete);
        } else {
            viewHolder.binding.imgSyncStatus.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public Course getItemAtPosition(int position) {
        return courses.get(position);
    }


    public class CourseListViewHolder extends RecyclerView.ViewHolder {

        RowCourseListBinding binding;


        public CourseListViewHolder(View itemView) {

            super(itemView);

            binding = RowCourseListBinding.bind(itemView);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(v -> {
                currentSelectedItem = getAdapterPosition();
                selectedOption = -1;
                contextMenuDialog.show();
                return true;
            });


        }

    }

    private void createDialog() {
        contextMenuDialog = new Dialog(context);
        contextMenuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        contextMenuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        contextMenuDialog.setContentView(R.layout.dialog_course_contextmenu);
        registerMenuClick(R.id.course_context_reset);
        registerMenuClick(R.id.course_context_delete);
        registerMenuClick(R.id.course_context_update_activity);

        //@Override
        contextMenuDialog.setOnDismissListener(dialogInterface -> {
            if (itemClickListener != null) {
                itemClickListener.onContextMenuItemSelected(currentSelectedItem, selectedOption);
            }
        });
    }

    private void registerMenuClick(final int id) {
        //@Override
        contextMenuDialog.findViewById(id).setOnClickListener(view -> {
            selectedOption = id;
            contextMenuDialog.dismiss();
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onContextMenuItemSelected(int position, int itemId);
    }
}


