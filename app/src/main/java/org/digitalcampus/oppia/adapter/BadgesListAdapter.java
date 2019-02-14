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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Badges;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BadgesListAdapter extends ArrayAdapter<Badges>{

	public static final String TAG = BadgesListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<Badges> badgesList;
	
	public BadgesListAdapter(Activity context, ArrayList<Badges> badgesList) {
		super(context, R.layout.fragment_badges_list_row, badgesList);
		this.ctx = context;
		this.badgesList = badgesList;
	}

    static class BadgeViewHolder{
        TextView badgeDescription;
        TextView badgeDate;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        BadgeViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.fragment_badges_list_row, parent, false);
            viewHolder = new BadgeViewHolder();
            viewHolder.badgeDescription = convertView.findViewById(R.id.badges_description);
            viewHolder.badgeDate = convertView.findViewById(R.id.badges_date);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (BadgeViewHolder) convertView.getTag();
        }

	    Badges b = badgesList.get(position);
        viewHolder.badgeDescription.setText(b.getDescription());
        viewHolder.badgeDate.setText(b.getDateAsString());
	    
	    return convertView;
	}

}
