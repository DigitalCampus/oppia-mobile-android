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

import java.util.ArrayList;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.SearchResultsListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.listener.DBListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.SearchResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends AppActivity {

	public static final String TAG = SearchActivity.class.getSimpleName();

    private SharedPreferences prefs;
    private long userId = 0;

	private EditText searchText;
    private TextView summary;
    private ProgressBar loadingSpinner;
    private ListView resultsList;
    private ImageView searchButton;

	private String currentSearch;
    private SearchResultsListAdapter srla;
    protected ArrayList<SearchResult> results = new ArrayList<SearchResult>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        srla = new SearchResultsListAdapter(this, results);
        resultsList = (ListView) findViewById(R.id.search_results_list);
        resultsList.setAdapter(srla);
        resultsList.setOnItemClickListener(new OnItemClickListener() {

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
	
	@Override
	public void onStart(){
		super.onStart();
		DbHelper db = new DbHelper(this);
		userId = db.getUserId(prefs.getString("preUsername", ""));
		DatabaseManager.getInstance().closeDatabase();
		
		searchText = (EditText) findViewById(R.id.search_string);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                performSearch();
                return false;
            }
        });
		summary = (TextView) findViewById(R.id.search_results_summary);
        loadingSpinner = (ProgressBar) findViewById(R.id.progressBar);
        searchButton = (ImageView) findViewById(R.id.searchbutton);
        searchButton.setClickable(true);
        searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                //We hide the keyboard
                InputMethodManager imm =  (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                performSearch();
			}
		});
	}

    @Override
    public void onResume(){
        super.onResume();
        if(!summary.getText().toString().equals("")){
            summary.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("currentSearch", currentSearch);
        if (summary.getVisibility() == View.VISIBLE){
            savedInstanceState.putString("summaryMsg", summary.getText().toString());
        }
        if (results.size() > 0){
            savedInstanceState.putSerializable("searchResults", results);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSearch = savedInstanceState.getString("currentSearch");
        String summaryMsg = savedInstanceState.getString("summaryMsg");
        if (summaryMsg != null){
            summary.setText(summaryMsg);
        }
        ArrayList<SearchResult> searchResults = (ArrayList<SearchResult>) savedInstanceState.getSerializable("searchResults");
        if ((searchResults != null) && searchResults.size() > 0){
            results.clear();
            results.addAll(searchResults);
            srla.notifyDataSetChanged();
        }
    }

    private void performSearch(){
        String newSearch = searchText.getText().toString();
        if (!newSearch.equals(currentSearch)){
            currentSearch = newSearch;

            searchButton.setEnabled(false);
            loadingSpinner.setVisibility(View.VISIBLE);
            summary.setText(getString(R.string.search_searching));
            summary.setVisibility(View.VISIBLE);
            setFadeAnimation(resultsList, false);
            setFadeAnimation(summary, true);

            new SearchTask().execute("");
        }
    }

    protected void setFadeAnimation(View view, boolean visible){
        Animation fadeAnimation = new AlphaAnimation(visible?0:1, visible?1:0);
        fadeAnimation.setInterpolator(new DecelerateInterpolator()); //add this
        fadeAnimation.setDuration(700);
        fadeAnimation.setFillAfter(true);
        view.setAnimation(fadeAnimation);
    }

    private class SearchTask extends AsyncTask<String, Object, ArrayList<SearchResult>> implements DBListener{

        @Override
        protected ArrayList<SearchResult> doInBackground(String... urls) {
            Log.d(TAG, "Starting search...");
            DbHelper db = new DbHelper(SearchActivity.this);
            ArrayList<SearchResult> searchResults = db.search(currentSearch, 100, userId, SearchActivity.this, this);
            DatabaseManager.getInstance().closeDatabase();

            return searchResults;
        }

        @Override
        protected void onPostExecute(ArrayList<SearchResult> searchResults) {
            results.clear();
            results.addAll(searchResults);
            srla.notifyDataSetChanged();
            resultsList.setSelectionAfterHeaderView();
            setFadeAnimation(resultsList, true);
            loadingSpinner.setVisibility(View.GONE);
            searchButton.setEnabled(true);

            summary.setText(results.size() > 0 ?
                    getString(R.string.search_result_summary, results.size(), currentSearch) :
                    getString(R.string.search_no_results, currentSearch));
        }

        @Override
        public void onProgressUpdate(Object... values){
            summary.setText("Fetching results...");
        }

        @Override
        public void onQueryPerformed() {
            publishProgress(true);


        }
    }
}
