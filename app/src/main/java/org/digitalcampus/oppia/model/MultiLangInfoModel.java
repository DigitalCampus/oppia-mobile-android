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

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class MultiLangInfoModel implements Serializable {

    public static final String TAG = MultiLangInfoModel.class.getSimpleName();

    private List<Lang> langs = new ArrayList<>();
    private List<Lang> titles = new ArrayList<>();
    private List<Lang> descriptions = new ArrayList<>();

    public static final String DEFAULT_NOTITLE = "No title set";

    public String getTitle(String lang) {
        String title = getInfo(lang, titles);
        return title == null ? DEFAULT_NOTITLE : title.trim();
    }

    public String getTitle(SharedPreferences prefs){
        return this.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
    }

    public void setTitles(List<Lang> titles) {
        this.titles = titles;
    }

    public void setTitlesFromJSONString(String jsonStr) {
        setInfoFromJSONString(jsonStr, this.titles, false);
    }

    public String getTitleJSONString(){
        return getInfoJSONString(this.titles);
    }

    public String getDescription(String lang) {
        return getInfo(lang, descriptions);
    }

    public String getDescription(SharedPreferences prefs){
        return this.getDescription(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
    }

    public void setDescriptions(List<Lang> descriptions) {
        this.descriptions = descriptions;
    }

    public void setDescriptionsFromJSONString(String jsonStr) {
        setInfoFromJSONString(jsonStr, this.descriptions, false);
    }

    public String getDescriptionJSONString(){
        return getInfoJSONString(this.descriptions);
    }

    public List<Lang> getLangs() {
        return langs;
    }
    public void setLangs(List<Lang> langs) {
        this.langs = langs;
    }

    public String getLangsJSONString(){
        return getInfoJSONString(this.langs);
    }

    public void setLangsFromJSONString(String jsonStr) {
        setInfoFromJSONString(jsonStr, this.langs, true);
    }

    private String getInfo(String lang, List<Lang> values){
        for(Lang l: values){
            if(l.getLanguage().equals(lang)){
                return l.getContent().trim();
            }
        }
        if(values.size() > 0){
            return values.get(0).getContent().trim();
        }

        return null;
    }

    private String getInfoJSONString(List<Lang> values){
        JSONArray array = new JSONArray();
        for(Lang l: values){
            JSONObject obj = new JSONObject();
            try {
                obj.put(l.getLanguage(), l.getContent());
            } catch (JSONException e) {
                Analytics.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }
            array.put(obj);
        }
        return array.toString();
    }

    private void setInfoFromJSONString(String jsonStr, List<Lang> values, boolean isLangs){
        try {
            JSONArray infoArray = new JSONArray(jsonStr);
            for(int i=0; i< infoArray.length(); i++){
                JSONObject infoObj = infoArray.getJSONObject(i);
                @SuppressWarnings("unchecked")
                Iterator<String> iter = infoObj.keys();
                while(iter.hasNext()){
                    String key = iter.next();
                    String info = "";
                    if(!isLangs) {
                        info = infoObj.getString(key);
                    }
                    Lang l = new Lang(key, info);
                    values.add(l);
                }
            }
        } catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "JSON error: ", e);
        } catch (NullPointerException npe){
            Analytics.logException(npe);
            Log.d(TAG, "Null pointer error: ", npe);
        }
    }

    public void setTitlesFromJSONObjectMap(JSONObject jsonObjectMultilang) throws JSONException {
        List<Lang> localLangs = parseLangs(jsonObjectMultilang);
        this.titles = localLangs;
    }

    public void setDescriptionsFromJSONObjectMap(JSONObject jsonObjectMultilang) throws JSONException {
        List<Lang> localLangs = parseLangs(jsonObjectMultilang);
        this.descriptions = localLangs;
    }

    private List<Lang> parseLangs(JSONObject jsonObjectMultilang) throws JSONException {
        Iterator<String> keys = jsonObjectMultilang.keys();
        List<Lang> localLangs = new ArrayList<>();

        while(keys.hasNext()) {
            String key = keys.next();
            String value = jsonObjectMultilang.getString(key);
            if (!TextUtils.isEmpty(value) && !TextUtils.equals(value,"null")){
                localLangs.add(new Lang(key, value));
            }
        }

        return localLangs;
    }
}
