package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowTagBinding;
import org.digitalcampus.oppia.model.Tag;

import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {


    private List<Tag> tags;
    private Context context;
    private OnItemClickListener itemClickListener;


    public TagsAdapter(Context context, List<Tag> tags) {
        this.context = context;
        this.tags = tags;
    }

    @Override
    public TagsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_tag, parent, false);

        // Return a new holder instance
        return new TagsViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final TagsViewHolder viewHolder, final int position) {

        final Tag t = getItemAtPosition(position);

        viewHolder.binding.tagName.setText(t.getName());
        viewHolder.binding.tagCount.setText(String.valueOf(t.getCount()));
        if(t.isHighlight()){
            viewHolder.binding.tagName.setTypeface(null, Typeface.BOLD);
        } else {
            viewHolder.binding.tagName.setTypeface(null, Typeface.NORMAL);
        }
        if(t.getDescription() != null && !t.getDescription().trim().equals("") ){
            viewHolder.binding.tagDescription.setText(t.getDescription());
            viewHolder.binding.tagDescription.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.tagDescription.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public Tag getItemAtPosition(int position) {
        return tags.get(position);
    }


    public class TagsViewHolder extends RecyclerView.ViewHolder {

        private final RowTagBinding binding;

        public TagsViewHolder(View itemView) {

            super(itemView);
            binding = RowTagBinding.bind(itemView);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
 

