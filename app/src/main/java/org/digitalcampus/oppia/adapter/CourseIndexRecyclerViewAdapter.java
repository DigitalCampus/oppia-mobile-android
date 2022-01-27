package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.CourseTitleBarBinding;
import org.digitalcampus.mobile.learning.databinding.RowCourseIndexSectionHeaderBinding;
import org.digitalcampus.mobile.learning.databinding.RowCourseIndexSectionItemBinding;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.CircleTransform;
import org.digitalcampus.oppia.utils.ui.ExpandableRecyclerView;

import java.io.File;
import java.util.List;
import java.util.Locale;


public class CourseIndexRecyclerViewAdapter extends ExpandableRecyclerView.Adapter<CourseIndexRecyclerViewAdapter.ChildViewHolder, CourseIndexRecyclerViewAdapter.SectionViewHolder, CourseIndexRecyclerViewAdapter.HeaderViewHolder, Activity, Section> {

    private List<Section> sectionList;
    private boolean showSectionNumbers;
    private boolean highlightCompleted;
    private String prefLang;
    private String courseLocation;
    private String courseTitle;
    private String courseIcon;
    private int highlightColor;
    private int normalColor;


    public CourseIndexRecyclerViewAdapter(Context ctx, SharedPreferences prefs, List<Section> sectionList, Course course) {
        super();
        this.sectionList = sectionList;

        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        showSectionNumbers = prefs.getBoolean(PrefsActivity.PREF_SHOW_SECTION_NOS, false);
        highlightCompleted = prefs.getBoolean(PrefsActivity.PREF_HIGHLIGHT_COMPLETED, App.DEFAULT_DISPLAY_COMPLETED);
        boolean startCollapsed = prefs.getBoolean(PrefsActivity.PREF_START_COURSEINDEX_COLLAPSED, false);

        this.startExpanded = !startCollapsed;
        courseLocation = course.getLocation();
        highlightColor = ContextCompat.getColor(ctx, R.color.course_index_highlight);
        normalColor = ContextCompat.getColor(ctx, R.color.text_dark);

        courseTitle = course.getTitle(prefLang);
        courseIcon = course.getImageFileFromRoot();

        this.setHeaderVisible(true);
    }

    public void expandCollapseAllSections(boolean expand) {
        for (int i = 0; i < sectionList.size(); i++) {
            if (expand) {
                expand(i);
            } else {
                collapse(i);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getGroupItemCount() {
        return sectionList.size();
    }

    @Override
    public int getChildItemCount(int i) {
        return sectionList.get(i).getActivities().size();
    }

    @Override
    public Section getGroupItem(int i) {
        return i < sectionList.size() ? sectionList.get(i) : null;
    }

    @Override
    public Activity getChildItem(int group, int child) {
        return sectionList.get(group).getActivities().get(child);
    }

    @Override
    protected HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_title_bar, parent, false);
        return new HeaderViewHolder(rootView);
    }

    @Override
    protected SectionViewHolder onCreateGroupViewHolder(ViewGroup parent) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_course_index_section_header, parent, false);
        return new SectionViewHolder(rootView);
    }

    @Override
    protected ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_course_index_section_item, parent, false);
        return new ChildViewHolder(rootView);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder holder) {
        holder.binding.courseTitle.setText(courseTitle);
        Picasso.get().load(new File(courseIcon))
                .placeholder(R.drawable.course_icon_placeholder)
                .error(R.drawable.course_icon_placeholder)
                .transform(new CircleTransform())
                .into(holder.binding.courseIcon);
    }

    @Override
    public void onBindGroupViewHolder(SectionViewHolder holder, int group) {
        super.onBindGroupViewHolder(holder, group);
        Section section = getGroupItem(group);
        String title = showSectionNumbers ? section.getOrder() + ". " : "";
        title += section.getTitle(prefLang);
        holder.binding.title.setText(title);
        holder.binding.sectionIcon.setVisibility(section.hasCustomImage() ? View.VISIBLE : View.GONE);
        if (section.hasCustomImage()){
            String image = section.getImageFilePath(courseLocation);
            Picasso.get().load(new File(image)).into(holder.binding.sectionIcon);
        }

        holder.binding.activitiesCompleted.setText(section.getCompletedActivities() + "/" + section.getActivities().size());

    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder, int group, int position) {
        super.onBindChildViewHolder(holder, group, position);
        Activity activity = getChildItem(group, position);
        boolean highlightActivity = highlightCompleted && activity.getCompleted();
        holder.binding.title.setText(activity.getTitle(prefLang));
        holder.binding.title.setTextColor(highlightActivity ? highlightColor : normalColor);
        holder.binding.badge.setVisibility(highlightActivity ? View.VISIBLE : View.GONE);

        if (activity.hasCustomImage()) {
            String image = activity.getImageFilePath(courseLocation);
            Picasso.get().load(new File(image)).into(holder.binding.icon);
        } else {
            int defaultActivityDrawable = activity.getDefaultResourceImage();
            holder.binding.icon.setImageResource(defaultActivityDrawable);
        }
    }


    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final CourseTitleBarBinding binding;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            binding = CourseTitleBarBinding.bind(itemView);
        }
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {


        private final RowCourseIndexSectionItemBinding binding;

        public ChildViewHolder(View itemView) {
            super(itemView);
            binding = RowCourseIndexSectionItemBinding.bind(itemView);

        }
    }

    public class SectionViewHolder extends ExpandableRecyclerView.GroupViewHolder {
        private final RowCourseIndexSectionHeaderBinding binding;

        public SectionViewHolder(View itemView) {
            super(itemView);
            binding = RowCourseIndexSectionHeaderBinding.bind(itemView);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

    }
}