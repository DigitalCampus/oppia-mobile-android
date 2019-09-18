package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.digitalcampus.mobile.learning.R;


public class ScorecardFragment extends AppFragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_scorecard, container, false);


        return layout;
    }
}