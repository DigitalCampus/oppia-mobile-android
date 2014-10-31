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

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.SearchResultsListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.SearchResult;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends AppActivity {

	public static final String TAG = SearchActivity.class.getSimpleName();

	private EditText searchText;
	private SearchResultsListAdapter srla;
	private TextView summary;
	private SharedPreferences prefs;
	private long userId = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		DbHelper db = new DbHelper(this);
		userId = db.getUserId(prefs.getString("preUsername", ""));
		DatabaseManager.getInstance().closeDatabase();
		
		searchText = (EditText) findViewById(R.id.search_string);
		summary = (TextView) findViewById(R.id.search_results_summary);
		ImageView searchNow = (ImageView) findViewById(R.id.searchbutton);
		searchNow.setClickable(true);
		searchNow.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				summary.setText(getString(R.string.search_searching));
				doSearch();
			}
		});
	}
	
	private void doSearch(){
		String searchString = searchText.getText().toString();
		DbHelper db = new DbHelper(this);
		ArrayList<SearchResult> results = db.search(searchString, 100, userId, this);
		DatabaseManager.getInstance().closeDatabase();
	
		srla = new SearchResultsListAdapter(this, results);
		ListView listView = (ListView) findViewById(R.id.search_results_list);
		listView.setAdapter(srla);
		
		if(results.size() > 0){
			summary.setText(getString(R.string.search_result_summary, results.size(), searchString));
			summary.setVisibility(View.VISIBLE);
		} else {
			summary.setText(getString(R.string.search_no_results, searchString));
			summary.setVisibility(View.VISIBLE);
		}
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Course course = (Course) view.getTag(R.id.TAG_COURSE);
				String digest = (String) view.getTag(R.id.TAG_ACTIVITY_DIGEST);
				Intent i = new Intent(SearchActivity.this, CourseIndexActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Course.TAG, course);
				tb.putSerializable("JumpTo", digest);
				i.putExtras(tb);
				SearchActivity.this.startActivity(i);
			}
		});
	}
}
