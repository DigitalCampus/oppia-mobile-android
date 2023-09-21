package org.digitalcampus.oppia.analytics

import android.content.Context

class AnalyticsProvider {
    fun shouldShowOptOutRationale(ctx: Context): Boolean {
        return Analytics.shouldShowOptOutRationale(ctx)
    }
}