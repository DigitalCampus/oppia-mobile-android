package org.digitalcampus.oppia.di;

import org.digitalcampus.oppia.activity.OppiaMobileActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(OppiaMobileActivity activity);
}
