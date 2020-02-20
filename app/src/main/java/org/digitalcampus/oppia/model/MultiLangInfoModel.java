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
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;


public class MultiLangInfoModel implements Serializable {

    public static final String TAG = MultiLangInfoModel.class.getSimpleName();

    private ArrayList<Lang> langs = new ArrayList<>();
    private ArrayList<Lang> titles = new ArrayList<>();
    private ArrayList<Lang> descriptions = new ArrayList<>();

    public static final String DEFAULT_NOTITLE = "No title set";
    public static final String DEFAULT_NODESCRIPTION = "No description set";

    public String getTitle(String lang) {
        String title = getInfo(lang, titles);
        return title == null ? DEFAULT_NOTITLE : title.trim();
    }

    public String getTitle(SharedPreferences prefs){
        return this.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
    }

    public void setTitles(ArrayList<Lang> titles) {
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

    public void setDescriptions(ArrayList<Lang> descriptions) {
        this.descriptions = descriptions;
    }

    public void setDescriptionsFromJSONString(String jsonStr) {
        setInfoFromJSONString(jsonStr, this.descriptions, false);
    }

    public String getDescriptionJSONString(){
        return getInfoJSONString(this.descriptions);
    }

    public ArrayList<Lang> getLangs() {
        return langs;
    }
    public void setLangs(ArrayList<Lang> langs) {
        this.langs = langs;
    }

    public String getLangsJSONString(){
        return getInfoJSONString(this.langs);
    }

    public void setLangsFromJSONString(String jsonStr) {
        setInfoFromJSONString(jsonStr, this.langs, true);
    }

    private String getInfo(String lang, ArrayList<Lang> values){
        for(Lang l: values){
            if(l.getLang().equals(lang)){
                return l.getContent().trim();
            }
        }
        if(values.size() > 0){
            return values.get(0).getContent().trim();
        }

        return null;
    }

    private String getInfoJSONString(ArrayList<Lang> values){
        JSONArray array = new JSONArray();
        for(Lang l: values){
            JSONObject obj = new JSONObject();
            try {
                obj.put(l.getLang(), l.getContent());
            } catch (JSONException e) {
                Mint.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }
            array.put(obj);
        }
        return array.toString();
    }

    private void setInfoFromJSONString(String jsonStr, ArrayList<Lang> values, boolean isLangs){
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
            Mint.logException(e);
            Log.d(TAG, "JSON error: ", e);
        } catch (NullPointerException npe){
            Mint.logException(npe);
            Log.d(TAG, "Null pointer error: ", npe);
        }
    }
}
