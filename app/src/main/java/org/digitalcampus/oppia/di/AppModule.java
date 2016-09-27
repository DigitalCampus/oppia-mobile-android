package org.digitalcampus.oppia.di;

import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.CoursesRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {


    @Provides
    @Singleton
    public CoursesRepository provideCoursesRepository(){
        return new CoursesRepository();
    }

    @Provides
    @Singleton
    public CompleteCourseProvider providesCourse(){
        return new CompleteCourseProvider();

    }



}
