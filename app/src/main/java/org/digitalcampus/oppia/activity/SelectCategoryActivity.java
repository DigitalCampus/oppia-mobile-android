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

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseCategoriesAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.DrawerDelegate;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseTag;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectCategoryActivity extends AppActivity implements CourseInstallerListener {

    private ArrayList<Course> courses;

    private ArrayList<CourseTag> tags = new ArrayList<>();
    private ArrayAdapter<CourseTag> tagsAdapter;
    private InstallerBroadcastReceiver receiver;
    private DrawerDelegate drawerDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        courses = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerDelegate = new DrawerDelegate(this, courses);
        drawerDelegate.initializeDrawer(toolbar);

        ListView tagsList = (ListView) findViewById(R.id.categories_list);
        tagsAdapter = new CourseCategoriesAdapter(this, tags);
        tagsList.setAdapter(tagsAdapter);
        tagsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(SelectCategoryActivity.this, OppiaMobileActivity.class);
                CourseTag selectedTag = tags.get(position);
                if (selectedTag.getTag()!= null && !selectedTag.getTag().equals("all")){
                    Bundle tb = new Bundle();
                    tb.putString(Course.TAG, selectedTag.getTag());
                    i.putExtras(tb);
                }
                startActivity(i);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseIntallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        displayCourses();
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void displayCourses(){
        DbHelper db = DbHelper.getInstance(this);
        long userId = db.getUserId(SessionManager.getUsername(this));
        courses.clear();
        courses.addAll(db.getCourses(userId, ""));

        tags.clear();
        HashMap<String, Integer> tagCount = new HashMap<>();
        for (Course course : courses){
            if (course.getTags() == null) continue;
            String[] tagsList = course.getTags().split(",");
            for (String tag : tagsList) {
                if (tagCount.containsKey(tag)){
                    int prevCount = tagCount.get(tag);
                    tagCount.put(tag, prevCount+1);
                }
                else{
                    tagCount.put(tag, 1);
                }
            }
        }

        if (tagCount.isEmpty()){
            //If none of the courses has tags (or there are no courses), go to the main activity
            this.finish();
            this.startActivity(new Intent(this, OppiaMobileActivity.class));
            return;
        }

        for (String key : tagCount.keySet()){
            tags.add(new CourseTag(key, tagCount.get(key)));
        }
        tags.add(new CourseTag("all", courses.size()));
        tagsAdapter.notifyDataSetChanged();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        drawerDelegate.createOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UIUtils.showUserData(menu, this, null);
        drawerDelegate.prepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.d(TAG, "Menu item selected: " + item.getTitle());
        return drawerDelegate.onOptionsItemSelected(item.getItemId());
    }

    @Override public void onDownloadProgress(String fileUrl, int progress) {}
    @Override public void onInstallProgress(String fileUrl, int progress) {}
    @Override public void onInstallFailed(String fileUrl, String message) {}

    @Override
    public void onInstallComplete(String fileUrl) {
        //Refresh list
    }


}
