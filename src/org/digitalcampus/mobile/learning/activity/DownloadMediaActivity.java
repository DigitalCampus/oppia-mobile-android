package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.listener.DownloadMediaListener;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.model.Media;
import org.digitalcampus.mobile.learning.task.DownloadMediaTask;
import org.digitalcampus.mobile.learning.task.Payload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class DownloadMediaActivity extends Activity implements DownloadMediaListener{

	public static final String TAG = "DownloadMediaActivity";
	private ArrayList<Object> missingMedia = new ArrayList<Object>();
	private ProgressDialog pDialog;
	private DownloadMediaTask task;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_media);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			missingMedia = (ArrayList<Object>) bundle.getSerializable(DownloadMediaActivity.TAG);
		}
		
		WebView wv = (WebView) findViewById(R.id.download_media_webview);
		String url = "file:///android_asset/www/download_media.html";
		wv.loadUrl(url);
		
		WebView wvml = (WebView) findViewById(R.id.download_media_list_webview);
		String strData = "<ul>";
		for(Object o: missingMedia){
			Media m = (Media) o;
			strData += "<li>"+m.getFilename()+"</li>";
		}
		strData += "</ul>";
		wvml.loadData(strData,"text/html","utf-8");
		Button btn = (Button) this.findViewById(R.id.download_media_btn);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				download();
			}
		});
	}

	private void download(){
		// TODO check the user is on a wifi network connection
		// TODO how to kill the task...
		// show progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setTitle(R.string.downloading);
		pDialog.setMessage(getString(R.string.download_starting));
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDialog.setProgress(0);
		pDialog.setMax(100);
		pDialog.setCancelable(false);
		pDialog.show();
		
	
		task = new DownloadMediaTask(this);
		Payload p = new Payload(0,missingMedia);
		task.setDownloadListener(this);
		task.execute(p);
		/*pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				task.cancel(true);
			}
		});*/
	}
	
	public void downloadStarting() {
		Log.d(TAG,"download starting");
	}

	public void downloadProgressUpdate(DownloadProgress msg) {
		pDialog.setMessage(msg.getMessage());
		pDialog.setProgress(msg.getProgress());
	}

	public void downloadComplete() {
		pDialog.cancel();
		this.finish();
	}
	
	
}
