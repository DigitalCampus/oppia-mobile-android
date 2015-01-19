/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Course;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DownloadCourseListAdapter extends ArrayAdapter<Course>{

	public static final String TAG = DownloadCourseListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<Course> courseList;
	private SharedPreferences prefs;

    private ListInnerBtnOnClickListener onClickListener;
	
	public DownloadCourseListAdapter(Activity context, ArrayList<Course> courseList) {
		super(context, R.layout.course_download_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

    static class DownloadCourseViewHolder{
        TextView courseTitle;
        TextView courseDraft;
        TextView courseDescription;
        Button actionBtn;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        DownloadCourseViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.course_download_row, parent, false);
            viewHolder = new DownloadCourseViewHolder();
            viewHolder.courseTitle = (TextView) convertView.findViewById(R.id.course_title);
            viewHolder.courseDraft = (TextView) convertView.findViewById(R.id.course_draft);
            viewHolder.courseDescription = (TextView) convertView.findViewById(R.id.course_description);
            viewHolder.actionBtn = (Button) convertView.findViewById(R.id.download_course_btn);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (DownloadCourseViewHolder) convertView.getTag();
        }

	    Course c = courseList.get(position);

        viewHolder.courseTitle.setText(c.getTitle(
                prefs.getString(PrefsActivity.PREF_LANGUAGE,
                Locale.getDefault().getLanguage())));

	    if (c.isDraft()){
            viewHolder.courseDraft.setText(ctx.getString(R.string.course_draft));
	    } else {
            viewHolder.courseDraft.setVisibility(View.GONE);
	    }

	    String desc = c.getDescription(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
	    if (desc != null){
            viewHolder.courseDescription.setText(desc);
	    } else {
            viewHolder.courseDescription.setVisibility(View.GONE);
	    }

	    if(c.isInstalled()){
	    	if(c.isToUpdate()){
                viewHolder.actionBtn.setText(R.string.update);
                viewHolder.actionBtn.setEnabled(true);
	    	} else if (c.isToUpdateSchedule()){
                viewHolder.actionBtn.setText(R.string.update_schedule);
                viewHolder.actionBtn.setEnabled(true);
	    	} else {
                viewHolder.actionBtn.setText(R.string.installed);
                viewHolder.actionBtn.setEnabled(false);
	    	}
	    } else {
            viewHolder.actionBtn.setText(R.string.install);
            viewHolder.actionBtn.setEnabled(true);
	    }

        viewHolder.actionBtn.setTag(position); //For passing the list item index
        viewHolder.actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickListener != null)
                    onClickListener.onClick((Integer) v.getTag());
            }
        });

	    return convertView;
	}

    public void setOnClickListener(ListInnerBtnOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
