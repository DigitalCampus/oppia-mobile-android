/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterTask extends APIRequestTask<User, Object, EntityResult<User>> {

    private RegisterListener mStateListener;

    public RegisterTask(Context ctx) {
        super(ctx);
    }

    public RegisterTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    public interface RegisterListener {
        void onSubmitComplete(User u);

        void onSubmitError(String error);

        void onConnectionError(String error, User u);
    }

    private class UserSubmitError extends User {}

    @Override
    protected EntityResult<User> doInBackground(User... params) {

        User user = params[0];
        EntityResult<User> result = new EntityResult<>();
        result.setEntity(user);

        boolean saveUser = true;
        if (!user.isOfflineRegister()) {
            saveUser = submitUserToServer(user, result, true);
        }

        if (saveUser) {
            DbHelper db = DbHelper.getInstance(ctx);
            boolean usernameExists = db.isUser(user.getUsername()) != -1;
            if (!usernameExists) {
                // add or update user in db
                db.addOrUpdateUser(user);
                result.setSuccess(true);
                result.setResultMessage(ctx.getString(R.string.register_complete));
            } else {
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.register_username_exists));
                result.setEntity(new UserSubmitError());
            }
        }

        return result;
    }


    boolean submitUserToServer(User u, EntityResult<User> result, boolean updateProgress) {
        try {
            if (updateProgress) {
                // update progress dialog
                publishProgress(ctx.getString(R.string.register_process));
            }

            // add post params
            JSONObject json = new JSONObject();
            json.put("username", u.getUsername());
            json.put("password", u.getPassword());
            json.put("passwordagain", u.getPasswordAgain());
            json.put("email", u.getEmail());
            json.put("first_name", u.getFirstname());
            json.put("last_name", u.getLastname());
            json.put("jobtitle", u.getJobTitle());
            json.put("organisation", u.getOrganisation());
            json.put("phoneno", u.getPhoneNo());

            List<CustomField> cFields = DbHelper.getInstance(ctx).getCustomFields();
            for (CustomField field : cFields) {
                CustomValue value = u.getCustomField(field.getKey());
                if (value != null) {
                    json.put(field.getKey(), value.getValue());
                }
            }

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, Paths.REGISTER_PATH))
                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            // make request
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject jsonResp = new JSONObject(response.body().string());
                u.setApiKey(jsonResp.getString("api_key"));
                u.setOfflineRegister(false);
                u.setBadges(getBadges(jsonResp));
                u.setScoringEnabled(getScoringEnabled(jsonResp));
                u.setBadgingEnabled(getBadgingEnabled(jsonResp));
                u.setFirstname(jsonResp.getString("first_name"));
                u.setLastname(jsonResp.getString("last_name"));
                saveMetaData(jsonResp);
                return true;
            } else if (response.code() == 400) {
                String bodyResponse = response.body().string();
                result.setSuccess(false);
                result.setEntity(new UserSubmitError());
                result.setResultMessage(bodyResponse);
                Log.d(TAG, bodyResponse);
            } else {
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.error_connection));
            }

        } catch (javax.net.ssl.SSLHandshakeException e) {
            Log.d(TAG, "SSLHandshakeException:", e);
            Analytics.logException(e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
        } catch (UnsupportedEncodingException e) {
            result.setSuccess(false);

            result.setResultMessage(ctx.getString(R.string.error_connection));
        } catch (IOException e) {
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_required));
        } catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "JSONException:", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_processing_response));
        }
        return false;
    }

    private int getBadges(JSONObject jsonResp) {
        try {
            return jsonResp.getInt("badges");
        } catch (JSONException e) {
            return 0;
        }
    }

    private boolean getScoringEnabled(JSONObject jsonResp) {
        try {
            return jsonResp.getBoolean("scoring");
        } catch (JSONException e) {
            return true;
        }
    }

    private boolean getBadgingEnabled(JSONObject jsonResp) {
        try {
            return jsonResp.getBoolean("badging");
        } catch (JSONException e) {
            return true;
        }
    }

    private void saveMetaData(JSONObject jsonResp) {
        try {
            JSONObject metadata = jsonResp.getJSONObject("metadata");
            MetaDataUtils mu = new MetaDataUtils(ctx);
            mu.saveMetaData(metadata, prefs);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException:", e);
        }
    }

    @Override
    protected void onPostExecute(EntityResult<User> result) {
        synchronized (this) {
            if (mStateListener != null) {
                User user = result.getEntity();
                String errorMessage = result.getResultMessage();

                if (user instanceof UserSubmitError) {
                    try {
                        JSONObject jo = new JSONObject(errorMessage);
                        errorMessage = jo.getString("error");
                    } catch (JSONException je) {
                        Log.d(TAG, je.getMessage());
                    }
                    mStateListener.onSubmitError(errorMessage);
                    return;
                    
                }

                if (result.isSuccess()) {
                    mStateListener.onSubmitComplete(user);
                } else {
                    mStateListener.onConnectionError(errorMessage, user);
                } 


            }
        }
    }

    public void setRegisterListener(RegisterListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}
