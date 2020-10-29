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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.SearchResultsAdapter;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.SearchResult;
import org.digitalcampus.oppia.utils.ui.SimpleAnimator;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppActivity {

    private long userId = 0;

	private EditText searchText;
    private TextView summary;
    private ProgressBar loadingSpinner;
    private RecyclerView recyclerResults;
    private ImageView searchButton;

	private String currentSearch;
    protected ArrayList<SearchResult> results = new ArrayList<>();
    private SearchResultsAdapter adapterResults;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
        adapterResults = new SearchResultsAdapter(this, results);
        recyclerResults = findViewById(R.id.recycler_results_search);
        recyclerResults.setAdapter(adapterResults);
        adapterResults.setOnItemClickListener((view, position) -> {
            Course course = (Course) view.getTag(R.id.TAG_COURSE);
            String digest = (String) view.getTag(R.id.TAG_ACTIVITY_DIGEST);

            Intent i = new Intent(SearchActivity.this, CourseIndexActivity.class);
            Bundle tb = new Bundle();
            tb.putSerializable(Course.TAG, course);
            tb.putSerializable(CourseIndexActivity.JUMPTO_TAG, digest);
            i.putExtras(tb);
            SearchActivity.this.startActivity(i);
        });
	}
	
	@Override
	public void onStart(){
		super.onStart();
        initialize();

		DbHelper db = DbHelper.getInstance(this);
		userId = db.getUserId(SessionManager.getUsername(this));
		
		searchText = findViewById(R.id.search_string);
        //@Override
        searchText.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard(v);
            performSearch();
            return false;
        });
		summary = findViewById(R.id.search_results_summary);
        loadingSpinner = findViewById(R.id.progressBar);
        searchButton = findViewById(R.id.searchbutton);
        if (searchButton != null) {
            searchButton.setClickable(true);
            searchButton.setOnClickListener(v -> {
                hideKeyboard(v);
                performSearch();
            });
        }

	}

    @Override
    public void onResume(){
        super.onResume();
        if(!summary.getText().toString().equals("")){
            summary.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("currentSearch", currentSearch);
        if (summary.getVisibility() == View.VISIBLE){
            savedInstanceState.putString("summaryMsg", summary.getText().toString());
        }
        if (!results.isEmpty()){
            savedInstanceState.putSerializable("searchResults", results);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSearch = savedInstanceState.getString("currentSearch");
        String summaryMsg = savedInstanceState.getString("summaryMsg");
        if (summaryMsg != null){
            summary.setText(summaryMsg);
        }
        ArrayList<SearchResult> searchResults = (ArrayList<SearchResult>) savedInstanceState.getSerializable("searchResults");
        if ((searchResults != null) && !searchResults.isEmpty()){
            results.clear();
            results.addAll(searchResults);
            adapterResults.notifyDataSetChanged();
        }
    }

    private void hideKeyboard(View v){
        //We hide the keyboard
        InputMethodManager imm =  (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void performSearch(){
        String newSearch = searchText.getText().toString().trim();

        if (TextUtils.isEmpty(newSearch)) {
            searchText.setText("");
            return;
        }

        if (!newSearch.equals(currentSearch)){
            currentSearch = newSearch;

            searchButton.setEnabled(false);
            loadingSpinner.setVisibility(View.VISIBLE);
            summary.setText(getString(R.string.search_message_searching));
            summary.setVisibility(View.VISIBLE);
            SimpleAnimator.fadeFromTop(recyclerResults, SimpleAnimator.FADE_OUT);
            SimpleAnimator.fadeFromTop(summary, SimpleAnimator.FADE_IN);

            new SearchTask().execute("");
        }
    }

    private class SearchTask extends AsyncTask<String, Object, List<SearchResult>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            summary.setText(getString(R.string.search_message_fetching));
        }

        @Override
        protected List<SearchResult> doInBackground(String... urls) {
            Log.d(TAG, "Starting search...");
            DbHelper db = DbHelper.getInstance(SearchActivity.this);
            List<SearchResult> searchResults = db.search(currentSearch, 100, userId, SearchActivity.this);

            //Save the search tracker
            new Tracker(SearchActivity.this)
                    .saveSearchTracker(currentSearch, searchResults.size());

            return searchResults;
        }

        @Override
        protected void onPostExecute(List<SearchResult> searchResults) {
            results.clear();
            results.addAll(searchResults);
            adapterResults.notifyDataSetChanged();
            SimpleAnimator.fadeFromTop(recyclerResults, SimpleAnimator.FADE_IN);
            loadingSpinner.setVisibility(View.GONE);
            searchButton.setEnabled(true);

            summary.setText(!results.isEmpty() ?
                    getString(R.string.search_result_summary, results.size(), currentSearch) :
                    getString(R.string.search_message_no_results, currentSearch));
        }

    }

}
