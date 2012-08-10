package org.digitalcampus.mtrain.adapter;

import java.util.List;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.activity.ModuleIndexActivity;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModuleListAdapter extends ArrayAdapter<Module> {

	public static final String TAG = "ModuleListAdapter";

	private LayoutInflater inflater;
	private Activity ctx;

	public ModuleListAdapter(Activity context, List<Module> moduleList) {
		super(context, R.layout.module_list_row, R.id.module_title, moduleList);
		this.ctx = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Module m = (Module) this.getItem(position);
		TextView textView;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.module_list_row, null);

			textView = (TextView) convertView.findViewById(R.id.module_title);
			textView.setTag(m);

			convertView.setTag(new ModuleViewHolder(textView, m));

			convertView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Log.d(TAG, "Clicked");
					ModuleViewHolder m = (ModuleViewHolder) v.getTag();
					v.setBackgroundResource(R.drawable.background_gradient);
					Log.d(TAG, m.getModule().getTitle());
					Intent i = new Intent(ctx, ModuleIndexActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(Module.TAG, (Module) m.getModule());
					i.putExtras(tb);
					ctx.startActivity(i);
				}
			});

		} else {
			ModuleViewHolder viewHolder = (ModuleViewHolder) convertView.getTag();
			textView = viewHolder.getTextView();
		}

		textView.setText(m.getTitle());

		return convertView;
	}

	private static class ModuleViewHolder {
		private TextView textView;
		private Module m;

		public ModuleViewHolder(TextView textView, Module m) {
			this.textView = textView;
			this.m = m;
		}

		public Module getModule() {
			return m;
		}

		public TextView getTextView() {
			return textView;
		}

	}
}
