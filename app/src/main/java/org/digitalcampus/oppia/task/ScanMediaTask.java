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

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.task.result.EntityListResult;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanMediaTask extends AsyncTask<List<Course>, String, EntityListResult<Media>> {

    public static final String TAG = ScanMediaTask.class.getSimpleName();
    private ScanMediaListener mStateListener;
    private final Context ctx;

    private List<String> downloadingMedia;

    public ScanMediaTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected EntityListResult<Media> doInBackground(List<Course>... params) {

        EntityListResult<Media> result = new EntityListResult<>();

        downloadingMedia = DownloadService.getTasksDownloading();
        DbHelper db = DbHelper.getInstance(ctx);

        List<Course> courseObjs = params[0];
        for (int i = 0; i < courseObjs.size(); i++) {
            Course course = (Course) courseObjs.get(i);
            List<Media> courseMedia = db.getCourseMedia(course.getCourseId());
            for (Media m : courseMedia) {
                publishProgress(m.getFilename());
                checkMedia(course, m, result);
            }
        }

        return result;
    }

    private void checkMedia(Course course, Media media, EntityListResult<Media> result) {
        String filename = Storage.getMediaPath(ctx) + media.getFilename();
        File mediaFile = new File(filename);
        if (!mediaFile.exists() ||
                (downloadingMedia != null && downloadingMedia.contains(media.getDownloadUrl()))) {
            // check media not already in list
            boolean add = true;
            for (Media currentMedia : result.getEntityList()) {
                //We have to add it if there is not other object with that filename
                add = !currentMedia.getFilename().equals(media.getFilename());
                if (!add) {
                    currentMedia.getCourses().add(course);
                    break;
                }
            }
            if (add) {
                media.getCourses().add(course);
                if (downloadingMedia != null && downloadingMedia.contains(media.getDownloadUrl())) {
                    media.setDownloading(true);
                }
                result.getEntityList().add(media);
                result.setSuccess(true);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.scanStart();
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress
                mStateListener.scanProgressUpdate(progress[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(EntityListResult<Media> result) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.scanComplete(result);
            }
        }
    }

    public void setScanMediaListener(ScanMediaListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
