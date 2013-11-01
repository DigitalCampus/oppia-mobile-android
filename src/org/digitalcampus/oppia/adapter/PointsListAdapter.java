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
import org.digitalcampus.oppia.model.Points;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PointsListAdapter extends ArrayAdapter<Points>{

	public static final String TAG = PointsListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final ArrayList<Points> pointsList;
	
	public PointsListAdapter(Activity context, ArrayList<Points> pointsList) {
		super(context, R.layout.points_list_row, pointsList);
		this.ctx = context;
		this.pointsList = pointsList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.points_list_row, parent, false);
	    Points p = pointsList.get(position);
	    
	    TextView pointsDescription = (TextView) rowView.findViewById(R.id.points_description);
	    pointsDescription.setText(p.getDescription());
	    
	    TextView pointsTime = (TextView) rowView.findViewById(R.id.points_time);
	    pointsTime.setText(p.getTimeAsString());
	    
	    TextView pointsDate = (TextView) rowView.findViewById(R.id.points_date);
	    pointsDate.setText(p.getDateAsString());
	    
	    TextView pointsPoints = (TextView) rowView.findViewById(R.id.points_points);
	    pointsPoints.setText(String.valueOf(p.getPoints()));
	    return rowView;
	}
}
