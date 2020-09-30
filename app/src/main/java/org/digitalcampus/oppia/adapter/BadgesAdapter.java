package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Badge;

import java.util.List;

public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.BadgesViewHolder> {


    private List<Badge> badges;
    private Context context;

    public BadgesAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @Override
    public BadgesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_fragment_badges_list, parent, false);

        // Return a new holder instance
        return new BadgesViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final BadgesViewHolder viewHolder, final int position) {

        final Badge b = getItemAtPosition(position);

        viewHolder.badgeDescription.setText(b.getDescription());
        viewHolder.badgeDate.setText(b.getDateAsString());

    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    public Badge getItemAtPosition(int position) {
        return badges.get(position);
    }


    public class BadgesViewHolder extends RecyclerView.ViewHolder {

        private TextView badgeDescription;
        private TextView badgeDate;

        public BadgesViewHolder(View itemView) {

            super(itemView);

            badgeDescription = itemView.findViewById(R.id.badges_description);
            badgeDate = itemView.findViewById(R.id.badges_date);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}


