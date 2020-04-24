package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.R;
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


    public CourseIndexRecyclerViewAdapter(Context ctx, List<Section> sectionList, Course course) {
        super();
        this.sectionList = sectionList;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
        holder.title.setText(courseTitle);
        Picasso.get().load(new File(courseIcon))
                .placeholder(R.drawable.course_icon_placeholder)
                .error(R.drawable.course_icon_placeholder)
                .transform(new CircleTransform())
                .into(holder.courseImage);
    }

    @Override
    public void onBindGroupViewHolder(SectionViewHolder holder, int group) {
        super.onBindGroupViewHolder(holder, group);
        Section section = getGroupItem(group);
        String title = showSectionNumbers ? section.getOrder() + ". " : "";
        title += section.getTitle(prefLang);
        holder.title.setText(title);
        holder.completedActivities.setText(section.getCompletedActivities() + "/" + section.getActivities().size());

    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder, int group, int position) {
        super.onBindChildViewHolder(holder, group, position);
        Activity activity = getChildItem(group, position);
        boolean highlightActivity = highlightCompleted && activity.getCompleted();
        holder.title.setText(activity.getTitle(prefLang));
        holder.title.setTextColor(highlightActivity ? highlightColor : normalColor);
        holder.completedBadge.setVisibility(highlightActivity ? View.VISIBLE : View.GONE);

        if (activity.hasCustomImage()) {
            String image = activity.getImageFilePath(courseLocation);
            Picasso.get().load(new File(image)).into(holder.activityImage);
        } else {
            int defaultActivityDrawable = activity.getDefaultResourceImage();
            holder.activityImage.setImageResource(defaultActivityDrawable);
        }
    }


    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView courseImage;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.course_title);
            courseImage = itemView.findViewById(R.id.course_icon);
        }
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView activityImage;
        private View completedBadge;

        public ChildViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            activityImage = itemView.findViewById(R.id.icon);
            completedBadge = itemView.findViewById(R.id.badge);

        }
    }

    public class SectionViewHolder extends ExpandableRecyclerView.GroupViewHolder {
        private TextView title;

        private TextView completedActivities;

        public SectionViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            completedActivities = itemView.findViewById(R.id.activities_completed);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

    }
}