package org.digitalcampus.oppia.di;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.ActivityLogRepository;
import org.digitalcampus.oppia.model.Badge;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    public static final String TAG = AppModule.class.getSimpleName();

    private Application app;

    public AppModule(Application app){
        this.app = app;
    }

    @Provides
    @Singleton
    public CoursesRepository provideCoursesRepository(){
        return new CoursesRepository();
    }

    @Provides
    @Singleton
    public CompleteCourseProvider provideCompleteCourseProvider(){
        return new CompleteCourseProvider();

    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    public User provideUser(){
        try {
            return DbHelper.getInstance(app).getUser(SessionManager.getUsername(app));
        } catch (UserNotFoundException e) {
            Mint.logException(e);
            Log.d(TAG, "User not found: ", e);
        }
        return new User();
    }

    @Provides
    public List<Points> providePointsList(){
        return new ArrayList<>();
    }

    @Provides
    public List<Badge> provideBadgesList(){
        return new ArrayList<>();
    }


    @Provides
    @Singleton
    public TagRepository provideTagRepository() {
        return new TagRepository();
    }

    @Provides
    @Singleton
    public ActivityLogRepository provideActivityLogRepository() {
        return new ActivityLogRepository();
    }


    @Provides
    @Singleton
    public CourseInstallRepository provideCourseInstallRepository() {
        return new CourseInstallRepository();
    }

    @Provides
    @Singleton
    public CourseInstallerServiceDelegate provideCourseInstallerServiceDelegate(){
        return new CourseInstallerServiceDelegate();
    }

    @Provides
    @Singleton
    public QuizAttemptRepository provideQuizAttemptRepository() {
        return new QuizAttemptRepository();
    }

    @Provides
    @Singleton
    public ApiEndpoint provideApiEndpoint() {
        return new RemoteApiEndpoint();
    }

    @Provides
    public CustomFieldsRepository provideProfileCustomFieldsList(){
        return new CustomFieldsRepository();
    }

}
