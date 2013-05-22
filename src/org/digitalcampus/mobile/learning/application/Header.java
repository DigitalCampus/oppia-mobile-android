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

package org.digitalcampus.mobile.learning.application;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.utils.UIUtils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Header extends RelativeLayout {

	public static final String TAG = Header.class.getSimpleName();
	
	public Header(Context context) {
		super(context);
	}

	public Header(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public void initHeader(Activity act) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.header, this);
	    UIUtils.showUserData(act);
	}
	
	public void initHeader(Activity act,String title) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.header, this);
	    UIUtils.showUserData(act);
	    TextView tv = (TextView) act.findViewById(R.id.page_title);
	    tv.setText(title);
	}
	
	public void updateHeader(Activity act){
		UIUtils.showUserData(act);
	}

}
