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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.GetImageListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.task.GetImageTask;
import org.digitalcampus.oppia.task.Payload;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TagListAdapter extends ArrayAdapter<Tag> implements GetImageListener{

	public static final String TAG = TagListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<Tag> tagList;
	private ImageView tagIcon;
	
	public TagListAdapter(Activity context, ArrayList<Tag> tagList) {
		super(context, R.layout.tag_row, tagList);
		this.ctx = context;
		this.tagList = tagList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.tag_row, parent, false);
	    Tag t = tagList.get(position);
	    rowView.setTag(t);
	    
	    TextView tagName = (TextView) rowView.findViewById(R.id.tag_name);
	    tagName.setText(ctx.getString(R.string.tag_label,t.getName(),t.getCount()));
	    
	    if(t.isHighlight()){
	    	tagName.setTypeface(null, Typeface.BOLD);
	    }
	    
	    if(t.getDescription() != null && !t.getDescription().trim().equals("") ){
		    TextView tagDesc = (TextView) rowView.findViewById(R.id.tag_description);
		    tagDesc.setText(t.getDescription());
		    tagDesc.setVisibility(View.VISIBLE);
	    }
	    
	    tagIcon = (ImageView) rowView.findViewById(R.id.tag_icon);
	    if(t.getIcon() != null){
	    	Log.d(TAG,t.getIcon());
	    	//GetImageTask task = new GetImageTask(this.ctx);
			//Payload p = new Payload(t.getIcon());
			//task.setGetImageListener(this);
			//task.execute(p);
	    }
	    return rowView;
	}

	public void downloadComplete(Payload p) {
		Log.d(TAG,"download complete");
		Drawable d = (Drawable) p.getResponseData().get(0);
		tagIcon.setImageDrawable(d);
		tagIcon.setVisibility(View.VISIBLE);
	}

	public void downloadProgressUpdate(DownloadProgress dp) {
		// TODO Auto-generated method stub
		
	}
}
