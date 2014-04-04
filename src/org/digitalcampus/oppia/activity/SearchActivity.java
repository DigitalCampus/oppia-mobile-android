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
import org.digitalcampus.oppia.adapter.DownloadCourseListAdapter;
import org.digitalcampus.oppia.adapter.SearchResultsListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.SearchResult;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends AppActivity {

	public static final String TAG = SearchActivity.class.getSimpleName();

	private EditText search;
	private SearchResultsListAdapter srla;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);     	
	}
	
	@Override
	public void onStart(){
		super.onStart();
		EditText search = (EditText) findViewById(R.id.search_string);
		
		doSearch();
	}
	
	private void doSearch(){
		String searchText = "malaria";
		DbHelper db = new DbHelper(this);
		ArrayList<SearchResult> results = db.search(searchText);
		db.close();
		
		srla = new SearchResultsListAdapter(this, results);
		ListView listView = (ListView) findViewById(R.id.search_results_list);
		listView.setAdapter(srla);
		
		if(results.size() > 0){
			TextView tvSummary = (TextView) findViewById(R.id.search_results_summary);
			tvSummary.setText(getString(R.string.search_result_summary, 1, results.size(), searchText));
			tvSummary.setVisibility(View.VISIBLE);
		}
	}
}
