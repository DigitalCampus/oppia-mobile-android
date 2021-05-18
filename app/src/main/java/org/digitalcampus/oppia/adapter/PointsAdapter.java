package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowFragmentPointsListBinding;
import org.digitalcampus.oppia.model.Points;

import java.util.List;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.PointsViewHolder> {

    private List<Points> points;
    private Context context;

    public PointsAdapter(Context context, List<Points> points) {
        this.context = context;
        this.points = points;
    }

    @Override
    public PointsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_fragment_points_list, parent, false);

        // Return a new holder instance
        return new PointsViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final PointsViewHolder viewHolder, final int position) {

        final Points point = getItemAtPosition(position);

        viewHolder.binding.pointsDescription.setText(point.getDescriptionPrettified());
        viewHolder.binding.pointsTime.setText(point.getTimeHoursMinutes());
        viewHolder.binding.pointsDate.setText(point.getDateDayMonth());
        viewHolder.binding.pointsPoints.setText(String.valueOf(point.getPointsAwarded()));

    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public Points getItemAtPosition(int position) {
        return points.get(position);
    }


    public class PointsViewHolder extends RecyclerView.ViewHolder {

        private final RowFragmentPointsListBinding binding;

        public PointsViewHolder(View itemView) {

            super(itemView);
            binding = RowFragmentPointsListBinding.bind(itemView);

        }

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}


