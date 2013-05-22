package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.listener.DownloadMediaListener;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.model.Media;
import org.digitalcampus.mobile.learning.task.DownloadMediaTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.utils.ConnectionUtils;
import org.digitalcampus.mobile.learning.utils.UIUtils;

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
	private final ArrayList<Media> mediaList;
	private DownloadMediaTask task;
	private ProgressDialog downloadDialog;
	private DownloadMediaListener mDownloadListener;
	
	public DownloadMediaListAdapter(Activity context, ArrayList<Media> mediaList) {
		super(context, R.layout.media_download_row, mediaList);
		this.ctx = context;
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
		downloadDialog = new ProgressDialog(ctx);
		downloadDialog.setTitle(R.string.downloading);
		downloadDialog.setMessage(ctx.getString(R.string.download_starting));
		downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadDialog.setProgress(0);
		downloadDialog.setMax(100);
		downloadDialog.setCancelable(false);
		downloadDialog.show();

		ArrayList<Media> alMedia = new ArrayList<Media>();
		alMedia.add(media);
		task = new DownloadMediaTask(ctx);
		Payload p = new Payload(0, alMedia);
		task.setDownloadListener(this);
		task.execute(p);
	}
	
	public void setDownloadMediaListener(DownloadMediaListener dml) {
        synchronized (this) {
        	mDownloadListener = dml;
        }
    }

	public void downloadProgressUpdate(DownloadProgress msg) {
		downloadDialog.setMessage(msg.getMessage());
		downloadDialog.setProgress(msg.getProgress());
	}

	public void downloadComplete(Payload response) {
		downloadDialog.cancel();
		synchronized (this) {
			if (mDownloadListener != null) {
				mDownloadListener.downloadComplete(response);
			}
		}
	}
	
	public void closeDialogs(){
		if (downloadDialog != null){
			downloadDialog.dismiss();
		}
	}
}
