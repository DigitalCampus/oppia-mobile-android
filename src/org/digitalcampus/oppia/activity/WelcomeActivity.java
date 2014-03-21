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
import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.LoginFragment;
import org.digitalcampus.oppia.fragments.RegisterFragment;
import org.digitalcampus.oppia.fragments.WelcomeFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class WelcomeActivity extends SherlockFragmentActivity implements ActionBar.TabListener  {

	public static final String TAG = WelcomeActivity.class.getSimpleName();
	private ActionBar actionBar;
	private ViewPager viewPager;
	private ActivityPagerAdapter apAdapter;
	private int currentTab = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		actionBar = getSupportActionBar();
		viewPager = (ViewPager) findViewById(R.id.activity_about_pager);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}
	
	@Override
	public void onStart() {
		super.onStart();

		actionBar.removeAllTabs();
		List<Fragment> fragments = new ArrayList<Fragment>();
		
		Fragment fWelcome = WelcomeFragment.newInstance();
		fragments.add(fWelcome);
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_welcome)).setTabListener(this), true);

		Fragment fLogin = LoginFragment.newInstance();
		fragments.add(fLogin);
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_login)).setTabListener(this), false);

		
		Fragment fRegister = RegisterFragment.newInstance();
		fragments.add(fRegister);
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_register)).setTabListener(this), false);

		apAdapter = new ActivityPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(apAdapter);

		viewPager.setCurrentItem(currentTab);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageScrollStateChanged(int arg0) {
				// do nothing
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// do nothing
			}

			public void onPageSelected(int arg0) {
				actionBar.setSelectedNavigationItem(arg0);
			}

		});
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
		this.currentTab = tab.getPosition();
		
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_welcome, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, PrefsActivity.class);
			startActivity(i);
			return true;
		case R.id.menu_about:
			Intent iA = new Intent(this, AboutActivity.class);
			startActivity(iA);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void switchTab(int tab){
		viewPager.setCurrentItem(tab);
		this.currentTab = tab;
	}
}

