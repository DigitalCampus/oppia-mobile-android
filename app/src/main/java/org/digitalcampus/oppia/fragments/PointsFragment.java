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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.ScorecardActivity;
import org.digitalcampus.oppia.adapter.PointsListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Points;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

public class PointsFragment extends AppFragment implements TabLayout.BaseOnTabSelectedListener {

    public static final String TAG = PointsFragment.class.getSimpleName();

    private final int POSITION_TAB_LAST_YEAR = 0;
    private final int POSITION_TAB_LAST_MONTH = 1;
    private final int POSITION_TAB_LAST_WEEK = 2;

    @Inject
    List<Points> pointsFull;
    List<Points> pointsFiltered = new ArrayList<>();
    private Map<String, Integer> pointsGrouped = new LinkedHashMap<>(); // LinkedHashMap: ordered by insertion. TreeMap: sorts naturally by key
    private int totalPoints;
    private PointsListAdapter pointsAdapter;
    private ListView listView;
    private TextView tvTotalPoints;
    private TabLayout tabsFilterPoints;
    private LineChart chart;
    List<Integer> yVals = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    private int currentDatesRangePosition;
    private Course course;

    public static PointsFragment newInstance() {
        return new PointsFragment();
    }

    private void findViews() {
        listView = getView().findViewById(R.id.points_list);
        tvTotalPoints = getView().findViewById(R.id.tv_total_points);
        tabsFilterPoints = getView().findViewById(R.id.tabs_filter_points);
        chart = getView().findViewById(R.id.chart);

        tabsFilterPoints.addOnTabSelectedListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_points, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViews();
        initializeDagger();
        configureChart();

        course = ((ScorecardActivity) getActivity()).getCourse();

        loadPoints();

        pointsAdapter = new PointsListAdapter(super.getActivity(), pointsFiltered);
        listView.setAdapter(pointsAdapter);

        showPointsFiltered(POSITION_TAB_LAST_YEAR);


    }

    private void configureChart() {
        chart.setNoDataText(getString(R.string.no_points_data));
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);

        chart.setPinchZoom(true);

//        chart.setViewPortOffsets(40f, 0f, 40f, 0f);
        chart.offsetLeftAndRight(getResources().getDimensionPixelSize(R.dimen.offset_chart_horizontal));

        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisRight().setEnabled(false);
//        chart.getXAxis().setEnabled(false);

        chart.getAxisLeft().setAxisMinimum(0);

