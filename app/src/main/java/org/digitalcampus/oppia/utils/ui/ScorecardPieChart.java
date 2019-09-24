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

package org.digitalcampus.oppia.utils.ui;

import android.app.Activity;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;

public class ScorecardPieChart {

    public static final String TAG = ScorecardPieChart.class.getSimpleName();

    private CircularProgressBar circularProgressBar;

    private Activity activity;
    private Course course;

    public ScorecardPieChart(Activity activity, CircularProgressBar circularProgressBar, Course course) {
        this.activity = activity;
        this.circularProgressBar = circularProgressBar;
        this.course = course;
    }

    public void configureChart(int margin, float donutSize) {


    }

    public void animate() {


        int totalActivities = course.getNoActivities();
        if (totalActivities == 0) {
            return;
        }

        circularProgressBar.setProgressMax(totalActivities);
        circularProgressBar.setProgressWithAnimation(course.getNoActivitiesCompleted(), MobileLearning.SCORECARD_ANIM_DURATION);

    }

}
