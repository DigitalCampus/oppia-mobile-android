package org.digitalcampus.oppia.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.DrawerDelegate;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.utils.UIUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SelectCategoryActivity extends AppActivity implements CourseInstallerListener {

    private SharedPreferences prefs;
    private ArrayList<Course> courses;

    private ArrayList<String> tags = new ArrayList<>();
    private ArrayAdapter<String> tagsAdapter;
    private InstallerBroadcastReceiver receiver;
    private DrawerDelegate drawerDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        courses = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerDelegate = new DrawerDelegate(this, courses);
        drawerDelegate.initializeDrawer(toolbar);

        ListView tagsList = (ListView) findViewById(R.id.categories_list);
        tagsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, tags);
        tagsList.setAdapter(tagsAdapter);
        tagsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(SelectCategoryActivity.this, OppiaMobileActivity.class);
                String selectedTag = tags.get(position);
                if (selectedTag!= null && !selectedTag.equals("all")){
                    Bundle tb = new Bundle();
                    tb.putString(Course.TAG, selectedTag);
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
        for (Course course : courses){
            if (course.getTags() == null) continue;
            String[] tagsList = course.getTags().split(",");
            for (String aTagsList : tagsList) {
                if ((aTagsList != null) && (!tags.contains(aTagsList.trim())))
                    tags.add(aTagsList);
            }
        }
        tags.add("all");
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
