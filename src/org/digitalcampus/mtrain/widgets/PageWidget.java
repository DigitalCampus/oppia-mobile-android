package org.digitalcampus.mtrain.widgets;

import org.digitalcampus.mtrain.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class PageWidget extends WidgetFactory{

	private static final String TAG = "PageWidget";
	
	private Context ctx;
	
	public PageWidget(Context context) {
		super(context);
		this.ctx = context;
		LinearLayout ll = (LinearLayout) ((Activity) this.ctx).findViewById(R.id.activity_widget);
		ll.removeAllViews();
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = vi.inflate(R.layout.widget_page, null);
		ll.addView(vv);
	}

}
