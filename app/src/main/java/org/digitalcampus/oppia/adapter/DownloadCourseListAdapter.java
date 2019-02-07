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

public class DownloadCourseListAdapter extends ArrayAdapter<CourseIntallViewAdapter>{

	public static final String TAG = DownloadCourseListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<CourseIntallViewAdapter> courseList;
	private SharedPreferences prefs;

    private String updateDescription;
    private String updateSchedDescription;
    private String installDescription;
    private String installedDescription;
    private String cancelDescription;

    private ListInnerBtnOnClickListener onClickListener;
	
	public DownloadCourseListAdapter(Activity context, ArrayList<CourseIntallViewAdapter> courseList) {
		super(context, R.layout.course_download_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        updateDescription = ctx.getString(R.string.update);
        installDescription = ctx.getString(R.string.install);
        installedDescription = ctx.getString(R.string.installed);
        cancelDescription = ctx.getString(R.string.cancel);
        updateSchedDescription = ctx.getString(R.string.update_schedule);
	}

    static class DownloadCourseViewHolder{
        TextView courseTitle;
        TextView courseDraft;
        TextView courseDescription;
        ImageButton actionBtn;
        ProgressBar actionProgress;
        TextView courseAuthor;
        TextView labelAuthor;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        DownloadCourseViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.course_download_row, parent, false);
            viewHolder = new DownloadCourseViewHolder();
            viewHolder.courseTitle = convertView.findViewById(R.id.course_title);
            viewHolder.courseDraft = convertView.findViewById(R.id.course_draft);
            viewHolder.courseDescription = convertView.findViewById(R.id.course_description);
            viewHolder.actionBtn = convertView.findViewById(R.id.download_course_btn);
            viewHolder.actionProgress = convertView.findViewById(R.id.download_progress);
            viewHolder.courseAuthor = convertView.findViewById(R.id.course_author);
            viewHolder.labelAuthor = convertView.findViewById(R.id.label_author);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (DownloadCourseViewHolder) convertView.getTag();
        }

        CourseIntallViewAdapter c = courseList.get(position);

        viewHolder.courseTitle.setText(c.getMultiLangInfo().getTitle(
                prefs.getString(PrefsActivity.PREF_LANGUAGE,
                Locale.getDefault().getLanguage())));

	    if (c.isDraft()){
            viewHolder.courseDraft.setVisibility(View.VISIBLE);
            viewHolder.courseDraft.setText(ctx.getString(R.string.course_draft));
	    } else {
            viewHolder.courseDraft.setVisibility(View.GONE);
	    }

	    String desc = c.getMultiLangInfo().getDescription(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
	    if (desc != null){
            viewHolder.courseDescription.setVisibility(View.VISIBLE);
            viewHolder.courseDescription.setText(desc);
	    } else {
            viewHolder.courseDescription.setVisibility(View.GONE);
	    }
        
        String author = c.getDisplayAuthorName();
        if ((author != null) && !(c.isDownloading() || c.isInstalling())){
            viewHolder.labelAuthor.setVisibility(View.VISIBLE);
            viewHolder.courseAuthor.setVisibility(View.VISIBLE);
            viewHolder.courseAuthor.setText(author);
        }
        else{
            viewHolder.labelAuthor.setVisibility(View.GONE);
            viewHolder.courseAuthor.setVisibility(View.GONE);
        }

        if (c.isDownloading() || c.isInstalling()){
            viewHolder.actionBtn.setImageResource(R.drawable.ic_action_cancel);
            viewHolder.actionBtn.setContentDescription(cancelDescription);
            viewHolder.actionBtn.setEnabled(!c.isInstalling());

            viewHolder.actionProgress.setVisibility(View.VISIBLE);
            if (c.getProgress()>0){
                viewHolder.actionProgress.setIndeterminate(false);
                viewHolder.actionProgress.setProgress(c.getProgress());
            }
            else {
                viewHolder.actionProgress.setIndeterminate(true);
            }
        }
	    else{
            viewHolder.actionProgress.setVisibility(View.GONE);
            if(c.isInstalled()){
                if(c.isToUpdate()){
                    viewHolder.actionBtn.setImageResource(R.drawable.ic_action_refresh);
                    viewHolder.actionBtn.setContentDescription(updateDescription);
                    viewHolder.actionBtn.setEnabled(true);
                } else if (c.isToUpdateSchedule()){
                    viewHolder.actionBtn.setImageResource(R.drawable.ic_action_refresh);
                    viewHolder.actionBtn.setContentDescription(updateSchedDescription);
                    viewHolder.actionBtn.setEnabled(true);
                } else {
                    viewHolder.actionBtn.setImageResource(R.drawable.ic_action_accept);
                    viewHolder.actionBtn.setContentDescription(installedDescription);
                    viewHolder.actionBtn.setEnabled(false);
                }
            } else {
                viewHolder.actionBtn.setImageResource(R.drawable.ic_action_download);
                viewHolder.actionBtn.setContentDescription(installDescription);
                viewHolder.actionBtn.setEnabled(true);
            }
        }

        viewHolder.actionBtn.setTag(position); //For passing the list item index
        viewHolder.actionBtn.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if(onClickListener != null)
                    onClickListener.onClick((Integer) v.getTag());
            }
        });

	    return convertView;
	}

    public void setOnClickListener(ListInnerBtnOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
