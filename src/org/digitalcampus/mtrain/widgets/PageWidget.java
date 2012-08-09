package org.digitalcampus.mtrain.widgets;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.application.Tracker;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PageWidget extends WidgetFactory {

	private static final String TAG = "PageWidget";

	private Context ctx;
	private Module module;
	private org.digitalcampus.mtrain.model.Activity activity;

	public PageWidget(Context context, Module module, org.digitalcampus.mtrain.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.activity = activity;
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		super.getLayout().addView(vv);

		// get the location data
		WebView wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		// TODO error check here that the file really exists first
		// TODO error check that location is in the hashmap
		String url = "file://" + module.getLocation() + "/" + activity.getActivityData().get("location");

		Log.v(TAG, "Loading: " + url);
		wv.loadUrl(url);

		// Track the page
		Tracker t = new Tracker(this.ctx);
		t.activityComplete(module.getModId(), activity.getDigest());
		
		// set up the page to intercept videos
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.contains("/video/")) {
					Log.d(TAG, "Intercepting click on video url: " + url);
					// extract video name from url
					int startPos = url.indexOf("/video/") + 7;
					String videoFileName = url.substring(startPos, url.length());
					Log.d(TAG, videoFileName);

					// check video file exists
					boolean exists = FileUtils.mediaFileExists(videoFileName);
					if (exists) {
						// launch intent to play video
						Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
						Uri data = Uri.parse(MTrain.MEDIA_PATH + videoFileName);
						// TODO check that the file really is video/mp4 and not another video type
						intent.setDataAndType(data, "video/mp4");
						// TODO track that the video has been played (or at least clicked on)
						Tracker t = new Tracker(ctx);
						t.mediaPlayed(PageWidget.this.module.getModId(), PageWidget.this.activity.getDigest(), videoFileName);
						ctx.startActivity(intent);
					} else {
						Toast.makeText(ctx, "Media file: '" + videoFileName + "' not found.", Toast.LENGTH_LONG).show();
					}
					return true;
				} else {
					Log.d(TAG, "Not doing anything with click");
					// return false so should not override url loading
					return false;
				}

			}
		});
	}

}
