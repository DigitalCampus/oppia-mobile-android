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
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.PointsListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Points;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

public class PointsFragment extends AppFragment implements TabLayout.BaseOnTabSelectedListener {

    public static final String TAG = PointsFragment.class.getSimpleName();

    private final int POSITION_TAB_LAST_YEAR = 0;
    private final int POSITION_TAB_LAST_MONTH = 1;
    private final int POSITION_TAB_LAST_WEEK = 2;

    @Inject
    List<Points> pointsFull;
    List<Points> pointsFiltered = new ArrayList<>();
    private Map<String, Integer> pointsGroupedByDay = new TreeMap<>(); // sorts automatically
    private int totalPoints;
    private PointsListAdapter pointsAdapter;
    private ListView listView;
    private TextView tvTotalPoints;
    private TabLayout tabsFilterPoints;
    private LineChart chart;
    List<Integer> yVals = new ArrayList<>();
    List<String> labels = new ArrayList<>();

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

        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);

//        XAxis xAxis = chart.getXAxis();
//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
////                return labels.get((int) value);
//                return String.valueOf(value);
//            }
//        });
//
////        xAxis.setSpaceMin(data.getBarWidth()/2);
////        xAxis.setSpaceMax(data.getBarWidth()/2);
//
////        xAxis.setXOffset(widthBetweenObservations);
//        xAxis.setGranularity(1);
//        xAxis.setGranularityEnabled(true);
////        xAxis.setCenterAxisLabels(true);
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawGridLines(false);
//        xAxis.setLabelRotationAngle(40);
//        xAxis.setLabelCount(2);

    }

    private void showPointsFiltered(int position) {

        DateTime initialDateTime;
        switch (position) {
            case POSITION_TAB_LAST_YEAR:
                initialDateTime = new DateTime().minusYears(1);
                break;

            case POSITION_TAB_LAST_MONTH:
                initialDateTime = new DateTime().minusMonths(1);
                break;

            case POSITION_TAB_LAST_WEEK:
                initialDateTime = new DateTime().minusWeeks(1);
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

        groupPointsByDay();

        tvTotalPoints.setText(String.valueOf(totalPoints));

        pointsAdapter.notifyDataSetChanged();

        loadPlot();
    }

    private void loadPlot() {


        yVals.clear();
        labels.clear();
        chart.animateY(1000, Easing.EaseInOutQuad);

        for (Map.Entry<String, Integer> entry : pointsGroupedByDay.entrySet()) {
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
        dataSet.setFillDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_chart_fill_gradient));

        LineData lineData = new LineData(dataSet);

        if (chart.getData() != null) {
            chart.clearValues();
        }

        chart.setData(lineData);
        chart.animateY(1000, Easing.EaseInOutQuad);
        chart.invalidate(); // refresh

//		plotXY.clear();
//
//
//		XYSeries series = new SimpleXYSeries(yVals, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, null);
////		((SimpleXYSeries) series).setTitle(null);
//		PointLabeler pointLabeler = new PointLabeler() {
//			@Override
//			public String getLabel(XYSeries series, int index) {
//				return String.valueOf(yVals.get(index));
//			}
//		};
//
//		LineAndPointFormatter formatter =
//				new LineAndPointFormatter(getActivity(), R.xml.line_point_formatter_with_labels);
//		formatter.setPointLabeler(pointLabeler);
//
//		plotXY.addSeries(series, formatter);
//		plotXY.invalidate();
    }


    private void groupPointsByDay() {

        totalPoints = 0;

        pointsGroupedByDay.clear();

        for (Points point : pointsFiltered) {
            totalPoints += point.getPoints();
            int previousPoints = 0;
            if (pointsGroupedByDay.containsKey(point.getDateAsString())) {
                previousPoints = pointsGroupedByDay.get(point.getDateAsString());
            }

            previousPoints += point.getPoints();

            pointsGroupedByDay.put(point.getDateAsString(), previousPoints);
        }

    }

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getActivity().getApplication();
        app.getComponent().inject(this);
    }

    private void loadPoints() {
        DbHelper db = DbHelper.getInstance(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(super.getActivity()));
        pointsFull = db.getUserPoints(userId);
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
