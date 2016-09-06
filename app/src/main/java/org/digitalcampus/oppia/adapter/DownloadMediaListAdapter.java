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

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadMediaListAdapter extends ArrayAdapter<Media> {

	public static final String TAG = DownloadMediaListAdapter.class.getSimpleName();

	private final Context ctx;
	private ArrayList<Media> mediaList;

    private ListInnerBtnOnClickListener onClickListener;
	
	public DownloadMediaListAdapter(Activity context, ArrayList<Media> mediaList) {
		super(context, R.layout.media_download_row, mediaList);
		this.ctx = context;
		this.mediaList = mediaList;
	}

    static class DownloadMediaViewHolder{
        TextView mediaCourses;
        TextView mediaTitle;
        TextView mediaPath;
        TextView mediaFileSize;
        ImageButton downloadBtn;
        ProgressBar downloadProgress;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        DownloadMediaViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.media_download_row, parent, false);
            viewHolder = new DownloadMediaViewHolder();
            viewHolder.mediaCourses = (TextView) convertView.findViewById(R.id.media_courses);
            viewHolder.mediaTitle = (TextView) convertView.findViewById(R.id.media_title);
            viewHolder.mediaPath = (TextView) convertView.findViewById(R.id.media_path);
            viewHolder.mediaFileSize = (TextView) convertView.findViewById(R.id.media_file_size);
            viewHolder.downloadBtn = (ImageButton) convertView.findViewById(R.id.action_btn);
            viewHolder.downloadProgress = (ProgressBar) convertView.findViewById(R.id.download_progress);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (DownloadMediaViewHolder) convertView.getTag();
        }

        Media m = mediaList.get(position);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String courses = ctx.getString(R.string.media_appears);
        for(int i = 0; i < m.getCourses().size(); i++){
            Course c = m.getCourses().get(i);
            String title = c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
            courses += i != 0 ? ", " + title : " " + title;
        }

        viewHolder.mediaCourses.setText(courses);
        viewHolder.mediaTitle.setText(m.getFilename());
        viewHolder.mediaPath.setText(m.getDownloadUrl());
		if(m.getFileSize() != 0){
            viewHolder.mediaFileSize.setText(ctx.getString(R.string.media_file_size,m.getFileSize()/(1024*1024)));
		} else {
            viewHolder.mediaFileSize.setVisibility(View.GONE);
		}

        viewHolder.downloadBtn.setTag(position); //For passing the list item index
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (onClickListener != null)
                    onClickListener.onClick((Integer) v.getTag());
            }
        });

        if (m.isDownloading()){
            viewHolder.downloadBtn.setImageResource(R.drawable.ic_action_cancel);
            viewHolder.downloadProgress.setVisibility(View.VISIBLE);
            if (m.getProgress()>0){
                viewHolder.downloadProgress.setIndeterminate(false);
                viewHolder.downloadProgress.setProgress(m.getProgress());
            }
            else {
                viewHolder.downloadProgress.setIndeterminate(true);
            }
        }
        else{
            viewHolder.downloadBtn.setImageResource(R.drawable.ic_action_download);
            viewHolder.downloadProgress.setVisibility(View.GONE);
        }
        return convertView;
	}
	
    public void setOnClickListener(ListInnerBtnOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
