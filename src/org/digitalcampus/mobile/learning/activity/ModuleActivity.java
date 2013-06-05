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
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ModuleActivity extends AppActivity implements OnUtteranceCompletedListener, OnInitListener {

	public static final String TAG = ModuleActivity.class.getSimpleName();
	private Section section;
	private Module module;
	private int currentActivityNo = 0;
	private WidgetFactory currentActivity;
	private SharedPreferences prefs;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector pageGestureDetector;
	View.OnTouchListener pageGestureListener;

	private GestureDetector quizGestureDetector;
	View.OnTouchListener quizGestureListener;

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

		// Gesture detection for pages
		pageGestureDetector = new GestureDetector(new PageGestureDetector());
		pageGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return pageGestureDetector.onTouchEvent(event);
			}
		};

		// Gesture detection for quizzes
		quizGestureDetector = new GestureDetector(new QuizGestureDetector());
		quizGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return quizGestureDetector.onTouchEvent(event);
			}
		};
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
		Log.d(TAG,"saved instance state");
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
		Log.d(TAG,"restored instance state");
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
			WebView wv = (WebView) this.findViewById(R.id.page_webview);
			wv.setOnTouchListener(pageGestureListener);
		}
		if (acts.get(this.currentActivityNo).getActType().equals("quiz")) {
			if(mQuiz != null){
				currentActivity = new MQuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo), mQuiz);
				Log.d(TAG,"Sending mquiz object");
				//currentActivity.setMQuiz(mQuiz);
			} else {
				currentActivity = new MQuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo));
			}
			ScrollView sv = (ScrollView) this.findViewById(R.id.quizScrollView);
			sv.setOnTouchListener(quizGestureListener);
		}
		this.setUpNav();
	}

	private void setUpNav() {
		Button prevB = (Button) ModuleActivity.this.findViewById(R.id.prev_btn);
		Button nextB = (Button) ModuleActivity.this.findViewById(R.id.next_btn);
		if (this.hasPrev()) {
			prevB.setEnabled(true);
			prevB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					movePrev();
				}
			});
		} else {
			prevB.setEnabled(false);
		}

		if (this.hasNext()) {
			nextB.setEnabled(true);
			nextB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					moveNext();
				}
			});
		} else {
			nextB.setEnabled(false);
		}
	}

	private boolean hasPrev() {
		if (this.currentActivityNo == 0) {
			return false;
		}
		return true;
	}

	private boolean hasNext() {
		int noActs = section.getActivities().size();
		if (this.currentActivityNo + 1 == noActs) {
			return false;
		} else {
			return true;
		}
	}

	private void moveNext() {
		this.stopReading();
		ArrayList<org.digitalcampus.mobile.learning.model.Activity> acts = section.getActivities();
		this.saveTracker(acts.get(currentActivityNo).getDigest());
		currentActivityNo++;
		loadActivity();
	}

	private void movePrev() {
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

	class PageGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if (ModuleActivity.this.hasNext()) {
						ModuleActivity.this.moveNext();
					}
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if (ModuleActivity.this.hasPrev()) {
						ModuleActivity.this.movePrev();
					}
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

	}

	class QuizGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if (ModuleActivity.this.currentActivity instanceof MQuizWidget) {
						if (((MQuizWidget) ModuleActivity.this.currentActivity).getMquiz().hasNext()) {
							((MQuizWidget) ModuleActivity.this.currentActivity).nextBtn.performClick();
						}
					}

				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if (ModuleActivity.this.currentActivity instanceof MQuizWidget) {
						if (((MQuizWidget) ModuleActivity.this.currentActivity).getMquiz().hasPrevious()) {
							((MQuizWidget) ModuleActivity.this.currentActivity).prevBtn.performClick();
						}
					}
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

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
}
