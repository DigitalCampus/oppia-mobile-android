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
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivitySearchBinding;
import org.digitalcampus.oppia.adapter.SearchResultsAdapter;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.SearchResult;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.ui.SimpleAnimator;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppActivity {

    private long userId = 0;

    private String currentSearch;
    protected ArrayList<SearchResult> results = new ArrayList<>();
    private SearchResultsAdapter adapterResults;
    private ActivitySearchBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        
        adapterResults = new SearchResultsAdapter(this, results);
        
        binding.recyclerResultsSearch.setAdapter(adapterResults);
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

//        new Handler().postDelayed(() -> {
//
//            new Thread(() -> {
//
//                for (int i = 0; i < 500000; i++) {
//                    SearchResult searchResult = new SearchResult();
//                    searchResult.setCourse(new Course());
//                    searchResult.setActivity(new Activity());
//                    searchResult.setSection(new Section());
//                    results.add(searchResult);
//
//                    Log.i(TAG, "onCreate: index: " + i);
//
//                }
//            }).start();
//        }, 2000);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        DbHelper db = DbHelper.getInstance(this);
        userId = db.getUserId(SessionManager.getUsername(this));

        //@Override
        binding.searchString.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard(v);
            performSearch();
            return false;
        });

        if (binding.searchbutton != null) {
            binding.searchbutton.setClickable(true);
            binding.searchbutton.setOnClickListener(v -> {
                hideKeyboard(v);
                performSearch();
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!binding.searchResultsSummary.getText().toString().equals("")) {
            binding.searchResultsSummary.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("currentSearch", currentSearch);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSearch = savedInstanceState.getString("currentSearch");
        if (!TextUtils.isEmpty(currentSearch)) {
            binding.searchString.setText(currentSearch);
            currentSearch = "";
            performSearch();
        }
    }

    private void hideKeyboard(View v) {
        //We hide the keyboard
        InputMethodManager imm = (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void performSearch() {
        String newSearch = binding.searchString.getText().toString().trim();

        if (TextUtils.isEmpty(newSearch)) {
            binding.searchString.setText("");
            return;
        }

        if (!newSearch.equals(currentSearch)) {
            currentSearch = newSearch;

            binding.searchbutton.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.searchResultsSummary.setText(getString(R.string.search_message_searching));
            binding.searchResultsSummary.setVisibility(View.VISIBLE);
            SimpleAnimator.fadeFromTop(binding.recyclerResultsSearch, SimpleAnimator.FADE_OUT);
            SimpleAnimator.fadeFromTop(binding.searchResultsSummary, SimpleAnimator.FADE_IN);

            new SearchTask().execute("");
        }
    }

    private class SearchTask extends AsyncTask<String, Object, List<SearchResult>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.searchResultsSummary.setText(getString(R.string.search_message_fetching));
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
            SimpleAnimator.fadeFromTop(binding.recyclerResultsSearch, SimpleAnimator.FADE_IN);
            binding.progressBar.setVisibility(View.GONE);
            binding.searchbutton.setEnabled(true);

            binding.searchResultsSummary.setText(!results.isEmpty() ?
                    getString(R.string.search_result_summary, results.size(), currentSearch) :
                    getString(R.string.search_message_no_results, currentSearch));
        }

    }

}
