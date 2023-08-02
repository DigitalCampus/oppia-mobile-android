package org.digitalcampus.oppia.api

import android.content.Context

interface ApiEndpoint {
    fun getFullURL(ctx: Context?, apiPath: String?): String?
}