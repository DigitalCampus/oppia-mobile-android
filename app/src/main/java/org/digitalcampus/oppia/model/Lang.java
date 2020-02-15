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

package org.digitalcampus.oppia.model;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Lang implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = -8960131611429444591L;
    public static final String TAG = Lang.class.getSimpleName();
    private String language;
    private String content;
    private String location;

    public Lang(String lang, String content){
        this.setLanguage(lang);
        this.setContent(content);
    }


    public String getLanguage() {
        String[] langCountry = this.language.split("_|-");
        return langCountry[0];
    }

    public void setLanguage(String lang) {
        // only set the first part of the lang - not the full localisation
        String[] langCountry = lang.split("_|-");
        this.language = langCountry[0];
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;

        if (this.getClass() != obj.getClass())
            return false;

        return TextUtils.equals(getLanguage(), ((Lang) obj).getLanguage());
    }

    @Override
    public int hashCode() {
        return getLanguage() != null ? getLanguage().hashCode() : 0;
    }
}
