package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Media;

import java.util.List;

public class DownloadMediaAdapter extends RecyclerView.Adapter<DownloadMediaAdapter.ViewHolder> {


    private List<Media> medias;
    private Context context;
    private OnItemClickListener itemClickListener;


    public DownloadMediaAdapter(Context context, List<Media> medias) {
        this.context = context;
        this.medias = medias;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_media, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Media media = getItemAtPosition(position);

        //        holder.tvName.setText(media.getName());
        //
        //        Picasso.with(context)
        //                .load(media.getLogo_url())
        ////                .placeholder(R.mipmap.img_default_grid)
        //                .error(R.mipmap.ic_mes_v2_144)
        //                .resizeDimen(R.dimen.width_image_small, R.dimen.height_image_small)
        //                .into(holder.imgEntity);



    }

    @Override
    public int getItemCount() {
        return medias.size();
    }

    public Media getItemAtPosition(int position) {
        return medias.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;

        public ViewHolder(View itemView) {

            super(itemView);

            //            tvName = (TextView) itemView.findViewById(R.id.tv_name);

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


