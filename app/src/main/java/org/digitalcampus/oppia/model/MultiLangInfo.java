package org.digitalcampus.oppia.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;


public class MultiLangInfo implements Serializable {

    private ArrayList<Lang> langs = new ArrayList<>();
    private ArrayList<Lang> titles = new ArrayList<>();
    private ArrayList<Lang> descriptions = new ArrayList<>();

    public String getTitle(String lang) {
        for(Lang l: titles){
            if(l.getLang().equals(lang)){
                return l.getContent();
            }
        }
        if(titles.size() > 0){
            return titles.get(0).getContent();
        }
        return "No title set";
    }

    public void setTitles(ArrayList<Lang> titles) {
        this.titles = titles;
    }


    public void setTitlesFromJSONString(String jsonStr) {
        try {
            JSONArray titlesArray = new JSONArray(jsonStr);
            for(int i=0; i<titlesArray.length(); i++){
                JSONObject titleObj = titlesArray.getJSONObject(i);
                @SuppressWarnings("unchecked")
                Iterator<String> iter = titleObj.keys();
                while(iter.hasNext()){
                    String key = iter.next();
                    String title = titleObj.getString(key);
                    Lang l = new Lang(key,title);
                    this.titles.add(l);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitleJSONString(){
        JSONArray array = new JSONArray();
        for(Lang l: this.titles){
            JSONObject obj = new JSONObject();
            try {
                obj.put(l.getLang(), l.getContent());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(obj);
        }
        return array.toString();
    }

    public String getDescription(String lang) {
        for(Lang l: descriptions){
            if(l.getLang().equals(lang)){
                return l.getContent();
            }
        }
        if(descriptions.size() > 0){
            return descriptions.get(0).getContent();
        }
        return null;
    }

    public void setDescriptions(ArrayList<Lang> descriptions) {
        this.descriptions = descriptions;
    }

    public void setDescriptionsFromJSONString(String jsonStr) {
        try {
            JSONArray descriptionsArray = new JSONArray(jsonStr);
            for(int i=0; i<descriptionsArray.length(); i++){
                JSONObject descriptionObj = descriptionsArray.getJSONObject(i);
                @SuppressWarnings("unchecked")
                Iterator<String> iter = descriptionObj.keys();
                while(iter.hasNext()){
                    String key = iter.next();
                    String description = descriptionObj.getString(key);
                    Lang l = new Lang(key,description);
                    this.descriptions.add(l);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getDescriptionJSONString(){
        JSONArray array = new JSONArray();
        for(Lang l: this.descriptions){
            JSONObject obj = new JSONObject();
            try {
                obj.put(l.getLang(), l.getContent());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(obj);
        }
        return array.toString();
    }

    public ArrayList<Lang> getLangs() {
        return langs;
    }

    public String getLangsJSONString(){
        JSONArray array = new JSONArray();
        for(Lang l: langs){
            JSONObject obj = new JSONObject();
            try {
                obj.put(l.getLang(), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(obj);
        }
        return array.toString();
    }

    public void setLangs(ArrayList<Lang> langs) {
        this.langs = langs;
    }



    public void setLangsFromJSONString(String jsonStr) {
        try {
            JSONArray langsArray = new JSONArray(jsonStr);
            for(int i=0; i<langsArray.length(); i++){
                JSONObject titleObj = langsArray.getJSONObject(i);
                @SuppressWarnings("unchecked")
                Iterator<String> iter = titleObj.keys();
                while(iter.hasNext()){
                    Lang l = new Lang(iter.next(),"");
                    this.langs.add(l);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException npe){
            npe.printStackTrace();
        }
    }
}
