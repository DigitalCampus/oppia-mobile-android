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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Points;

import java.util.List;

public class PointsListAdapter extends ArrayAdapter<Points>{

	public static final String TAG = PointsListAdapter.class.getSimpleName();
	
	private final Context ctx;
	private final List<Points> pointsList;
	
	public PointsListAdapter(Activity context, List<Points> pointsList) {
		super(context, R.layout.fragment_points_list_row, pointsList);
		this.ctx = context;
		this.pointsList = pointsList;
	}

    static class PointsViewHolder{
        TextView pointsDescription;
        TextView pointsTime;
        TextView pointsDate;
        TextView pointsPoints;

    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        PointsViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.fragment_points_list_row, parent, false);
            viewHolder = new PointsViewHolder();
            viewHolder.pointsDescription = convertView.findViewById(R.id.points_description);
            viewHolder.pointsTime = convertView.findViewById(R.id.points_time);
            viewHolder.pointsDate = convertView.findViewById(R.id.points_date);
            viewHolder.pointsPoints = convertView.findViewById(R.id.points_points);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (PointsViewHolder) convertView.getTag();
        }

	    Points p = pointsList.get(position);
        viewHolder.pointsDescription.setText(p.getDescription());
        viewHolder.pointsTime.setText(p.getTimeAsString());
        viewHolder.pointsDate.setText(p.getDateAsString());
        viewHolder.pointsPoints.setText(String.valueOf(p.getPoints()));

	    return convertView;
	}
}
