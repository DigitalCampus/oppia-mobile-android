package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
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

        viewHolder.tagName.setText(t.getName());
        viewHolder.tagCount.setText(String.valueOf(t.getCount()));
        if(t.isHighlight()){
            viewHolder.tagName.setTypeface(null, Typeface.BOLD);
        } else {
            viewHolder.tagName.setTypeface(null, Typeface.NORMAL);
        }
        if(t.getDescription() != null && !t.getDescription().trim().equals("") ){
            viewHolder.tagDescription.setText(t.getDescription());
            viewHolder.tagDescription.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tagDescription.setVisibility(View.GONE);
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
        
        private TextView tagName;
        private TextView tagDescription;
        private TextView tagCount;

        public TagsViewHolder(View itemView) {

            super(itemView);

            tagName = itemView.findViewById(R.id.tag_name);
            tagDescription = itemView.findViewById(R.id.tag_description);
            tagCount = itemView.findViewById(R.id.tag_count);

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
 

