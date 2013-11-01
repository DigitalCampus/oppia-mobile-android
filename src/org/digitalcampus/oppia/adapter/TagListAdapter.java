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
import org.digitalcampus.oppia.model.Tag;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TagListAdapter extends ArrayAdapter<Tag>{

	public static final String TAG = TagListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<Tag> tagList;
	
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
	    return rowView;
	}
	
	public void closeDialogs(){
		//if (myProgress != null){
		//	myProgress.dismiss();
		//}
	}
}
