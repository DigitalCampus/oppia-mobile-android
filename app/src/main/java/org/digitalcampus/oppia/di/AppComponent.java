package org.digitalcampus.oppia.di;

import org.digitalcampus.oppia.activity.ActivityLogActivity;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.EditProfileActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.database.DBMigration;
import org.digitalcampus.oppia.fragments.ActivitiesFragment;
import org.digitalcampus.oppia.fragments.AppFragment;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.GlobalQuizAttemptsFragment;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;
import org.digitalcampus.oppia.fragments.LeaderboardFragment;
import org.digitalcampus.oppia.fragments.LoginFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.fragments.RegisterFragment;
import org.digitalcampus.oppia.fragments.ResetFragment;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.NoCourseDownloadedManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(CourseIndexActivity activity);
    void inject(TagSelectActivity activity);
    void inject(ViewDigestActivity activity);
    void inject(ActivityLogActivity activity);
    void inject(DownloadActivity activity);
    void inject(AppActivity activity);
    void inject(PrefsActivity activity);
    void inject(CourseQuizAttemptsActivity activity);
    void inject(LoginFragment fragment);
    void inject(ResetFragment fragment);
    void inject(GlobalScorecardFragment fragment);
    void inject(PointsFragment fragment);
    void inject(LeaderboardFragment fragment);
    void inject(ActivitiesFragment fragment);
    void inject(BadgesFragment fragment);
    void inject(CoursesListFragment fragment);
    void inject(AppFragment fragment);
    void inject(GlobalQuizAttemptsFragment fragment);
    void inject(RegisterFragment fragment);

    void inject(EditProfileActivity activity);

    void inject(AdminSecurityManager adminSecurityManager);

    void inject(NoCourseDownloadedManager noCourseDownloadedManager);

    User getUser();

    void inject(DBMigration dbMigration);
}
