package org.digitalcampus.mobile.learning.activity;

import java.io.IOException;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.ModuleMetaPage;
import org.digitalcampus.mobile.learning.utils.FileUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class ModuleMetaPageActivity extends AppActivity {

	public static final String TAG = ModuleMetaPageActivity.class.getSimpleName();
	private Module module;
	private SharedPreferences prefs;
	private int pageid;
	private ModuleMetaPage mmp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_metapage);
		this.drawHeader();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			module = (Module) bundle.getSerializable(Module.TAG);
			setTitle(module.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
			pageid = (Integer) bundle.getSerializable(ModuleMetaPage.TAG);
			mmp = module.getMetaPage(pageid);
			Log.d(TAG,prefs.getString("prefLanguage", Locale.getDefault().getLanguage()));
			Log.d(TAG,mmp.getLang(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())).getTitle());
			Log.d(TAG,mmp.getLang(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())).getContent());
		}
		
		TextView titleTV = (TextView) findViewById(R.id.module_title);
		String title = module.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())) + ": " + mmp.getLang(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())).getTitle();
		titleTV.setText(title);
		
		TextView versionTV = (TextView) findViewById(R.id.module_versionid);
		versionTV.setText(module.getProps().get("versionid"));
		
		TextView shortnameTV = (TextView) findViewById(R.id.module_shortname);
		shortnameTV.setText(module.getProps().get("shortname"));
		
		WebView wv = (WebView) this.findViewById(R.id.metapage_webview);
		wv.setBackgroundColor(0x00000000);
		String url = module.getLocation() + "/" +mmp.getLang(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())).getContent();
		
		try {
			String content =  "<html><head>";
			content += "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
			content += "<link href='file:///android_asset/www/style.css' rel='stylesheet' type='text/css'/>";
			content += "</head>";
			content += FileUtils.readFile(url);
			content += "</html>";
			wv.loadDataWithBaseURL("file://" + module.getLocation() + "/", content, "text/html", "utf-8", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			wv.loadUrl("file://" + url);
		}
	    
	}
	
}
