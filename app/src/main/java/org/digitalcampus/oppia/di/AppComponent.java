package org.digitalcampus.oppia.di;

import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(OppiaMobileActivity activity);
    void inject(CourseIndexActivity activity);

    void inject(GlobalScorecardFragment fragment);
}
