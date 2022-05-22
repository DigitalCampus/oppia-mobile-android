package org.digitalcampus.oppia.utils.course_status;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ViewStatusBadgeBinding;
import org.digitalcampus.oppia.model.Course;

import java.util.HashMap;
import java.util.Map;

public class CourseStatusBadgeView extends FrameLayout {

    private Map<String, CourseStatusBadgeData> courseStatusBadgeDataMap = new HashMap<String, CourseStatusBadgeData>() {{

        put(Course.STATUS_DRAFT, new CourseStatusBadgeData(
                R.string.status_draft, R.drawable.ic_status_draft, R.color.bg_badge_status_draft));

        put(Course.STATUS_NEW_DOWNLOADS_DISABLED, new CourseStatusBadgeData(
                R.string.status_new_download_disabled, R.drawable.ic_new_download_disabled, R.color.bg_badge_status_new_download_disabled));

        put(Course.STATUS_ARCHIVED, new CourseStatusBadgeData(
                R.string.status_archived, R.drawable.ic_status_archived, R.color.bg_badge_status_archived));

        put(Course.STATUS_READ_ONLY, new CourseStatusBadgeData(
                R.string.status_read_only, R.drawable.ic_status_read_only, R.color.bg_badge_status_read_only));

    }};

    private ViewStatusBadgeBinding binding;


    public CourseStatusBadgeView(@NonNull Context context) {
        super(context);
        init();
    }

    public CourseStatusBadgeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CourseStatusBadgeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = ViewStatusBadgeBinding.inflate(LayoutInflater.from(getContext()));
        addView(binding.getRoot());

        setVisibility(GONE);
    }

    public void setCourseStatus(String status) {

        if (Course.STATUS_LIVE.equals(status) || Course.STATUS_NEW_DOWNLOADS_DISABLED.equals(status)) {
            setVisibility(GONE);
            return;
        }

        if (!courseStatusBadgeDataMap.containsKey(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        setVisibility(VISIBLE);

        CourseStatusBadgeData courseStatusBadgeData = courseStatusBadgeDataMap.get(status);
        binding.getRoot().getBackground().setColorFilter(
                ContextCompat.getColor(getContext(), courseStatusBadgeData.getColor()), PorterDuff.Mode.SRC_ATOP);
        binding.imgStatusBadge.setImageResource(courseStatusBadgeData.getIcon());
        binding.tvStatusBadge.setText(courseStatusBadgeData.getText());

    }
}
