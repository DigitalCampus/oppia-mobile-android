/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.DownloadMediaListAdapter;
import org.digitalcampus.oppia.listener.DownloadCompleteListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.DownloadMediaTask;
import org.digitalcampus.oppia.task.DownloadTasksController;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class DownloadMediaActivity extends AppActivity implements DownloadCompleteListener {

	public static final String TAG = DownloadMediaActivity.class.getSimpleName();

    private SharedPreferences prefs;
    private ArrayList<Media> missingMedia;
	private DownloadMediaListAdapter dmla;

    private DownloadTasksController tasksController;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_media);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			missingMedia = (ArrayList<Media>) bundle.getSerializable(DownloadMediaActivity.TAG);
		}
        else{
            missingMedia = new ArrayList<Media>();
        }

		dmla = new DownloadMediaListAdapter(this, missingMedia);
        dmla.setOnClickListener(new DownloadMediaListener());

        ListView listView = (ListView) findViewById(R.id.missing_media_list);
		listView.setAdapter(dmla);
		
		Button downloadViaPCBtn = (Button) this.findViewById(R.id.download_media_via_pc_btn);
		downloadViaPCBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				downloadViaPC();
			}
		});

		Editor e = prefs.edit();
		e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
		e.commit();
	}
	
	@Override
	public void onResume(){
		super.onResume();
        if ((missingMedia != null) && missingMedia.size()>0) {
            //We already have loaded media (coming from orientationchange)
            dmla.notifyDataSetChanged();
        }
        if (tasksController == null){
            tasksController = new DownloadTasksController(this, prefs);
        }
        tasksController.setOnDownloadCompleteListener(this);
        tasksController.setCtx(this);
	}

    @Override
    public void onDestroy(){
        tasksController.setOnDownloadCompleteListener(null);
        tasksController.setCtx(null);
        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Media> savedMissingMedia = (ArrayList<Media>) savedInstanceState.getSerializable(TAG);
        this.missingMedia.clear();
        this.missingMedia.addAll(savedMissingMedia);
        tasksController = (DownloadTasksController) savedInstanceState.getParcelable("tasksProgress");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(TAG, missingMedia);
        if (tasksController != null){
            tasksController.setOnDownloadCompleteListener(null);
            savedInstanceState.putParcelable("tasksProgress", tasksController);
        }
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
			UIUtils.showAlert(this, R.string.info, this.getString(R.string.download_via_pc_message,filename));
		} catch (FileNotFoundException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
		} catch (IOException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
		}
	}

    @Override
    public void onComplete(Payload response) {
        if (response.isResult()){
            missingMedia.remove((Media) response.getData().get(0));
            dmla.notifyDataSetChanged();
            UIUtils.showAlert(this, R.string.info,response.getResultResponse());
        } else {
            UIUtils.showAlert(this, R.string.error,response.getResultResponse());
        }
    }

    private class DownloadMediaListener implements ListInnerBtnOnClickListener {
        @Override
        public void onClick(int position) {

            if(!ConnectionUtils.isOnWifi(DownloadMediaActivity.this)){
                UIUtils.showAlert(DownloadMediaActivity.this, R.string.warning, R.string.warning_wifi_required);
                return;
            }
            Log.d("media-download", "Clicked " + position);
            Media mediaToDownload = missingMedia.get(position);
            if (!tasksController.isTaskInProgress()){
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(mediaToDownload);
                Payload p = new Payload(data);

                DownloadMediaTask task = new DownloadMediaTask(DownloadMediaActivity.this);
                task.setDownloadListener(tasksController);
                tasksController.setTaskInProgress(true);
                tasksController.showDialog();
                task.execute(p);
            }
        }
    }

}
