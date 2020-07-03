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

package org.digitalcampus.oppia.adapter;

import java.util.List;

import android.content.Context;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;

import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan;
import io.github.inflationx.calligraphy3.TypefaceUtils;


public class ActivityPagerAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = ActivityPagerAdapter.class.getSimpleName();

    private List<Fragment> fragments;
    private List<String> tabTitles;
    private Context ctx;
	
	public ActivityPagerAdapter(Context ctx, FragmentManager fm, List<Fragment> fragments, List<String> titles) {
		super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.ctx = ctx;
		this.fragments = fragments;
        this.tabTitles = titles;
	}

	@Override
	public Fragment getItem(int index) {
		return fragments.get(index);
	}

    @Override
    public CharSequence getPageTitle(int position) {
        String title = tabTitles.get(position);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(ctx.getAssets(), "fonts/montserrat.ttf"));
        SpannableStringBuilder s = new SpannableStringBuilder();
        s.append(title).setSpan(typefaceSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return SpannableString.valueOf(s);
    }

	@Override
	public int getCount() {
		return fragments.size();
	}


	public void updateTabLayout(TabLayout.Tab tab, int layoutId, String tabTitle){

	    if (tab == null){
	        return;
        }
	    View v = LayoutInflater.from(ctx).inflate(layoutId, null);
        TextView tv = v.findViewById(R.id.tabTitle);
        tv.setText(tabTitle);
        tab.setCustomView(v);
    }

    public void updateTabViews(TabLayout tabs) {

	    if (tabs.getTabCount() == 1){
            updateTabLayout(
                    tabs.getTabAt(0),
                    R.layout.tablayout_fullwidth_tab,
                    tabTitles.get(0));
            return;
        }

        for (int i = 0; i < tabs.getTabCount(); i++) {
            updateTabLayout(
                    tabs.getTabAt(i),
                    R.layout.tablayout_fixed_tab,
                    tabTitles.get(i));
        }
    }

}
