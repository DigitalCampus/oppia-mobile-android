package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;

import java.util.List;

public class DevicesBTAdapter extends RecyclerView.Adapter<DevicesBTAdapter.ViewHolder> {


    private List<String> devices;
    private Context context;
    private OnItemClickListener itemClickListener;


    public DevicesBTAdapter(Context context, List<String> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_device, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final String device = getItemAtPosition(position);

        holder.rootView.setText(device);

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public String getItemAtPosition(int position) {
        return devices.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView rootView;

        public ViewHolder(View itemView) {

            super(itemView);

            rootView = (TextView) itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, getAdapterPosition());
                    }
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


