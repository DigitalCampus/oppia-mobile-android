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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.CircleTransform;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

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
        CircularProgressBar circularProgressBar;
        TextView courseTitle;
        TextView courseDescription;
        ImageView courseImage;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        CourseViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.course_list_row, parent, false);
            viewHolder = new CourseViewHolder();
            viewHolder.courseTitle = convertView.findViewById(R.id.course_title);
            viewHolder.courseDescription = convertView.findViewById(R.id.course_description);
            viewHolder.courseImage = convertView.findViewById(R.id.course_image);
            viewHolder.circularProgressBar = convertView.findViewById(R.id.circularProgressBar);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (CourseViewHolder) convertView.getTag();
        }

	    Course c = courseList.get(position);
        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        viewHolder.courseTitle.setText(c.getTitle(lang));
        String description = c.getDescription(lang);
	    if (!TextUtils.isEmpty(description) && prefs.getBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, true)){
            viewHolder.courseDescription.setText(description);
            viewHolder.courseDescription.setVisibility(View.VISIBLE);
	    } else {
            viewHolder.courseDescription.setVisibility(View.GONE);
	    }

	    if (prefs.getBoolean(PrefsActivity.PREF_SHOW_PROGRESS_BAR, MobileLearning.DEFAULT_DISPLAY_PROGRESS_BAR)){
            int courseProgress = (int) c.getProgressPercent();
            viewHolder.circularProgressBar.setVisibility(View.VISIBLE);
            viewHolder.circularProgressBar.setProgressWithAnimation(courseProgress, 1000l);
	    } else {
            viewHolder.circularProgressBar.setVisibility(View.GONE);
	    }
	    
		// set image
		if(c.getImageFile() != null){
			String image = c.getImageFileFromRoot();
            Picasso.get().load(new File(image))
                    .placeholder(R.drawable.default_course)
                    .transform(new CircleTransform())
                    .into(viewHolder.courseImage);
		}
        else{
            viewHolder.courseImage.setImageResource(R.drawable.default_course);
        }
	    return convertView;
	}

}
