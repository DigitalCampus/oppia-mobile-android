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

package org.digitalcampus.oppia.fragments;

import org.digitalcampus.mobile.learning.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout.LayoutParams;

public class OppiaWebViewFragment extends Fragment{

	public static final String TAG = OppiaWebViewFragment.class.getSimpleName();
	private static final String TAG_ID = "OppiaWebViewFragment_TAG_ID";
	
	private WebView webView;
	private String url;
	private int id;
	
	public static OppiaWebViewFragment newInstance(int id, String url) {
		OppiaWebViewFragment myFragment = new OppiaWebViewFragment();
		Bundle args = new Bundle();
	    args.putSerializable(OppiaWebViewFragment.TAG, url);
	    args.putSerializable(OppiaWebViewFragment.TAG_ID, id);
	    myFragment.setArguments(args);
	    return myFragment;
	}

	public OppiaWebViewFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_webview, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		this.id = (Integer) getArguments().getSerializable(OppiaWebViewFragment.TAG_ID);
		vv.setId(id);
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		webView = (WebView) super.getActivity().findViewById(this.id);
		webView.getSettings().setJavaScriptEnabled(true);
		url = (String) getArguments().getString(OppiaWebViewFragment.TAG);
		webView.loadUrl(url);

	}

}
