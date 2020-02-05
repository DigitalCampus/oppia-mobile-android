package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Badges;

import java.util.List;

public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.ViewHolder> {


    private List<Badges> badges;
    private Context context;
    private OnItemClickListener itemClickListener;

    public BadgesAdapter(Context context, List<Badges> badges) {
        this.context = context;
        this.badges = badges;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_fragment_badges_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final Badges b = getItemAtPosition(position);

        viewHolder.badgeDescription.setText(b.getDescription());
        viewHolder.badgeDate.setText(b.getDateAsString());

    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    public Badges getItemAtPosition(int position) {
        return badges.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        private TextView badgeDescription;
        private TextView badgeDate;

        public ViewHolder(View itemView) {

            super(itemView);

            badgeDescription = itemView.findViewById(R.id.badges_description);
            badgeDate = itemView.findViewById(R.id.badges_date);

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


