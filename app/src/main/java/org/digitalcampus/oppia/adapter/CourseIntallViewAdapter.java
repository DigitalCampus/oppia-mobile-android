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

import android.util.Log;

import org.digitalcampus.oppia.model.Course;

public class CourseIntallViewAdapter extends Course {

	private static final long serialVersionUID = -4251898143809197224L;

	public CourseIntallViewAdapter(String root) {
        super(root);
    }

    //Extension for UI purposes
    private boolean downloading;
    private boolean installing;
    private int progress;

    private String authorUsername;
    private String authorName;

    public boolean isDownloading() {
        return downloading;
    }
    public void setDownloading(boolean downloading) { this.downloading = downloading; }

    public boolean isInstalling() {
        return installing;
    }
    public void setInstalling(boolean installing) {
        this.installing = installing;
    }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getDisplayAuthorName(){
        if ((authorName == null) && (authorUsername == null)) return null;
        String displayName = authorName == null ? "" : authorName;
        if (authorUsername != null){
            if (authorName != null)
                displayName += " (@" + authorUsername + ")";
            else
                displayName += authorUsername;
        }
        Log.d(TAG, "AAAAAAAAAAAAA");
        return displayName;
    }
}
