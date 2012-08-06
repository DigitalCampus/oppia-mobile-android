package org.digitalcampus.mtrain.widgets;

import android.content.Context;
import android.widget.LinearLayout;

public abstract class WidgetFactory extends LinearLayout{
	
	private Context ctx;
	
	public WidgetFactory(Context context) {
		super(context);
		this.ctx = context;
	}
	
}
