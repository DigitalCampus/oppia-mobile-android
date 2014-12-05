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
import org.digitalcampus.oppia.model.SearchResult;
import org.digitalcampus.oppia.utils.ImageUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchResultsListAdapter  extends ArrayAdapter<SearchResult>{

	public static final String TAG = SearchResultsListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<SearchResult> searchResultList;
	private SharedPreferences prefs;
	
	public SearchResultsListAdapter(Activity context, ArrayList<SearchResult> searchResultList) {
		super(context, R.layout.search_results_row, searchResultList);
		this.ctx = context;
		this.searchResultList = searchResultList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

    static class SearchResultsViewHolder{
        TextView activityTitle;
        TextView sectionTitle;
        TextView courseTitle;
        ImageView courseImage;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        SearchResultsViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.search_results_row, parent, false);
            viewHolder = new SearchResultsViewHolder();
            viewHolder.activityTitle = (TextView) convertView.findViewById(R.id.activity_title);
            viewHolder.sectionTitle = (TextView) convertView.findViewById(R.id.section_title);
            viewHolder.courseTitle = (TextView) convertView.findViewById(R.id.course_title);
            viewHolder.courseImage = (ImageView) convertView.findViewById(R.id.course_image);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (SearchResultsViewHolder) convertView.getTag();
        }

	    SearchResult sr = searchResultList.get(position);

	    String cTitle = sr.getCourse().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
	    String sTitle = sr.getSection().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
	    String aTitle = sr.getActivity().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

        viewHolder.activityTitle.setText(aTitle);
        viewHolder.sectionTitle.setText(sTitle);
        viewHolder.courseTitle.setText(cTitle);

        convertView.setTag(R.id.TAG_COURSE,sr.getCourse());
        convertView.setTag(R.id.TAG_ACTIVITY_DIGEST,sr.getActivity().getDigest());
		
		if(sr.getCourse().getImageFile() != null){
			BitmapDrawable courseImageBitmap = ImageUtils.LoadBMPsdcard(sr.getCourse().getImageFileFromRoot(), ctx.getResources(), R.drawable.dc_logo);
            viewHolder.courseImage.setImageDrawable(courseImageBitmap);
		}
	   
	    return convertView;
	}
}
