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

package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityTypesAdapter;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.model.ActivityCount;
import org.digitalcampus.oppia.model.ActivityType;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ActivitiesFragment extends AppFragment implements TabLayout.OnTabSelectedListener, ActivityTypesAdapter.OnItemClickListener {

    private static final String ARG_COURSE = "arg_course";

    private static final int DURATION_CHART_Y_VALUES_ANIMATION = 1000;

    private static final int POSITION_TAB_LAST_WEEK = 0;
    private static final int POSITION_TAB_LAST_MONTH = 1;
    private static final int POSITION_TAB_LAST_YEAR = 2;

    @Inject
    List<Points> pointsFull;
    private List<Points> pointsFiltered = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private Map<String, ActivityCount> activitiesGrouped = new LinkedHashMap<>(); // LinkedHashMap: ordered by insertion. TreeMap: sorts naturally by key
    private LineChart chart;
    private int currentDatesRangePosition;
    private Course course;
    private RecyclerView recyclerActivityTypes;
    private ArrayList<ActivityType> activityTypes;

    public static ActivitiesFragment newInstance(Course course) {
        ActivitiesFragment fragment = new ActivitiesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment ;
    }

    private void findViews() {
        recyclerActivityTypes = getView().findViewById(R.id.recycler_activity_types);
        TabLayout tabsFilterPoints = getView().findViewById(R.id.tabs_filter_points);
        chart = getView().findViewById(R.id.chart);

        tabsFilterPoints.addOnTabSelectedListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activities, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViews();
        getAppComponent().inject(this);
        configureActivityTypes();
        configureChart();

        course = (Course) getArguments().getSerializable(ARG_COURSE);

        loadPoints();
        showPointsFiltered(POSITION_TAB_LAST_WEEK);

    }

    private void configureActivityTypes() {
        activityTypes = new ArrayList<>();
        activityTypes.add(new ActivityType(getString(R.string.event_activity_all), ActivityType.ALL,
                ContextCompat.getColor(getActivity(), R.color.chart_line_activity_all), true));
        activityTypes.add(new ActivityType(getString(R.string.event_activity_completed), Gamification.EVENT_NAME_ACTIVITY_COMPLETED,
                ContextCompat.getColor(getActivity(), R.color.chart_line_activity_completed), false));
        activityTypes.add(new ActivityType(getString(R.string.event_media_watched), Gamification.EVENT_NAME_MEDIA_PLAYED,
                ContextCompat.getColor(getActivity(), R.color.chart_line_media_watched), false));
        activityTypes.add(new ActivityType(getString(R.string.event_course_downloaded), Gamification.EVENT_NAME_COURSE_DOWNLOADED,
                ContextCompat.getColor(getActivity(), R.color.chart_line_course_downloaded), false));
        activityTypes.add(new ActivityType(getString(R.string.event_quiz_attempt), Gamification.EVENT_NAME_QUIZ_ATTEMPT,
                ContextCompat.getColor(getActivity(), R.color.chart_line_quiz_attempt), false));

        ActivityTypesAdapter adapterActivityTypes = new ActivityTypesAdapter(getActivity(), activityTypes);
        adapterActivityTypes.setOnItemClickListener(this);
        recyclerActivityTypes.setAdapter(adapterActivityTypes);
    }

    private void configureChart() {
        chart.setNoDataText(getString(R.string.no_points_data));
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);

        chart.setPinchZoom(true);

        chart.offsetLeftAndRight(getResources().getDimensionPixelSize(R.dimen.offset_chart_horizontal));

        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisRight().setEnabled(false);

        chart.getAxisLeft().setAxisMinimum(0);

        XAxis xAxis = chart.getXAxis();

        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(40);

    }

    private void showPointsFiltered(int position) {

        currentDatesRangePosition = position;

        DateTime initialDateTime;
        DateTime initialNowAtEndOfDay = new DateTime();
        initialNowAtEndOfDay.withHourOfDay(23);
        initialNowAtEndOfDay.withMinuteOfHour(59);
        initialNowAtEndOfDay.withSecondOfMinute(59);
        switch (currentDatesRangePosition) {
            case POSITION_TAB_LAST_YEAR:
                initialDateTime = initialNowAtEndOfDay.minusYears(1);
                break;

            case POSITION_TAB_LAST_MONTH:
                initialDateTime = initialNowAtEndOfDay.minusMonths(1);
                break;

            case POSITION_TAB_LAST_WEEK:
                initialDateTime = initialNowAtEndOfDay.minusWeeks(1);
                break;

            default:
                throw new IllegalStateException("Tab position invalid");
        }

        pointsFiltered.clear();

        for (Points point : pointsFull) {
            if (point.getDateTime().isAfter(initialDateTime.toInstant())) {
                pointsFiltered.add(point);
            }
        }

        groupPoints();

        loadPlot(true);
    }

    private void groupPoints() {

        activitiesGrouped.clear();

        Calendar calendarIterate = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.set(Calendar.HOUR_OF_DAY, 23);
        calendarNow.set(Calendar.MINUTE, 59);
        calendarNow.set(Calendar.SECOND, 59);

        DateTimeFormatter datetimeFormatter;

        switch (currentDatesRangePosition) {
            case POSITION_TAB_LAST_WEEK:
                datetimeFormatter = DateUtils.DATE_FORMAT_DAY_MONTH;
                calendarIterate.add(Calendar.DAY_OF_MONTH, -7);
                while (calendarIterate.before(calendarNow)) {
                    activitiesGrouped.put(datetimeFormatter.print(calendarIterate.getTimeInMillis()), ActivityCount.initialize(activityTypes));
                    calendarIterate.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;

            case POSITION_TAB_LAST_MONTH:

                datetimeFormatter = DateUtils.DATE_FORMAT_DAY_MONTH;
                calendarIterate.add(Calendar.MONTH, -1);
                while (calendarIterate.before(calendarNow)) {
                    activitiesGrouped.put(datetimeFormatter.print(calendarIterate.getTimeInMillis()), ActivityCount.initialize(activityTypes));
                    calendarIterate.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;

            case POSITION_TAB_LAST_YEAR:

                datetimeFormatter = DateUtils.MONTH_FORMAT;
                calendarIterate.add(Calendar.YEAR, -1);
                calendarIterate.add(Calendar.MONTH, 1);
                while (calendarIterate.before(calendarNow)) {
                    activitiesGrouped.put(datetimeFormatter.print(calendarIterate.getTimeInMillis()), ActivityCount.initialize(activityTypes));
                    calendarIterate.add(Calendar.DAY_OF_MONTH, 1);
                }

                break;

                default:
                    throw new IllegalArgumentException("currentDatesRangePosition not valid: " + currentDatesRangePosition);
        }

        for (Points point : pointsFiltered) {

            String key = datetimeFormatter.print(point.getDateTime());
            if (activitiesGrouped.containsKey(key)) {
                if (activitiesGrouped.get(key).hasValidEvent(point.getEvent())) {
                    activitiesGrouped.get(key).incrementNumberActivityType(point.getEvent());
                    activitiesGrouped.get(key).incrementNumberActivityType(ActivityType.ALL);
                }
            } else {
                Log.e(TAG, "groupPoints: this should not happen. Just in case avoids exception");
            }

        }

    }

    private void loadPlot(boolean animate) {

        for (ActivityType activityType : activityTypes) {
            activityType.getValues().clear();
        }

        loadLabels();

        LineData lineData = new LineData();
        int maxYValue = 0;
        for (ActivityType activityType : activityTypes) {

            if (!activityType.isEnabled()) {
                continue;
            }

            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < activityType.getValues().size(); i++) {
                int yValue = activityType.getValues().get(i);
                entries.add(new Entry(i, yValue));

                maxYValue = Math.max(maxYValue, yValue);
            }

            LineDataSet dataSet = new LineDataSet(entries, activityType.getName()); // add entries to dataset
            dataSet.setColor(activityType.getColor());
            dataSet.setDrawValues(false);
            dataSet.setDrawCircles(false);
            dataSet.setLineWidth(getResources().getDimension(R.dimen.width_line_chart_activities));
            lineData.addDataSet(dataSet);
        }

        if (chart.getData() != null) {
            chart.clear();
        }
        chart.setData(lineData);

        configureAxis(maxYValue);
        chart.invalidate(); // refresh

        if (animate) {
            chart.animateY(DURATION_CHART_Y_VALUES_ANIMATION, Easing.EaseInOutQuad);
        }

    }

    private void loadLabels(){
        labels.clear();
        for (Map.Entry<String, ActivityCount> entryMap : activitiesGrouped.entrySet()) {
            labels.add(entryMap.getKey());
            ActivityCount activityCount = entryMap.getValue();
            for (ActivityType activityType : activityTypes) {
                if (!activityType.isEnabled()) {
                    continue;
                }
                int value = activityCount.getValueForType(activityType.getType());
                activityType.getValues().add(value);
            }
        }
    }

    private void configureAxis(int maxYValue){
        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(Math.min(labels.size(), 12));
        xAxis.setValueFormatter(new ValueFormatter(){
            @Override
            public String getFormattedValue(float value) {

                Log.i(TAG, "getFormattedValue: enter. value: " + (int) value);
                try {
                    return labels.get((int) value);
                } catch (IndexOutOfBoundsException e) {
                    Log.i(TAG, "getFormattedValue: exception. value: " + (int) value);
                    return "MAL";
                }
            }
        });

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setLabelCount(Math.min(maxYValue, 10));
        yAxis.setValueFormatter(new ValueFormatter(){

            @Override
            public String getFormattedValue(float value) {
                // Hide decimal values
                boolean hasDecimals = value % 1 != 0;
                return hasDecimals ? "" : String.valueOf((int) value);
            }
        });
        yAxis.setSpaceTop(0);

    }

    private void loadPoints() {
        DbHelper db = DbHelper.getInstance(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(super.getActivity()));
        pointsFull = db.getUserPoints(userId, course, true);
    }

    // Useful for testing
    private List<Points> getMockPoints() {

        List<Points> pointsMock = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);

        String[] eventTypes = new String[]{Gamification.EVENT_NAME_ACTIVITY_COMPLETED, Gamification.EVENT_NAME_MEDIA_PLAYED,
                Gamification.EVENT_NAME_COURSE_DOWNLOADED, Gamification.EVENT_NAME_QUIZ_ATTEMPT};

        for (int i = 0; i < 366; i++) {

            Points mockPoint = new Points();
            mockPoint.setDateTime(DateUtils.DATETIME_FORMAT.print(calendar.getTimeInMillis()));
            int random = new Random().nextInt(70);
            mockPoint.setPointsAwarded(random);
            mockPoint.setEvent(eventTypes[random % eventTypes.length]); // random event type
            mockPoint.setDescription("Description mock " + i);

            if (i % 13 == 0) {
                // to add some days with 0 points
                mockPoint.setPointsAwarded(0);
            }

            pointsMock.add(mockPoint);

            if (i % 7 == 0) {
                // to add some days with more than one number of points
                Points mockPointExtra = new Points();
                mockPointExtra.setDateTime(DateUtils.DATETIME_FORMAT.print(calendar.getTimeInMillis()));
                mockPointExtra.setPointsAwarded(new Random().nextInt(70));
                mockPointExtra.setEvent("Event extra " + i);
                mockPointExtra.setDescription("Description extra " + i);
                pointsMock.add(mockPointExtra);
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return pointsMock;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        showPointsFiltered(tab.getPosition());

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // do nothing
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // do nothing
    }

    // Event types list
    @Override
    public void onItemClick(int position, String type, boolean enabled) {

        loadPlot(false);
    }
}
