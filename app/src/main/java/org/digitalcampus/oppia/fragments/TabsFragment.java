package org.digitalcampus.oppia.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabsFragment extends AppFragment {

    private TabLayout tabs;
    private ViewPager viewPager;

    private void findViews(View layout) {
        tabs = layout.findViewById( R.id.tabs );
        viewPager = layout.findViewById( R.id.view_pager );
    }



    public TabsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_tabs, container, false);
        findViews(layout);



        return layout;
    }

    public void configureFragments(List<Fragment> fragments, List<String> tabTitles) {

        ActivityPagerAdapter apAdapter = new ActivityPagerAdapter(getActivity(),
                getActivity().getSupportFragmentManager(), fragments, tabTitles);
        viewPager.setAdapter(apAdapter);
        tabs.setupWithViewPager(viewPager);
        apAdapter.updateTabViews(tabs);
    }

}
