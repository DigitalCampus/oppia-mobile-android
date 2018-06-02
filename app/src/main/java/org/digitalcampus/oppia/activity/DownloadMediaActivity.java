/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.splunk.mint.Mint;

import org.apache.commons.io.output.TaggedOutputStream;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.DownloadMediaListAdapter;
import org.digitalcampus.oppia.listener.DownloadMediaListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadBroadcastReceiver;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class DownloadMediaActivity extends AppActivity implements DownloadMediaListener {

	public static final String TAG = DownloadMediaActivity.class.getSimpleName();

    private SharedPreferences prefs;
    private ArrayList<Media> missingMedia;
	private DownloadMediaListAdapter dmla;
    private DownloadBroadcastReceiver receiver;
    Button downloadViaPCBtn;
    private TextView emptyState;
    private boolean isSortByCourse;
    private TextView downloadSelected;
    private TextView unselectAll;
    private View missingMediaContainer;
    private ListView mediaList;
    private ArrayList<Media> mediaSelected;

    public enum DownloadMode { INDIVIDUALLY, DOWNLOAD_ALL, STOP_ALL }
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_media);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			missingMedia = (ArrayList<Media>) bundle.getSerializable(DownloadMediaActivity.TAG);
		}
        else{
            missingMedia = new ArrayList<>();
        }

        mediaSelected = new ArrayList<>();

		dmla = new DownloadMediaListAdapter(this, missingMedia);
        dmla.setOnClickListener(new DownloadMediaListener());

        mediaList = (ListView) findViewById(R.id.missing_media_list);
		mediaList.setAdapter(dmla);

        mediaList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        mediaList.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                Log.v(TAG, "Count: " + mediaList.getCheckedItemCount());
                if(checked){
                    mediaSelected.add(missingMedia.get(position));
                }else{
                    mediaSelected.remove(missingMedia.get(position));
                }

                int count = mediaSelected.size();
                mode.setSubtitle(count == 1 ? count + " item selected" : count + " items selected");

                for(Media m: mediaSelected){
                    if(!m.isDownloading()){
                        downloadSelected.setText(getString(R.string.missing_media_download_selected));
                        break;
                    }
                }

            }

            @Override
            public boolean onCreateActionMode(final ActionMode mode, Menu menu) {

                onPrepareOptionsMenu(menu);
                mode.setTitle(R.string.title_download_media);

                if (missingMediaContainer.getVisibility() != View.VISIBLE){
                    missingMediaContainer.setVisibility(View.VISIBLE);
                    downloadSelected.setOnClickListener(new OnClickListener() {

                        public void onClick(View v) {
                            DownloadMode downloadMode = downloadSelected.getText()
                                    .equals(getString(R.string.missing_media_download_selected)) ? DownloadMode.DOWNLOAD_ALL
                                                                                                 : DownloadMode.STOP_ALL;
                            downloadSelected.setText(downloadSelected.getText()
                                    .equals(getString(R.string.missing_media_download_selected)) ? getString(R.string.missing_media_stop_selected)
                                                                                                 : getString(R.string.missing_media_download_selected));

                            for(Media m : mediaSelected){
                                downloadMedia(m, downloadMode);
                            }

                            mode.finish();
                        }
                    });
                    unselectAll.setOnClickListener(new OnClickListener() {

                        public void onClick(View view) {
                            mode.finish();
                        }
                    });
                    showDownloadMediaMessage();
                }

                dmla.setEnterOnMultiChoiceMode(true);
                dmla.notifyDataSetChanged();

                downloadSelected.setText(getString(R.string.missing_media_stop_selected));
                unselectAll.setText(getString(R.string.missing_media_unselect_all));
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {return false; }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_sort_by: {
                        if(isSortByCourse){
                            dmla.sortByFilename();
                            isSortByCourse = false;
                            item.setTitle(getString(R.string.menu_sort_by_course));
                        }else{
                            dmla.sortByCourse();
                            isSortByCourse = true;
                            item.setTitle(getString(R.string.menu_sort_by_filename));
                        }
                        invalidateOptionsMenu();
                        return true;
                    }
                    case R.id.menu_select_all:
                        for(int i= 0; i < mediaList.getAdapter().getCount(); i++){
                            if(!mediaList.isItemChecked(i)) {
                                mediaList.setItemChecked(i, true);
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mediaSelected.clear();
                hideDownloadMediaMessage();
                dmla.setEnterOnMultiChoiceMode(false);
                dmla.notifyDataSetChanged();
            }
        });

        missingMediaContainer = this.findViewById(R.id.home_messages);
        downloadSelected = (TextView) this.findViewById(R.id.download_selected);
        unselectAll = (TextView) this.findViewById(R.id.unselect_all);

		
		downloadViaPCBtn = (Button) this.findViewById(R.id.download_media_via_pc_btn);
		downloadViaPCBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                downloadViaPC();
            }
        });

		Media.resetMediaScan(prefs);

        emptyState = (TextView) findViewById(R.id.empty_state);
	}
	
	@Override
	public void onResume(){
		super.onResume();
        if ((missingMedia != null) && missingMedia.size()>0) {
            //We already have loaded media (coming from orientationchange)
            dmla.sortByFilename();
            isSortByCourse = false;
            dmla.notifyDataSetChanged();
            emptyState.setVisibility(View.GONE);
            downloadViaPCBtn.setVisibility(View.VISIBLE);
        }else{
            emptyState.setVisibility(View.VISIBLE);
            downloadViaPCBtn.setVisibility(View.GONE);
        }
        receiver = new DownloadBroadcastReceiver();
        receiver.setMediaListener(this);
        IntentFilter broadcastFilter = new IntentFilter(DownloadService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);

        invalidateOptionsMenu();
	}

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Media> savedMissingMedia = (ArrayList<Media>) savedInstanceState.getSerializable(TAG);
        this.missingMedia.clear();
        this.missingMedia.addAll(savedMissingMedia);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(TAG, missingMedia);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.missing_media_sortby, menu);
        MenuItem selectAll = menu.findItem(R.id.menu_select_all);
        if(selectAll != null) {
            selectAll.setVisible(missingMedia.size() != 0);
        }

        MenuItem sortBy = menu.findItem(R.id.menu_sort_by);
        if(sortBy != null){
            sortBy.setVisible(missingMedia.size() != 0);
            sortBy.setTitle(isSortByCourse ? getString(R.string.menu_sort_by_filename)
                    : getString(R.string.menu_sort_by_course));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch(itemId){
            case R.id.menu_sort_by: {
                if(isSortByCourse){
                    dmla.sortByFilename();
                    isSortByCourse = false;
                }else{
                    dmla.sortByCourse();
                    isSortByCourse = true;
                }
                invalidateOptionsMenu();
                return true;
            }
            case R.id.menu_select_all:
                for(int i= 0; i < mediaList.getAdapter().getCount(); i++){
                    if(!mediaList.isItemChecked(i)) {
                        mediaList.setItemChecked(i, true);
                    }
                }
                return true;
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

	private void downloadViaPC(){

        if (Storage.getStorageStrategy().getStorageType().equals(PrefsActivity.STORAGE_OPTION_INTERNAL)){
            UIUtils.showAlert(this, R.string.prefStorageLocation, this.getString(R.string.download_via_pc_extenal_storage));
            return;
        }
        FileOutputStream f = null;
        Writer out = null;
        try {
            String filename = "oppia-media.html";
            String path = ExternalStorageStrategy.getInternalBasePath(this);
            InputStream input = this.getAssets().open("templates/download_via_pc.html");
            String html = FileUtils.readFile(input);

            html = html.replace("##page_title##", getString(R.string.download_via_pc_title));
            html = html.replace("##app_name##", getString(R.string.app_name));
            html = html.replace("##primary_color##", "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.highlight_light) & 0x00ffffff));
            html = html.replace("##secondary_color##", "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.highlight_dark) & 0x00ffffff));
            html = html.replace("##download_via_pc_title##", getString(R.string.download_via_pc_title));
            html = html.replace("##download_via_pc_intro##", getString(R.string.download_via_pc_intro));
            html = html.replace("##download_via_pc_final##", getString(R.string.download_via_pc_final, path));

            String downloadData = "";
            for(Media m : missingMedia){
                downloadData += "<li><a href='"+m.getDownloadUrl()+"'>"+m.getFilename()+"</a></li>";
            }
            html = html.replace("##download_files##", downloadData);

		    File file = new File(Environment.getExternalStorageDirectory(),filename);
			f = new FileOutputStream(file);
			out = new OutputStreamWriter(new FileOutputStream(file));
			out.write(html);
			out.close();
			f.close();
			UIUtils.showAlert(this, R.string.info, this.getString(R.string.download_via_pc_message,filename));
		} catch (FileNotFoundException fnfe) {
			Mint.logException(fnfe);
            Log.d(TAG, "File not found", fnfe);
		} catch (IOException ioe) {
			Mint.logException(ioe);
            Log.d(TAG, "IOException", ioe);
		} finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "couldn't close Writer object", ioe);
                }
            }
            if(f != null){
                try {
                    f.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "couldn't close FileOutputStream object", ioe);
                }
            }
        }
	}

    @Override
    public void onDownloadProgress(String fileUrl, int progress) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null){
            mediaFile.setProgress(progress);
            dmla.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadFailed(String fileUrl, String message) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            mediaFile.setDownloading(false);
            mediaFile.setProgress(0);
            dmla.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadComplete(String fileUrl) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null){
            Toast.makeText(this,  this.getString(R.string.download_complete), Toast.LENGTH_LONG).show();

            missingMedia.remove(mediaFile);
            dmla.notifyDataSetChanged();
            emptyState.setVisibility((missingMedia.size()==0) ? View.VISIBLE : View.GONE);
            downloadViaPCBtn.setVisibility((missingMedia.size()==0) ? View.GONE : View.VISIBLE);
            invalidateOptionsMenu();
        }
    }

    private Media findMedia(String fileUrl){
        if ( missingMedia.size()>0){
            for (Media mediaFile : missingMedia){
                if (mediaFile.getDownloadUrl().equals(fileUrl)){
                    return mediaFile;
                }
            }
        }
        return null;
    }

    private void downloadMedia(Media mediaToDownload, DownloadMode mode){
        if(!ConnectionUtils.isOnWifi(DownloadMediaActivity.this) && !DownloadMediaActivity.this.prefs.getBoolean(PrefsActivity.PREF_BACKGROUND_DATA_CONNECT, false)){
            UIUtils.showAlert(DownloadMediaActivity.this, R.string.warning, R.string.warning_wifi_required);
            return;
        }

        if(!mediaToDownload.isDownloading()){
            if(mode.equals(DownloadMode.DOWNLOAD_ALL) ||
                    mode.equals(DownloadMode.INDIVIDUALLY)) {
                startDownload(mediaToDownload);
            }
        }else{
            if(mode.equals(DownloadMode.STOP_ALL) ||
                mode.equals(DownloadMode.INDIVIDUALLY)) {
                 stopDownload(mediaToDownload);
            }
        }


    }

    private void startDownload(Media mediaToDownload){
        Intent mServiceIntent = new Intent(DownloadMediaActivity.this, DownloadService.class);
        mServiceIntent.putExtra(DownloadService.SERVICE_ACTION, DownloadService.ACTION_DOWNLOAD);
        mServiceIntent.putExtra(DownloadService.SERVICE_URL, mediaToDownload.getDownloadUrl());
        mServiceIntent.putExtra(DownloadService.SERVICE_DIGEST, mediaToDownload.getDigest());
        mServiceIntent.putExtra(DownloadService.SERVICE_FILENAME, mediaToDownload.getFilename());
        DownloadMediaActivity.this.startService(mServiceIntent);

        mediaToDownload.setDownloading(true);
        mediaToDownload.setProgress(0);
        dmla.notifyDataSetChanged();

        downloadSelected.setText(getString(R.string.missing_media_download_selected));
        for(Media m: mediaSelected){
            if(m.isDownloading()){
                downloadSelected.setText(getString(R.string.missing_media_stop_selected));
                break;
            }
        }
    }
    private void stopDownload(Media mediaToDownload){
        Intent mServiceIntent = new Intent(DownloadMediaActivity.this, DownloadService.class);
        mServiceIntent.putExtra(DownloadService.SERVICE_ACTION, DownloadService.ACTION_CANCEL);
        mServiceIntent.putExtra(DownloadService.SERVICE_URL, mediaToDownload.getDownloadUrl());
        DownloadMediaActivity.this.startService(mServiceIntent);

        mediaToDownload.setDownloading(false);
        mediaToDownload.setProgress(0);
        dmla.notifyDataSetChanged();

        for(Media m: mediaSelected){
            if(!m.isDownloading()){
                downloadSelected.setText(getString(R.string.missing_media_download_selected));
                break;
            }
        }
    }

    private void showDownloadMediaMessage(){
        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        missingMediaContainer.startAnimation(anim);

        missingMediaContainer.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(0, missingMediaContainer.getMeasuredHeight());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            //@Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mediaList.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
                mediaList.setSelectionAfterHeaderView();
            }
        });
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();
    }

    private void hideDownloadMediaMessage(){

        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -200);
        anim.setDuration(900);
        missingMediaContainer.startAnimation(anim);

        missingMediaContainer.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(missingMediaContainer.getMeasuredHeight(), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            //@Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mediaList.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
                mediaList.setSelectionAfterHeaderView();
            }
        });
        animator.setStartDelay(0);
        animator.setDuration(700);
        animator.start();


        missingMediaContainer.setVisibility(View.GONE);
    }

    private class DownloadMediaListener implements ListInnerBtnOnClickListener {
    	
    	public final String TAG = DownloadMediaListener.class.getSimpleName();
    	
        //@Override
        public void onClick(int position) {

            Log.d(TAG, "Clicked " + position);
            Media mediaToDownload = missingMedia.get(position);

        	downloadMedia(mediaToDownload, DownloadMode.INDIVIDUALLY);

        }


    }

}
