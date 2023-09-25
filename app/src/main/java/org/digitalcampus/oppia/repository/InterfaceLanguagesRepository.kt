package org.digitalcampus.oppia.repository

import org.digitalcampus.mobile.learning.BuildConfig

class InterfaceLanguagesRepository {
    fun getLanguageOptions(): List<String> {
        return BuildConfig.INTERFACE_LANGUAGE_OPTIONS.split(",")
    }
}