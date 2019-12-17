package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Points;

import java.util.List;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {


    private List<Points> points;
    private Context context;
    private OnItemClickListener itemClickListener;


    public PointsAdapter(Context context, List<Points> points) {
        this.context = context;
        this.points = points;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.fragment_points_list_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final Points point = getItemAtPosition(position);

        viewHolder.pointsDescription.setText(point.getDescription());
        viewHolder.pointsTime.setText(point.getTimeHoursMinutes());
        viewHolder.pointsDate.setText(point.getDateDayMonth());
        viewHolder.pointsPoints.setText(String.valueOf(point.getPoints()));

    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public Points getItemAtPosition(int position) {
        return points.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        private TextView pointsDescription;
        private TextView pointsTime;
        private TextView pointsDate;
        private TextView pointsPoints;

        public ViewHolder(View itemView) {

            super(itemView);

            pointsDescription = itemView.findViewById(R.id.points_description);
            pointsTime = itemView.findViewById(R.id.points_time);
            pointsDate = itemView.findViewById(R.id.points_date);
            pointsPoints = itemView.findViewById(R.id.points_points);

            rootView = itemView;
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}


