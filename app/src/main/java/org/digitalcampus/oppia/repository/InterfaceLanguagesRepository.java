package org.digitalcampus.oppia.repository;

import org.digitalcampus.mobile.learning.BuildConfig;

public class InterfaceLanguagesRepository {

    public String[] getLanguageOptions() {
        return BuildConfig.INTERFACE_LANGUAGE_OPTIONS.split(",");
    }
}
