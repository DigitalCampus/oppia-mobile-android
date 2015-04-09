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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.lucasr.twowayview.TwoWayView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionListAdapter extends ArrayAdapter<Section>{

	public static final String TAG = SectionListAdapter.class.getSimpleName();
	public static final String TAG_PLACEHOLDER = "placeholder";
	
	private final Context ctx;
	private final ArrayList<Section> sectionList;
	private SharedPreferences prefs;
	private Course course;
    private final String locale;

	public SectionListAdapter(Context context, Course course, ArrayList<Section> sectionList) {
		super(context, R.layout.section_list_row, sectionList);
		this.ctx = context;
		this.sectionList = sectionList;
		this.course = course;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        locale = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
	}

    static class SectionViewHolder{
        TextView sectionTitle;
        TwoWayView sectionActivities;
    }

	public View getView(int position, View convertView, ViewGroup parent) {

        SectionViewHolder viewHolder;
        final ActivityAdapter innerListAdapter;

        final Section section = sectionList.get(position);
        ArrayList<Activity> sectionActivities = section.getActivities();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.section_list_row, parent, false);
            viewHolder = new SectionViewHolder();
            viewHolder.sectionTitle = (TextView) convertView.findViewById(R.id.section_title);
            viewHolder.sectionActivities = (TwoWayView) convertView.findViewById(R.id.section_activities);
            innerListAdapter = new ActivityAdapter(locale, course.getLocation());
            viewHolder.sectionActivities.setAdapter(innerListAdapter);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (SectionViewHolder) convertView.getTag();
            innerListAdapter = (ActivityAdapter) viewHolder.sectionActivities.getAdapter();
        }

	    String title = "";
	    if(prefs.getBoolean(PrefsActivity.PREF_SHOW_SECTION_NOS, false)){
	    	title += String.valueOf(section.getOrder()) + ". ";
	    }
	    title += section.getTitle(locale);
        viewHolder.sectionTitle.setText(title);
        innerListAdapter.setData(sectionActivities);
        viewHolder.sectionActivities.setSelection(0);
        viewHolder.sectionActivities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ctx, CourseActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Section.TAG, section);
                tb.putSerializable(Course.TAG, course);
                tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, position);
                intent.putExtras(tb);
                ctx.startActivity(intent);
            }
        });

        /*
        // we clear the previous children views (if set)
        viewHolder.sectionActivities.removeAllViews();
	    for(int i=0 ; i<sectionActivities.size(); i++){

            Activity activity = sectionActivities.get(i);
            View horizRowItem = inflater.inflate(R.layout.section_horizonal_item, parent, false);
		    
		    viewHolder.sectionActivities.addView(horizRowItem);
		    
		    TextView tv = (TextView) horizRowItem.findViewById(R.id.activity_title);
		    tv.setText(activity.getTitle(locale));
		    
		    // set image
		    ImageView iv = (ImageView) horizRowItem.findViewById(R.id.activity_image);
	    	if(!activity.hasCustomImage()){
	    		iv.setScaleType(ImageView.ScaleType.CENTER);
	    	}
	    	//iv.setImageDrawable(activity.getImageFile(course.getLocation(), ctx.getResources()));
	    	LinearLayout activityObject = (LinearLayout) horizRowItem.findViewById(R.id.activity_object);
	    	// highlight if completed
	    	if(activity.getCompleted() && prefs.getBoolean(PrefsActivity.PREF_HIGHLIGHT_COMPLETED, MobileLearning.DEFAULT_DISPLAY_COMPLETED)){
	    		activityObject.setBackgroundResource(R.drawable.activity_background_completed);
	    	}

	    	activityObject.setTag(R.id.TAG_PLACEHOLDER_ID, i);
		    // set clicker
	    	activityObject.setClickable(true);
	    	activityObject.setSelected(true);
	    	activityObject.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					int placeholder = (Integer) v.getTag(R.id.TAG_PLACEHOLDER_ID);
					Intent i = new Intent(ctx, CourseActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(Section.TAG, section);
					tb.putSerializable(Course.TAG, course);
					tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, (Integer) placeholder);
					i.putExtras(tb);
	         		ctx.startActivity(i);
				}
            		    });
	    }*/
	    
	    return convertView;
	}


    private class ActivityAdapter extends BaseAdapter{

        class ActivityViewHolder{
            TextView activityTitle;
            ImageView activityImage;
            View activityContainer;
        }

        private ArrayList<Activity> listActivities = new ArrayList<Activity>();
        private String locale;
        private String courseLocation;

        public ActivityAdapter(String locale, String courseLocation){
            this.locale = locale;
            this.courseLocation = courseLocation;
        }

        @Override
        public int getCount() {
            return listActivities.size();
        }

        @Override
        public Activity getItem(int position) {
            return listActivities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ActivityViewHolder viewHolder;
            Activity activity = listActivities.get(position);
            if (convertView == null) {
                viewHolder = new ActivityViewHolder();
                Context context = parent.getContext();
                convertView = LayoutInflater.from(context).inflate(R.layout.section_horizonal_item, null);
                viewHolder.activityContainer = convertView.findViewById(R.id.activity_object);
                viewHolder.activityTitle = (TextView)  viewHolder.activityContainer.findViewById(R.id.activity_title);
                viewHolder.activityImage = (ImageView)  viewHolder.activityContainer.findViewById(R.id.activity_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ActivityViewHolder) convertView.getTag();
            }

            viewHolder.activityTitle.setText(activity.getTitle(locale));
            viewHolder.activityImage.setScaleType(!activity.hasCustomImage()?ImageView.ScaleType.CENTER: ImageView.ScaleType.FIT_CENTER);
            viewHolder.activityImage.setImageDrawable(activity.getImageFile(courseLocation, ctx.getResources()));
            boolean highlightActivity = activity.getCompleted() && prefs.getBoolean(PrefsActivity.PREF_HIGHLIGHT_COMPLETED, MobileLearning.DEFAULT_DISPLAY_COMPLETED);
            viewHolder.activityContainer.setBackgroundResource(highlightActivity ? R.drawable.activity_background_completed : 0);

            return convertView;
        }

        public void setData(List<Activity> data) {
            listActivities.clear();
            listActivities.addAll(data);
            notifyDataSetChanged();
        }
    }
}

