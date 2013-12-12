package org.digitalcampus.oppia.adapter;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.DownloadMediaListener;
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

public class DownloadMediaListAdapter extends ArrayAdapter<Media> implements DownloadMediaListener {

	public static final String TAG = DownloadMediaListAdapter.class.getSimpleName();

	private final Context ctx;
	private ArrayList<Media> mediaList;
	private DownloadMediaTask task;
	private ProgressDialog downloadDialog;
	private DownloadMediaListener mDownloadListener;
	private boolean inProgress = false;
	
	public DownloadMediaListAdapter(Activity context, ArrayList<Media> mediaList) {
		super(context, R.layout.media_download_row, mediaList);
		this.ctx = context;
		this.mediaList = mediaList;
	}

	public void setMediaList(ArrayList<Media> mediaList){
		this.mediaList = mediaList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.media_download_row, parent, false);
		Media m = mediaList.get(position);
		rowView.setTag(m);
		TextView mediaTitle = (TextView) rowView.findViewById(R.id.media_title);
		mediaTitle.setText(m.getFilename());
		TextView mediaFileSize = (TextView) rowView.findViewById(R.id.media_file_size);
		if(m.getFileSize() != 0){
			mediaFileSize.setText(ctx.getString(R.string.media_file_size,m.getFileSize()/(1024*1024)));
		} else {
			mediaFileSize.setVisibility(View.GONE);
		}
		
		
		Button downloadBtn = (Button) rowView.findViewById(R.id.action_btn);
		downloadBtn.setTag(m);
		downloadBtn.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		Media m = (Media) v.getTag();
         		DownloadMediaListAdapter.this.download(m);
         	}
         });
		return rowView;
	}

	private void download(Media media) {
		if(!ConnectionUtils.isOnWifi(ctx)){
			UIUtils.showAlert(ctx, R.string.warning, R.string.warning_wifi_required);
			return;
		}

		// show progress dialog
		this.showProgressDialog();
		this.inProgress = true;
		
		ArrayList<Media> alMedia = new ArrayList<Media>();
		alMedia.add(media);
		task = new DownloadMediaTask(ctx);
		Payload p = new Payload(alMedia);
		task.setDownloadListener(this);
		task.execute(p);
	}
	
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
}
