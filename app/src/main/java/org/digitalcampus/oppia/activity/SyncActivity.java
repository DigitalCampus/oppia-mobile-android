package org.digitalcampus.oppia.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.ExportActivityFragment;
import org.digitalcampus.oppia.fragments.TransferFragment;

import java.util.ArrayList;
import java.util.List;

public class SyncActivity extends AppActivity {

    public static final String TAG = SyncActivity.class.getSimpleName();
    public static final String TAB_ACTIVE = "TAB_ACTIVE";

    public static final int TAB_EXPORT = 0;
    public static final int TAB_ACTIVITY = 1;

    private ViewPager viewPager;
    private TabLayout tabs;
    private int currentTab = 0;
    private SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sync);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        viewPager = (ViewPager) findViewById(R.id.activity_sync_pager);
        tabs = (TabLayout) findViewById(R.id.tabs_toolbar);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            currentTab = bundle.getInt(SyncActivity.TAB_ACTIVE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        Fragment fTransfer = TransferFragment.newInstance();
        fragments.add(fTransfer);
        titles.add(this.getString(R.string.tab_title_transfer));

        Fragment fExport = ExportActivityFragment.newInstance();
        fragments.add(fExport);
        titles.add(this.getString(R.string.tab_title_activity));

        ActivityPagerAdapter adapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
        adapter.updateTabViews(tabs);
        viewPager.setCurrentItem(currentTab);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
    }
}
