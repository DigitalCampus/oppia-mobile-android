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

package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.adapter.SectionListAdapter;
import org.digitalcampus.mobile.learning.application.Tracker;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.Section;
import org.digitalcampus.mobile.learning.service.TrackerService;
import org.digitalcampus.mobile.learning.utils.UIUtils;
import org.digitalcampus.mobile.learning.widgets.MQuizWidget;
import org.digitalcampus.mobile.learning.widgets.PageWidget;
import org.digitalcampus.mobile.learning.widgets.ResourceWidget;
import org.digitalcampus.mobile.learning.widgets.WidgetFactory;
import org.digitalcampus.mquiz.MQuiz;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ModuleActivity extends AppActivity implements OnUtteranceCompletedListener, OnInitListener {

	public static final String TAG = ModuleActivity.class.getSimpleName();
	private Section section;
	private Module module;
	private int currentActivityNo = 0;
	private WidgetFactory currentActivity;
	private SharedPreferences prefs;
	
	private static int TTS_CHECK = 0;
	static TextToSpeech myTTS;
	private boolean ttsRunning = false;

	private HashMap<String, Object> mediaPlayingState = new HashMap<String, Object>();
	private MQuiz mQuiz;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_module);
		this.drawHeader();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			section = (Section) bundle.getSerializable(Section.TAG);
			module = (Module) bundle.getSerializable(Module.TAG);
			currentActivityNo = (Integer) bundle.getSerializable(SectionListAdapter.TAG_PLACEHOLDER);
		}	
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong("activityStartTimeStamp", currentActivity.getStartTime());
		savedInstanceState.putBoolean("mediaPlaying", currentActivity.getMediaPlaying());
		savedInstanceState.putLong("mediaStartTimeStamp", currentActivity.getMediaStartTime());
		savedInstanceState.putString("mediaFileName", currentActivity.getMediaFileName());
		savedInstanceState.putInt("currentActivityNo", this.currentActivityNo);
		savedInstanceState.putSerializable("mquiz", currentActivity.getMQuiz());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentActivity.setMediaPlaying(savedInstanceState.getBoolean("mediaPlaying"));
		currentActivity.setMediaStartTime(savedInstanceState.getLong("mediaStartTimeStamp"));
		currentActivity.setMediaFileName(savedInstanceState.getString("mediaFileName"));
		currentActivity.setStartTime(savedInstanceState.getLong("activityStartTimeStamp"));
		this.currentActivityNo = savedInstanceState.getInt("currentActivityNo");
		this.mQuiz = (MQuiz) savedInstanceState.getSerializable("mquiz");
	}

	@Override
	public void onStart() {
		super.onStart();
		setTitle(section.getTitle(prefs
				.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
		loadActivity();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (myTTS != null) {
			myTTS.shutdown();
		}

		ArrayList<org.digitalcampus.mobile.learning.model.Activity> acts = section.getActivities();
		this.saveTracker(acts.get(this.currentActivityNo).getDigest());

		// start a new tracker service
		Log.d(TAG, "Starting tracker service");
		Intent service = new Intent(this, TrackerService.class);

		Bundle tb = new Bundle();
		tb.putBoolean("backgroundData", true);
		service.putExtras(tb);

		if (currentActivity != null) {
			mediaPlayingState.put("Media_Playing", currentActivity.getMediaPlaying());
			mediaPlayingState.put("Media_StartTime", currentActivity.getMediaStartTime());
			mediaPlayingState.put("Media_File", currentActivity.getMediaFileName());
		}
		this.startService(service);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (currentActivity != null) {
			if (mediaPlayingState.containsKey("Media_Playing")) {
				currentActivity.setMediaPlaying((Boolean) mediaPlayingState.get("Media_Playing"));
			}
			if (mediaPlayingState.containsKey("Media_StartTime")) {
				currentActivity.setMediaStartTime((Long) mediaPlayingState.get("Media_StartTime"));
			}
			if (mediaPlayingState.containsKey("Media_File")) {
				currentActivity.setMediaFileName((String) mediaPlayingState.get("Media_File"));
			}
			currentActivity.mediaStopped();
		}
		loadActivity();
	}

	@Override
	protected void onDestroy() {
		if (myTTS != null) {
			myTTS.shutdown();
			myTTS = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_module, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = (MenuItem) menu.findItem(R.id.menu_tts);
		if (ttsRunning) {
			item.setTitle(R.string.menu_stop_read_aloud);
		} else {
			item.setTitle(R.string.menu_read_aloud);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_language:
			createLanguageDialog();
			return true;
		case R.id.menu_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		case R.id.menu_tts:
			if (myTTS == null && !ttsRunning) {
				// check for TTS data
				Intent checkTTSIntent = new Intent();
				checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
				startActivityForResult(checkTTSIntent, TTS_CHECK);
			} else if (myTTS != null && ttsRunning){
				this.stopReading();
			} else {
				// TTS not installed so show message
				Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadActivity() {
		ArrayList<org.digitalcampus.mobile.learning.model.Activity> acts = section.getActivities();
		TextView tb = (TextView) this.findViewById(R.id.module_activity_title);

		tb.setText(acts.get(this.currentActivityNo).getTitle(
				prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));

		if (acts.get(this.currentActivityNo).getActType().equals("page")) {
			currentActivity = new PageWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo));
		} else if (acts.get(this.currentActivityNo).getActType().equals("quiz")) {
			if(mQuiz != null){
				currentActivity = new MQuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo), mQuiz);
			} else {
				currentActivity = new MQuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo));
			}			
		} else if (acts.get(this.currentActivityNo).getActType().equals("resource")) {
			currentActivity = new ResourceWidget(this, module, acts.get(this.currentActivityNo));
		}
		this.setUpNav();
	}

	private void setUpNav() {
		Button prevB = (Button) ModuleActivity.this.findViewById(R.id.prev_btn);
		Button nextB = (Button) ModuleActivity.this.findViewById(R.id.next_btn);
		if (this.hasPrev()) {
			prevB.setVisibility(View.VISIBLE);
			prevB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					movePrev();
				}
			});
		} else {
			prevB.setVisibility(View.INVISIBLE);
		}

		if (this.hasNext()) {
			nextB.setVisibility(View.VISIBLE);
			nextB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					moveNext();
				}
			});
		} else {
			nextB.setVisibility(View.INVISIBLE);
		}
	}

	public boolean hasPrev() {
		if (this.currentActivityNo == 0) {
			return false;
		}
		return true;
	}

	public boolean hasNext() {
		int noActs = section.getActivities().size();
		if (this.currentActivityNo + 1 == noActs) {
			return false;
		} else {
			return true;
		}
	}

	public void moveNext() {
		this.stopReading();
		ArrayList<org.digitalcampus.mobile.learning.model.Activity> acts = section.getActivities();
		this.saveTracker(acts.get(currentActivityNo).getDigest());
		currentActivityNo++;
		loadActivity();
	}

	public void movePrev() {
		this.stopReading();
		ArrayList<org.digitalcampus.mobile.learning.model.Activity> acts = section.getActivities();
		this.saveTracker(acts.get(currentActivityNo).getDigest());
		currentActivityNo--;
		loadActivity();
	}

	private boolean saveTracker(String digest) {
		if (currentActivity != null && currentActivity.activityHasTracker()) {
			Tracker t = new Tracker(this);
			JSONObject json = currentActivity.getTrackerData();
			t.saveTracker(module.getModId(), digest, json, currentActivity.activityCompleted());
		}
		return true;
	}

	private void createLanguageDialog() {
		UIUtils ui = new UIUtils();
		ui.createLanguageDialog(this, module.getLangs(), prefs, new Callable<Boolean>() {
			public Boolean call() throws Exception {
				ModuleActivity.this.onStart();
				return true;
			}
		});
	}
	
	public void onInit(int status) {
		// check for successful instantiation
		if (status == TextToSpeech.SUCCESS) {
			Log.d(TAG, "tts success");
			ttsRunning = true;
			currentActivity.setReadAloud(true);
			HashMap<String,String> params = new HashMap<String,String>();
			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,TAG);
			myTTS.speak(currentActivity.getContentToRead(), TextToSpeech.QUEUE_FLUSH, params);
			myTTS.setOnUtteranceCompletedListener(this);
		} else {
			// TTS not installed so show message
			Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_CHECK) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// the user has the necessary data - create the TTS
				myTTS = new TextToSpeech(this, this);
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void stopReading() {
		if (myTTS != null) {
			myTTS.stop();
			myTTS = null;
		}
		this.ttsRunning = false;
	}

	public void onUtteranceCompleted(String utteranceId) {
		Log.d(TAG,"Finished reading");
		this.ttsRunning = false;
		myTTS = null;
	}
	
	public WidgetFactory getCurrentActivity(){
		return this.currentActivity;
	}
}
