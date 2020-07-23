package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.ActivityType;

import java.util.List;

public class ActivityTypesAdapter extends RecyclerView.Adapter<ActivityTypesAdapter.ActivityTypesViewHolder> {


    private List<ActivityType> activityTypes;
    private Context context;
    private OnItemClickListener itemClickListener;


    public ActivityTypesAdapter(Context context, List<ActivityType> activityTypes) {
        this.context = context;
        this.activityTypes = activityTypes;
    }

    @Override
    public ActivityTypesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_activity_type, parent, false);

        // Return a new holder instance
        return new ActivityTypesViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final ActivityTypesViewHolder holder, final int position2) {

        final ActivityType activityType = getItemAtPosition(holder.getAdapterPosition());

        holder.tvActivityType.setText(activityType.getName());
        holder.tvActivityType.setTextColor(activityType.getColor());
        holder.imgShowHide.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(activityType.getColor(), BlendModeCompat.SRC_OVER));
        if (activityType.isEnabled()) {
            holder.imgShowHide.setImageResource(R.drawable.ic_eye_show);
            holder.imgShowHide.getBackground().setAlpha(255);
            holder.imgShowHide.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.WHITE, BlendModeCompat.SRC_ATOP));
        } else {
            holder.imgShowHide.setImageResource(R.drawable.ic_eye_hide);
            holder.imgShowHide.getBackground().setAlpha(0);
            holder.imgShowHide.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(activityType.getColor(), BlendModeCompat.SRC_ATOP));
        }
    }

    @Override
    public int getItemCount() {
        return activityTypes.size();
    }

    public ActivityType getItemAtPosition(int position) {
        return activityTypes.get(position);
    }


    public class ActivityTypesViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvActivityType;
        private final AppCompatImageButton imgShowHide;

        public ActivityTypesViewHolder(View itemView) {

            super(itemView);

            tvActivityType = itemView.findViewById(R.id.tv_activity_type);
            imgShowHide = itemView.findViewById(R.id.img_show_hide);

            itemView.setOnClickListener(v -> {
                ActivityType activityType = getItemAtPosition(getAdapterPosition());
                activityType.setEnabled(!activityType.isEnabled());
                notifyDataSetChanged();

                if (itemClickListener != null) {
                    itemClickListener.onItemClick(getAdapterPosition(), activityType.getType(), activityType.isEnabled());
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String type, boolean enabled);
    }
}


