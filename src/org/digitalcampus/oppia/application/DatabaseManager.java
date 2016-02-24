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

package org.digitalcampus.oppia.application;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseManager {

    private int mOpenCounter;

    private static DatabaseManager instance;
    private DbHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(DbHelper dbh) {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        instance.setDatabaseHelper(dbh);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if ((mOpenCounter == 1) && (mDatabaseHelper!=null)){
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        Log.d("Database", "conns:" + mOpenCounter);
        if(mOpenCounter == 0) {
            // Closing database
            if (mDatabase.isOpen()){
                mDatabase.close();
            }
            Log.d("Database", "Nulling dbHelper");
            mDatabaseHelper = null;
        }
    }

    private void setDatabaseHelper(DbHelper database) {
        this.mDatabaseHelper = database;
    }
}
