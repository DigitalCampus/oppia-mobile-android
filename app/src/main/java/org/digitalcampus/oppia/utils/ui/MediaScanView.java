package org.digitalcampus.oppia.utils.ui;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.florent37.viewanimator.ViewAnimator;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ViewMediaScanBinding;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScanMediaTask;

import java.util.ArrayList;
import java.util.List;

public class MediaScanView extends FrameLayout implements ScanMediaListener {

    private ViewMediaScanBinding binding;
    private SharedPreferences prefs;
    private View viewBelow;
    private int initialViewBelowPadding = 0;

    public MediaScanView(@NonNull Context context) {
        super(context);
        init();
    }


    public MediaScanView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaScanView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        binding = ViewMediaScanBinding.inflate(LayoutInflater.from(getContext()));
        addView(binding.getRoot());

        setVisibility(GONE);
    }

    public void setViewBelow(View viewBelow) {
        this.viewBelow = viewBelow;
        initialViewBelowPadding = viewBelow.getPaddingTop();
    }

    //region ScanMedia
    ///Everything related to the ScanMediaTask, including UI management

    public void scanMedia(List<Course> courses) {

        if (Media.shouldScanMedia(prefs)) {
            ScanMediaTask task = new ScanMediaTask(getContext());
            Payload p = new Payload(courses);
            task.setScanMediaListener(this);
            task.execute(p);
        } else {
            hideView();
        }
    }

    private void showView() {

        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        startAnimation(anim);

        binding.getRoot().measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(initialViewBelowPadding, binding.getRoot().getMeasuredHeight());
        animator.addUpdateListener(valueAnimator -> viewBelow.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0));
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();

        setVisibility(VISIBLE);
    }

    private void hideView() {
        setVisibility(View.GONE);
        viewBelow.setPadding(0, initialViewBelowPadding, 0, 0);

    }

    //    private void animateScanMediaMessage(){
//        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
//        anim.setDuration(900);
//        messageContainer.startAnimation(anim);
//
//        messageContainer.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        ValueAnimator animator = ValueAnimator.ofInt(initialCourseListPadding, messageContainer.getMeasuredHeight());
//        animator.addUpdateListener(valueAnimator -> recyclerCourses.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0));
//        animator.setStartDelay(200);
//        animator.setDuration(700);
//        animator.start();
//    }
//
//    private void hideScanMediaMessage(){
//        messageContainer.setVisibility(View.GONE);
//        recyclerCourses.setPadding(0, initialCourseListPadding, 0, 0);
//    }

    /* ScanMediaListener implementation */
    public void scanStart() {
        binding.tvMediaMessage.setText(R.string.info_scan_media_start);
    }

    public void scanProgressUpdate(String msg) {
        binding.tvMediaMessage.setText(getContext().getString(R.string.info_scan_media_checking, msg));
    }

    public void scanComplete(Payload response) {

        if (response.getResponseData() != null && !response.getResponseData().isEmpty()) {

            binding.tvMediaMessage.setText(R.string.info_scan_media_missing);
            binding.btnMediaDownload.setText(R.string.scan_media_download_button);
            binding.btnMediaDownload.setTag(response.getResponseData());

            binding.btnMediaDownload.setOnClickListener(view -> {
                @SuppressWarnings("unchecked")
                ArrayList<Object> m = (ArrayList<Object>) view.getTag();
                Intent i = new Intent(getContext(), DownloadMediaActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(DownloadMediaActivity.MISSING_MEDIA, m);
                i.putExtras(tb);
                getContext().startActivity(i);
            });

            if (getVisibility() != View.VISIBLE) {

                showView();
//                animateScanMediaMessage();
            }

            Media.resetMediaScan(prefs);
        } else {
//            hideScanMediaMessage();
            hideView();
            binding.btnMediaDownload.setOnClickListener(null);
            binding.btnMediaDownload.setTag(null);
            Media.updateMediaScan(prefs);
        }
    }


}
