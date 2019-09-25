package org.digitalcampus.oppia.di;

import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.fragments.ActivitiesFragment;
import org.digitalcampus.oppia.fragments.AppFragment;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.model.User;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(CourseIndexActivity activity);
    void inject(TagSelectActivity activity);
    void inject(DownloadActivity activity);
    void inject(AppActivity activity);

    void inject(GlobalScorecardFragment fragment);
    void inject(PointsFragment fragment);
    void inject(ActivitiesFragment fragment);
    void inject(BadgesFragment fragment);
    void inject(CoursesListFragment fragment);
    void inject(AppFragment fragment);

    User getUser();
}
