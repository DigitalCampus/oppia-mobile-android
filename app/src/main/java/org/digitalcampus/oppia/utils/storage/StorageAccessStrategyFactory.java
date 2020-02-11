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

package org.digitalcampus.oppia.utils.storage;

import org.digitalcampus.oppia.activity.PrefsActivity;

public class StorageAccessStrategyFactory {

    private StorageAccessStrategyFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static StorageAccessStrategy createStrategy(String type){
        if ((type!=null)&&(type.equals(PrefsActivity.STORAGE_OPTION_INTERNAL))){
            return new InternalStorageStrategy();
        }
        else{
            return new ExternalStorageStrategy();
        }
    }
}
