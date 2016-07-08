/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class CourseListAdapter extends ArrayAdapter<Course> {

	public static final String TAG = CourseListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<Course> courseList;
	private SharedPreferences prefs;
	
	public CourseListAdapter(Activity context, ArrayList<Course> courseList) {
		super(context, R.layout.course_list_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

    static class CourseViewHolder{
        TextView courseTitle;
        TextView courseDescription;
        ProgressBar courseProgress;
        ImageView courseImage;
        ProgressBarAnimator barAnimator;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        CourseViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.course_list_row, parent, false);
            viewHolder = new CourseViewHolder();
            viewHolder.courseTitle = (TextView) convertView.findViewById(R.id.course_title);
            viewHolder.courseDescription = (TextView) convertView.findViewById(R.id.course_description);
            viewHolder.courseProgress = (ProgressBar) convertView.findViewById(R.id.course_progress_bar);
            viewHolder.courseImage = (ImageView) convertView.findViewById(R.id.course_image);
            viewHolder.barAnimator = new ProgressBarAnimator(viewHolder.courseProgress);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (CourseViewHolder) convertView.getTag();
        }

	    Course c = courseList.get(position);
        viewHolder.courseTitle.setText(c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));

	    if (prefs.getBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, true)){
            viewHolder.courseDescription.setText(c.getDescription(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
	    } else {
            viewHolder.courseDescription.setVisibility(View.GONE);
	    }

	    if (prefs.getBoolean(PrefsActivity.PREF_SHOW_PROGRESS_BAR, MobileLearning.DEFAULT_DISPLAY_PROGRESS_BAR)){
            int courseProgress = (int) c.getProgressPercent();
            viewHolder.courseProgress.setProgress(courseProgress);
            viewHolder.barAnimator.animate(courseProgress);
            //Set the value to true so it doesn' t get animated again
            viewHolder.barAnimator.setAnimated(true);
	    } else {
            viewHolder.courseProgress.setVisibility(View.GONE);
	    }
	    
		// set image
		if(c.getImageFile() != null){
			String image = c.getImageFileFromRoot();
            Picasso.with(ctx).load(new File(image))
                    .placeholder(R.drawable.default_course)
                    .into(viewHolder.courseImage);
		}
        else{
            viewHolder.courseImage.setImageResource(R.drawable.default_course);
        }
	    return convertView;
	}

}
