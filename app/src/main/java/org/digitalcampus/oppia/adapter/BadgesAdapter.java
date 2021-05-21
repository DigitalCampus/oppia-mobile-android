package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowFragmentBadgesListBinding;
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

        viewHolder.binding.badgesDescription.setText(b.getDescription());
        viewHolder.binding.badgesDate.setText(b.getDateAsString());

    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    public Badge getItemAtPosition(int position) {
        return badges.get(position);
    }


    public class BadgesViewHolder extends RecyclerView.ViewHolder {

        private final RowFragmentBadgesListBinding binding;

        public BadgesViewHolder(View itemView) {

            super(itemView);

            binding = RowFragmentBadgesListBinding.bind(itemView);

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}