        XAxis xAxis = chart.getXAxis();

//        xAxis.setSpaceMin(data.getBarWidth()/2);
//        xAxis.setSpaceMax(data.getBarWidth()/2);

//        xAxis.setXOffset(widthBetweenObservations);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
//        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(40);
//        xAxis.setLabelCount(2);

    }

    private void showPointsFiltered(int position) {

        currentDatesRangePosition = position;

        DateTime initialDateTime = new DateTime();
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

        tvTotalPoints.setText(String.valueOf(totalPoints));

        pointsAdapter.notifyDataSetChanged();

        loadPlot();
    }

    private void loadPlot() {


        yVals.clear();
        labels.clear();
        chart.animateY(1000, Easing.EaseInOutQuad);

        for (Map.Entry<String, Integer> entry : pointsGrouped.entrySet()) {
            yVals.add(entry.getValue());
            labels.add(entry.getKey());
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < yVals.size(); i++) {
            entries.add(new Entry(i, yVals.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.highlight_light));
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(getGradientDrawable());

        LineData lineData = new LineData(dataSet);

        if (chart.getData() != null) {
            chart.clear();
        }


        chart.setData(lineData);
        chart.animateY(1000, Easing.EaseInOutQuad);

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(Math.min(entries.size(), 12));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Log.i(TAG, "getFormattedValue: enter. value: " + (int)value);
                try {
                    return labels.get((int) value);
                } catch (IndexOutOfBoundsException e) {
                    Log.i(TAG, "getFormattedValue: exception. value: " + (int)value);
                    return "MAL";
                }
//                return String.valueOf(value);
            }
        });

        chart.invalidate(); // refresh
    }

    private Drawable getGradientDrawable() {

        int colorStart = ContextCompat.getColor(getActivity(), R.color.highlight_light);
        int colorEnd = ContextCompat.getColor(getActivity(), R.color.highlight_mid);

        int alpha = 128;
        int colorStartAlpha = Color.argb(alpha, Color.red(colorStart), Color.green(colorStart), Color.blue(colorStart));
        int colorEndAlpha = Color.argb(alpha, Color.red(colorEnd), Color.green(colorEnd), Color.blue(colorEnd));

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {colorStartAlpha, colorEndAlpha});
        gd.setCornerRadius(0f);

        return gd;
    }


    private void groupPoints() {

        totalPoints = 0;

        pointsGrouped.clear();

        Calendar calendarIterate = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.set(Calendar.HOUR_OF_DAY, 23);
        calendarNow.set(Calendar.MINUTE, 59);
        calendarNow.set(Calendar.SECOND, 59);

        DateTimeFormatter datetimeFormatter = null;

        switch (currentDatesRangePosition) {
            case POSITION_TAB_LAST_WEEK:
                datetimeFormatter = MobileLearning.DATE_FORMAT_DAY_MONTH;
                calendarIterate.add(Calendar.DAY_OF_MONTH, -7);
                while (calendarIterate.before(calendarNow)) {
                    pointsGrouped.put(datetimeFormatter.print(calendarIterate.getTimeInMillis()), 0);
                    calendarIterate.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;

            case POSITION_TAB_LAST_MONTH:

                datetimeFormatter = MobileLearning.DATE_FORMAT_DAY_MONTH;
                calendarIterate.add(Calendar.MONTH, -1);
                while (calendarIterate.before(calendarNow)) {
                    pointsGrouped.put(datetimeFormatter.print(calendarIterate.getTimeInMillis()), 0);
                    calendarIterate.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;

            case POSITION_TAB_LAST_YEAR:

                datetimeFormatter = MobileLearning.MONTH_FORMAT;
                calendarIterate.add(Calendar.YEAR, -1);
                calendarIterate.add(Calendar.MONTH, 1);
                while (calendarIterate.before(calendarNow)) {
                    pointsGrouped.put(datetimeFormatter.print(calendarIterate.getTimeInMillis()), 0);
                    calendarIterate.add(Calendar.DAY_OF_MONTH, 1);
                }

                break;
        }

        for (Points point : pointsFiltered) {
            totalPoints += point.getPoints();
            int previousPoints = 0;
            String key = datetimeFormatter.print(point.getDateTime());
            if (pointsGrouped.containsKey(key)) {
                previousPoints = pointsGrouped.get(key);
            } else {
                Log.e(TAG, "groupPoints: this should not happen. Just in case avoids exception");
                continue;
            }

            int newPoints = previousPoints + point.getPoints();

            pointsGrouped.put(key, newPoints);
        }

    }

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getActivity().getApplication();
        app.getComponent().inject(this);
    }

    private void loadPoints() {
        DbHelper db = DbHelper.getInstance(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(super.getActivity()));
        pointsFull = db.getUserPoints(userId, course, false);

//        pointsFull = getMockPoints();
    }

    // Useful for testing
    private List<Points> getMockPoints() {

        List<Points> pointsMock = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);

        for (int i = 0; i < 366; i++) {

            Points mockPoint = new Points();
            mockPoint.setDateTime(MobileLearning.DATETIME_FORMAT.print(calendar.getTimeInMillis()));
            mockPoint.setPoints(new Random().nextInt(70));
            mockPoint.setEvent("Event mock " + i);
            mockPoint.setDescription("Description mock " + i);

            if (i % 13 == 0) {
                // to add some days with 0 points
                mockPoint.setPoints(0);
            }

            pointsMock.add(mockPoint);

            if (i % 7 == 0) {
                // to add some days with more than one number of points
                Points mockPointExtra = new Points();
                mockPointExtra.setDateTime(MobileLearning.DATETIME_FORMAT.print(calendar.getTimeInMillis()));
                mockPointExtra.setPoints(new Random().nextInt(70));
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

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
