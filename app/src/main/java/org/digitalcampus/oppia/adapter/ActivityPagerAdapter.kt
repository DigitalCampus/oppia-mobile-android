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
package org.digitalcampus.oppia.adapter

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan
import io.github.inflationx.calligraphy3.TypefaceUtils
import org.digitalcampus.mobile.learning.R

class ActivityPagerAdapter(
    private val ctx: Context,
    fm: FragmentManager,
    private val fragments: List<Fragment>,
    private val tabTitles: List<String>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        val TAG = ActivityPagerAdapter::class.simpleName
    }

    override fun getItem(index: Int): Fragment {
        return fragments[index]
    }

    override fun getPageTitle(position: Int): CharSequence {
        val title = tabTitles[position]
        val typefaceSpan = CalligraphyTypefaceSpan(TypefaceUtils.load(ctx.assets, "fonts/montserrat.ttf"))
        val s = SpannableStringBuilder()
        s.append(title).setSpan(typefaceSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return SpannableString.valueOf(s)
    }

    override fun getCount(): Int {
        return fragments.size
    }

    private fun updateTabLayout(tab: TabLayout.Tab?, layoutId: Int, tabTitle: String?) {
        tab?.let {
            val v = LayoutInflater.from(ctx).inflate(layoutId, null)
            val tv = v.findViewById<TextView>(R.id.tabTitle)
            tv.text = tabTitle
            tab.customView = v
        }
    }

    fun updateTabViews(tabs: TabLayout) {
        if (tabs.tabCount == 1) {
            updateTabLayout(tabs.getTabAt(0), R.layout.tablayout_fullwidth_tab, tabTitles[0])
            return
        }
        for (i in 0 until tabs.tabCount) {
            updateTabLayout(tabs.getTabAt(i), R.layout.tablayout_fixed_tab, tabTitles[i])
        }
    }
}