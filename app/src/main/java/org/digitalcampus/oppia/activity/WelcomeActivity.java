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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.fragments.LoginFragment;
import org.digitalcampus.oppia.fragments.RegisterFragment;
import org.digitalcampus.oppia.fragments.ResetFragment;
import org.digitalcampus.oppia.fragments.WelcomeFragment;
import org.digitalcampus.oppia.model.Lang;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppActivity {

	public static final int TAB_WELCOME = 0;
    public static final int TAB_LOGIN = 1;
    public static final int TAB_REGISTER = 2;
    public static final int TAB_PASSWORD = 3;

    private ViewPager viewPager;
    private int currentTab = TAB_WELCOME;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		viewPager = findViewById(R.id.activity_welcome_pager);
		getAppComponent().inject(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.getMenu().clear();
		toolbar.inflateMenu(R.menu.activity_welcome);
		toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

		List<Fragment> fragments = new ArrayList<>();
        List<String> tabTitles = new ArrayList<>();
		
		Fragment fWelcome = WelcomeFragment.newInstance();
		fragments.add(fWelcome);
        tabTitles.add(this.getString(R.string.tab_title_welcome));

		Fragment fLogin = LoginFragment.newInstance();
		fragments.add(fLogin);
        tabTitles.add(this.getString(R.string.tab_title_login));

		Fragment fRegister = RegisterFragment.newInstance();
		fragments.add(fRegister);
        tabTitles.add(this.getString(R.string.tab_title_register));

		Fragment fReset = ResetFragment.newInstance();
		fragments.add(fReset);
        tabTitles.add(this.getString(R.string.tab_title_reset));

        ActivityPagerAdapter apAdapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, tabTitles);
		viewPager.setAdapter(apAdapter);
		viewPager.setCurrentItem(currentTab);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_welcome, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_settings) {
			Intent i = new Intent(this, PrefsActivity.class);
			Bundle tb = new Bundle();
			ArrayList<Lang> langs = new ArrayList<>();
			Lang lang = new Lang("en","English");
			langs.add(lang);
			tb.putSerializable("langs", langs);
			i.putExtras(tb);
			startActivity(i);
			return true;
		} else if (itemId == R.id.menu_about) {
			Intent iA = new Intent(this, AboutActivity.class);
			startActivity(iA);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void switchTab(int tab){
		viewPager.setCurrentItem(tab);
		this.currentTab = tab;
	}

	@Override
	public void onBackPressed() {
		if (currentTab == TAB_WELCOME){
			super.onBackPressed();
		}
		else{
			switchTab(TAB_WELCOME);
		}
	}
}

