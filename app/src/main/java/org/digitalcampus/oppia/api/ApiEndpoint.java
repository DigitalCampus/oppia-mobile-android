package org.digitalcampus.oppia.api;

import android.content.Context;

public interface ApiEndpoint {
    String getFullURL(Context ctx, String apiPath);
}
