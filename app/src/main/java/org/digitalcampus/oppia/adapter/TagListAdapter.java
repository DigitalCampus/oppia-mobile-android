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

import org.kano.training.oppia.R;
import org.digitalcampus.oppia.model.Tag;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class TagListAdapter extends ArrayAdapter<Tag> {

	public static final String TAG = TagListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<Tag> tagList;
	
	public TagListAdapter(Activity context, ArrayList<Tag> tagList) {
		super(context, R.layout.tag_row, tagList);
		this.ctx = context;
		this.tagList = tagList;
	}

    static class TagViewHolder{
        TextView tagName;
        TextView tagDescription;
        ImageView tagIcon;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        TagViewHolder viewHolder;
        
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.tag_row, parent, false);
            viewHolder = new TagViewHolder();
            viewHolder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
            viewHolder.tagDescription = (TextView) convertView.findViewById(R.id.tag_description);
            viewHolder.tagIcon = (ImageView) convertView.findViewById(R.id.tag_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TagViewHolder) convertView.getTag();
        }

	    Tag t = tagList.get(position);

        viewHolder.tagName.setText(ctx.getString(R.string.tag_label,t.getName(),t.getCount()));
	    if(t.isHighlight()){ 
	    	viewHolder.tagName.setTypeface(null, Typeface.BOLD); 
	    } else {
	    	viewHolder.tagName.setTypeface(null, Typeface.NORMAL);
	    }
	    if(t.getDescription() != null && !t.getDescription().trim().equals("") ){
            viewHolder.tagDescription.setText(t.getDescription());
            viewHolder.tagDescription.setVisibility(View.VISIBLE);
	    } else {
	    	viewHolder.tagDescription.setVisibility(View.GONE);
	    }
	    if(t.getIcon() != null){
            Picasso.with(ctx).load(t.getIcon()).into(viewHolder.tagIcon);
            viewHolder.tagIcon.setVisibility(View.VISIBLE);
	    } else {
	    	viewHolder.tagIcon.setVisibility(View.GONE);
	    }

	    return convertView;
	}
}

