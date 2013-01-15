package org.digitalcampus.mobile.learning.application;

import org.digitalcampus.mobile.learning.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Header extends LinearLayout {

	public static final String TAG = Header.class.getSimpleName();
	public ImageView pageIcon;
	public TextView pageTitle;
	
	public Header(Context context) {
		super(context);
	}

	public Header(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}


	public void initHeader() {
		inflateHeader();
	}

	private void inflateHeader() {
	    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.header, this);
	    //pageTitle = 
	}
}
