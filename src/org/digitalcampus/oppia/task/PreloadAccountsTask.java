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
import android.os.AsyncTask;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.PreloadAccountsListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PreloadAccountsTask extends AsyncTask<Payload, DownloadProgress, Payload>{

    public final static String TAG = PreloadAccountsTask.class.getSimpleName();
    private Context ctx;
    private PreloadAccountsListener mListener;

    private static final int CSV_COLUMNS = 3;
    private static final String CSV_SEPARATOR = ",";
    private static final int CSV_USERNAME_COLUMN = 0;
    private static final int CSV_PASSWORD_COLUMN = 1;
    private static final int CSV_APIKEY_COLUMN = 2;

    public PreloadAccountsTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Payload doInBackground(Payload... params) {
        Payload payload = new Payload();
        payload.setResult(false);
        String csvPath = Storage.getStorageLocationRoot(ctx) + File.separator + SessionManager.ACCOUNTS_CSV_FILENAME;
        File csvAccounts = new File(csvPath);

        if (csvAccounts.exists()){
            BufferedReader reader = null;
            try {
                String line;
                reader = new BufferedReader(new FileReader(csvAccounts));
                DbHelper db = DbHelper.getInstance(ctx);
                int usersAdded = 0;
                while ((line = reader.readLine()) != null) {
                    String[] rowData = line.split(CSV_SEPARATOR);

                    if (rowData.length < CSV_COLUMNS){
                        Log.d(TAG, "Bad csv line, ignoring: " + line);
                        continue;
                    }
                    User csvUser = new User();
                    csvUser.setUsername(rowData[CSV_USERNAME_COLUMN]);
                    csvUser.setPasswordEncrypted(rowData[CSV_PASSWORD_COLUMN]);
                    csvUser.setApiKey(rowData[CSV_APIKEY_COLUMN]);

                    db.addOrUpdateUser(csvUser);
                    usersAdded++;
                }

                if (usersAdded>0){
                    payload.setResult(true);
                    payload.setResultResponse(ctx.getString(R.string.info_startup_preloaded_accounts, usersAdded));
                }
                Log.d(TAG, usersAdded + " users added");
            }
            catch (IOException ex) {
                Mint.logException(ex);
                ex.printStackTrace();
                payload.setResult(true);
                payload.setResultResponse(ctx.getString(R.string.error_preloading_accounts));
            }
            finally {
                try {
                    if (reader!=null) reader.close();
                    boolean deleted = csvAccounts.delete();
                    Log.d(TAG, "CSV file " + (deleted?"":"not ") + "deleted");
                }
                catch (IOException e) {
                    Mint.logException(e);
                }
            }
        }

        return payload;
    }

    @Override
    protected void onPostExecute(Payload p) {
        synchronized (this) {
            if (mListener != null) {
                mListener.onPreloadAccountsComplete(p);
            }
        }
    }

    public void setPreloadAccountsListener(PreloadAccountsListener listener) {
        synchronized (this) {
            mListener = listener;
        }
    }
}
