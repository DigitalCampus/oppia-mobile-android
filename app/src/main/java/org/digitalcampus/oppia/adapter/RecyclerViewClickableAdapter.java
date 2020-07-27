package org.digitalcampus.oppia.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerViewClickableAdapter<H extends RecyclerViewClickableAdapter.ViewHolder> extends RecyclerView.Adapter<H> {

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }

}
