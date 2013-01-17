package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.model.Points;

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
	    pointsTime.setText(p.getTime());
	    
	    TextView pointsDate = (TextView) rowView.findViewById(R.id.points_date);
	    pointsDate.setText(p.getDate());
	    
	    TextView pointsPoints = (TextView) rowView.findViewById(R.id.points_points);
	    pointsPoints.setText(String.valueOf(p.getPoints()));
	    return rowView;
	}
}
