package org.digitalcampus.mtrain.widgets;

import java.util.HashMap;

import org.digitalcampus.mtrain.model.Module;

import android.content.Context;
import android.widget.LinearLayout;

public abstract class WidgetFactory extends LinearLayout{
	
	private Context ctx;
	
	public WidgetFactory(Context context, Module module, HashMap<String,String> data ) {
		super(context);
		this.ctx = context;
	}
	
}
