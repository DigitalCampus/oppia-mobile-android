package org.digitalcampus.oppia.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentAboutBinding;
import org.digitalcampus.mobile.learning.databinding.FragmentTabsBinding;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabsFragment extends AppFragment {

    private FragmentTabsBinding binding;

    public TabsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void configureFragments(List<Fragment> fragments, List<String> tabTitles) {

        ActivityPagerAdapter apAdapter = new ActivityPagerAdapter(getActivity(),
                getActivity().getSupportFragmentManager(), fragments, tabTitles);
        binding.viewPager.setAdapter(apAdapter);
        binding.tabs.setupWithViewPager(binding.viewPager);
        apAdapter.updateTabViews(binding.tabs);
    }

}
