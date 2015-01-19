package org.digitalcampus.oppia.adapter;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.DownloadMediaListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.DownloadMediaTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DownloadMediaListAdapter extends ArrayAdapter<Media> {

	public static final String TAG = DownloadMediaListAdapter.class.getSimpleName();

	private final Context ctx;
	private ArrayList<Media> mediaList;

    private ListInnerBtnOnClickListener onClickListener;
	
	public DownloadMediaListAdapter(Activity context, ArrayList<Media> mediaList) {
		super(context, R.layout.media_download_row, mediaList);
		this.ctx = context;
		this.mediaList = mediaList;
	}

    static class DownloadMediaViewHolder{
        TextView mediaTitle;
        TextView mediaFileSize;
        Button downloadBtn;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        DownloadMediaViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.media_download_row, parent, false);
            viewHolder = new DownloadMediaViewHolder();
            viewHolder.mediaTitle = (TextView) convertView.findViewById(R.id.media_title);
            viewHolder.mediaFileSize = (TextView) convertView.findViewById(R.id.media_file_size);
            viewHolder.downloadBtn = (Button) convertView.findViewById(R.id.action_btn);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (DownloadMediaViewHolder) convertView.getTag();
        }

        Media m = mediaList.get(position);

        viewHolder.mediaTitle.setText(m.getFilename());
		if(m.getFileSize() != 0){
            viewHolder.mediaFileSize.setText(ctx.getString(R.string.media_file_size,m.getFileSize()/(1024*1024)));
		} else {
            viewHolder.mediaFileSize.setVisibility(View.GONE);
		}

        viewHolder.downloadBtn.setTag(position); //For passing the list item index
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
                if(onClickListener != null)
                    onClickListener.onClick((Integer) v.getTag());
         	}
         });

		return convertView;
	}

    public void setOnClickListener(ListInnerBtnOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
    /*
	public void showProgressDialog(){
		// show progress dialog
		downloadDialog = new ProgressDialog(ctx);
		downloadDialog.setTitle(R.string.downloading);
		downloadDialog.setMessage(ctx.getString(R.string.download_starting));
		downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadDialog.setProgress(0);
		downloadDialog.setMax(100);
		downloadDialog.setCancelable(false);	
		downloadDialog.show();
	}
	
	public void setDownloadMediaListener(DownloadMediaListener dml) {
        synchronized (this) {
        	mDownloadListener = dml;
        }
    }

	public void downloadProgressUpdate(DownloadProgress msg) {
		if(downloadDialog != null){
			downloadDialog.setMessage(msg.getMessage());
			downloadDialog.setProgress(msg.getProgress());
		}
	}

	public void downloadComplete(Payload response) {
		this.closeDialog();
		this.inProgress = false;
		synchronized (this) {
			if (mDownloadListener != null) {
				mDownloadListener.downloadComplete(response);
			}
		}
	}
	
	public void closeDialog(){
		if (downloadDialog != null){
			downloadDialog.dismiss();
		}
	}
	
	public void openDialog(){
		if (downloadDialog != null && this.inProgress){
			downloadDialog.show();
		}
	}
	*/
}
