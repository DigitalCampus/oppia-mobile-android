package org.digitalcampus.oppia.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.fragments.ScorecardFragment;

public class MainActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_bottom_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment fragment = null;

                switch (menuItem.getItemId()) {
                    case R.id.nav_bottom_home:
                        fragment = new CoursesListFragment();
                        break;

                    case R.id.nav_bottom_scorecard:
                        fragment = new ScorecardFragment();
                        break;

                    case R.id.nav_bottom_points:
                        fragment = new PointsFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();
                return true;
            }
        });
    }

}
