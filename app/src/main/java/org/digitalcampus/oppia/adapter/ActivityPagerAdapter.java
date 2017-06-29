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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.kano.training.oppia.R;

public class ActivityPagerAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = ActivityPagerAdapter.class.getSimpleName();

    private List<Fragment> fragments;
    private List<String> tabTitles;
    private Context ctx;
	
	public ActivityPagerAdapter(Context ctx, FragmentManager fm, List<Fragment> fragments, List<String> titles) {
		super(fm);
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
        return tabTitles.get(position);
    }

	@Override
	public int getCount() {
		return fragments.size();
	}

    public View getTabView(int position) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.tablayout_fixed_tab, null);
        TextView tv = (TextView) v.findViewById(R.id.tabTitle);
        tv.setText(tabTitles.get(position));
        return v;
    }
}
