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

import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BaselineActivity extends AppActivity {

	private SharedPreferences prefs;
	private int currentActivityNo = 0;
	private Activity activity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_baseline);
		this.drawHeader();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.activity = (Activity) bundle.getSerializable(Activity.TAG);
		}		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		loadActivity();
	}
	
	private void loadActivity() {
		TextView tb = (TextView) this.findViewById(R.id.module_activity_title);

		tb.setText(activity.getTitle(
				prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
		this.setUpNav();
	}
	
	private void setUpNav() {
		Button prevB = (Button) this.findViewById(R.id.prev_btn);
		Button nextB = (Button) this.findViewById(R.id.next_btn);
		if (this.hasPrev()) {
			prevB.setVisibility(View.VISIBLE);
			prevB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//movePrev();
				}
			});
		} else {
			prevB.setVisibility(View.INVISIBLE);
		}

		if (this.hasNext()) {
			nextB.setVisibility(View.VISIBLE);
			nextB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//moveNext();
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
		if (this.currentActivityNo + 1 == 1) {
			return false;
		} else {
			return true;
		}
	}
}
