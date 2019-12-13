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


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.SearchResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class SearchResultsListAdapter  extends ArrayAdapter<SearchResult>{

	public static final String TAG = SearchResultsListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<SearchResult> searchResultList;
    private String prefLang;
	
	public SearchResultsListAdapter(Context context, ArrayList<SearchResult> searchResultList) {
		super(context, R.layout.search_results_row, searchResultList);
		this.ctx = context;
		this.searchResultList = searchResultList;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
	}

    static class SearchResultsViewHolder{
        private TextView activityTitle;
        private TextView sectionTitle;
        private TextView courseTitle;
        private ImageView activityImage;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        SearchResultsViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.search_results_row, parent, false);
            viewHolder = new SearchResultsViewHolder();
            viewHolder.activityTitle = convertView.findViewById(R.id.activity_title);
            viewHolder.sectionTitle = convertView.findViewById(R.id.section_title);
            viewHolder.courseTitle = convertView.findViewById(R.id.course_title);
            viewHolder.activityImage = convertView.findViewById(R.id.activity_icon);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (SearchResultsViewHolder) convertView.getTag();
        }


	    SearchResult sr = searchResultList.get(position);
        Activity activity = sr.getActivity();
        Course course = sr.getCourse();

	    String cTitle = course.getTitle(prefLang);
	    String sTitle = sr.getSection().getTitle(prefLang);
	    String aTitle = activity.getTitle(prefLang);

        viewHolder.activityTitle.setText(aTitle);
        viewHolder.sectionTitle.setText(sTitle);
        viewHolder.courseTitle.setText(cTitle);
        Log.d(TAG, course.getLocation());
        convertView.setTag(R.id.TAG_COURSE, course);
        convertView.setTag(R.id.TAG_ACTIVITY_DIGEST, activity.getDigest());

        Log.d(TAG, activity.getImageFilePath(""));
        if (activity.hasCustomImage()){

            String image = activity.getImageFilePath(course.getLocation());
            Log.d(TAG, new File(image).exists() ? "Exists" : "Noooo");
            Picasso.get().load(new File(image)).into(viewHolder.activityImage);
        }
        else {
            int defaultActivityDrawable = activity.getDefaultResourceImage();
            viewHolder.activityImage.setImageResource(defaultActivityDrawable);
        }
	   
	    return convertView;
	}
}
