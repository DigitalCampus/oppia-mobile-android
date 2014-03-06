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
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.fragment_badges_list_row, parent, false);
	    Badges b = badgesList.get(position);
	    
	    TextView badgesDescription = (TextView) rowView.findViewById(R.id.badges_description);
	    badgesDescription.setText(b.getDescription());
	    
	    TextView badgesTime = (TextView) rowView.findViewById(R.id.badges_time);
	    badgesTime.setText(b.getTimeAsString());
	    
	    TextView badgesDate = (TextView) rowView.findViewById(R.id.badges_date);
	    badgesDate.setText(b.getDateAsString());
	    
	    return rowView;
	}
}
