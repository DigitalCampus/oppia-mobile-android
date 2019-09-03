package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.ActivityType;

import java.util.List;

public class ActivityTypesAdapter extends RecyclerView.Adapter<ActivityTypesAdapter.ViewHolder> {


    private List<ActivityType> activityTypes;
    private Context context;
    private OnItemClickListener itemClickListener;


    public ActivityTypesAdapter(Context context, List<ActivityType> activityTypes) {
        this.context = context;
        this.activityTypes = activityTypes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_activity_type, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position2) {

        final ActivityType activityType = getItemAtPosition(holder.getAdapterPosition());

        holder.tvActivityType.setText(activityType.getName());
        holder.tvActivityType.setTextColor(activityType.getColor());
        holder.imgShowHide.getBackground().setColorFilter(activityType.getColor(), PorterDuff.Mode.SRC_ATOP);
        if (activityType.isEnabled()) {
            holder.imgShowHide.setImageResource(R.drawable.ic_eye_show);
//            holder.imgShowHide.setBackgroundResource(android.R.drawable.btn_default);
            holder.imgShowHide.getBackground().setAlpha(255);
            holder.imgShowHide.setColorFilter(Color.WHITE);
//            holder.imgShowHide.setBackgroundColor(activityType.getColor());
        } else {
            holder.imgShowHide.setImageResource(R.drawable.ic_eye_hide);
            holder.imgShowHide.getBackground().setAlpha(0);
            holder.imgShowHide.setColorFilter(activityType.getColor());
//            holder.imgShowHide.setBackgroundDrawable(null);
        }


    }

    @Override
    public int getItemCount() {
        return activityTypes.size();
    }

    public ActivityType getItemAtPosition(int position) {
        return activityTypes.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvActivityType;
        private final AppCompatImageButton imgShowHide;
        public View rootView;

        public ViewHolder(View itemView) {

            super(itemView);

            tvActivityType = itemView.findViewById(R.id.tv_activity_type);
            imgShowHide = itemView.findViewById(R.id.img_show_hide);

            rootView = itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityType activityType = getItemAtPosition(getAdapterPosition());
                    activityType.setEnabled(!activityType.isEnabled());
                    notifyDataSetChanged();

                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition(), activityType.getType(), activityType.isEnabled());
                    }
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


