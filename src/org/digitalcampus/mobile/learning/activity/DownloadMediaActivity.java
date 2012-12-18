package org.digitalcampus.mobile.learning.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.DownloadMediaListener;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.model.Media;
import org.digitalcampus.mobile.learning.task.DownloadMediaTask;
import org.digitalcampus.mobile.learning.task.Payload;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
		wv.setBackgroundColor(0x00000000);
		// hack to get transparent background on webviews
		if (Build.VERSION.SDK_INT >= 11){ // Android v3.0+
			try {
				Method method = View.class.getMethod("setLayerType", int.class, Paint.class);
				method.invoke(wv, 1, new Paint()); // 1 = LAYER_TYPE_SOFTWARE (API11)
			} catch (Exception e) {
				
			}
		}
		String url = "file:///android_asset/www/download_media.html";
		wv.loadUrl(url);
		
		WebView wvml = (WebView) findViewById(R.id.download_media_list_webview);
		wvml.setBackgroundColor(0x00000000);
		// hack to get transparent background on webviews
		if (Build.VERSION.SDK_INT >= 11){ // Android v3.0+
			try {
				Method method = View.class.getMethod("setLayerType", int.class, Paint.class);
				method.invoke(wvml, 1, new Paint()); // 1 = LAYER_TYPE_SOFTWARE (API11)
			} catch (Exception e) {
				
			}
		}
		String strData = "<ul>";
		for(Object o: missingMedia){
			Media m = (Media) o;
			strData += "<li>"+m.getFilename()+"</li>";
		}
		strData += "</ul>";
		wvml.loadData(strData,"text/html","utf-8");
		Button downloadBtn = (Button) this.findViewById(R.id.download_media_btn);
		downloadBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				download();
			}
		});
		Button downloadViaPCBtn = (Button) this.findViewById(R.id.download_media_via_pc_btn);
		downloadViaPCBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				downloadViaPC();
			}
		});
	}

	private void download(){
		//check the user is on a wifi network connection
		ConnectivityManager conMan = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo == null || netInfo.getType() != ConnectivityManager.TYPE_WIFI){
			MobileLearning.showAlert(this, R.string.warning, R.string.warning_wifi_required);
			return;
		}
		
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
	}
	
	private void downloadViaPC(){
		String filename = "mobile-learning-media.html";
		String strData = "<html>";
		strData += "<head><title>"+this.getString(R.string.download_via_pc_title)+"</title></head>";
		strData += "<body>";
		strData += "<h3>"+this.getString(R.string.download_via_pc_title)+"</h3>";
		strData += "<p>"+this.getString(R.string.download_via_pc_intro)+"</p>";
		strData += "<ul>";
		for(Object o: missingMedia){
			Media m = (Media) o;
			strData += "<li><a href='"+m.getDownloadUrl()+"'>"+m.getFilename()+"</a></li>";
		}
		strData += "</ul>";
		strData += "</body></html>";
		strData += "<p>"+this.getString(R.string.download_via_pc_final,"/digitalcampus/media/")+"</p>";
		
		File file = new File(Environment.getExternalStorageDirectory(),filename);
		try {
			FileOutputStream f = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(new FileOutputStream(file));
			out.write(strData);
			out.close();
			f.close();
			MobileLearning.showAlert(this, R.string.info, this.getString(R.string.download_via_pc_message,filename));
		} catch (FileNotFoundException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
		} catch (IOException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
		}
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
