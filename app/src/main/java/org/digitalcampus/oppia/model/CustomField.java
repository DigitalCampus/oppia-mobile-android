package org.digitalcampus.oppia.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.utils.storage.StorageUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CustomField {

    private static final String TYPE_STRING = "str";
    private static final String TYPE_BOOLEAN = "bool";
    private static final String TYPE_INT = "int";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_CHOICES = "choices";

    private String key;
    private String label;
    private boolean required;
    private String type;
    private String helperText;
    private int order;
    private String collectionName;
    private List<Pair<String, String>> collection;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHelperText() {
        return helperText;
    }

    public void setHelperText(String helperText) {
        this.helperText = helperText;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collection) {
        this.collectionName = collection;
    }

    public boolean isString(){ return TextUtils.equals(type, TYPE_STRING); }
    public boolean isBoolean(){ return TextUtils.equals(type, TYPE_BOOLEAN); }
    public boolean isInteger(){ return TextUtils.equals(type, TYPE_INT); }
    public boolean isFloat(){ return TextUtils.equals(type, TYPE_FLOAT); }
    public boolean isChoices(){ return TextUtils.equals(type, TYPE_CHOICES); }

    public static void loadCustomFieldsFromAssets(Context ctx){
        String data = StorageUtils.readFileFromAssets(ctx, "custom_fields.json");
        try {
            JSONObject json = new JSONObject(data);
            JSONArray fields = json.getJSONArray("fields");
            DbHelper db = DbHelper.getInstance(ctx);
            db.clearCustomFields();

            for (int i = 0; i<fields.length(); i++){
                JSONObject f = fields.getJSONObject(i);
                CustomField field = new CustomField();
                field.setKey(f.getString("name"));
                field.setLabel(f.getString("label"));
                field.setRequired(f.getBoolean("required"));
                field.setType(f.getString("type"));
                if (f.has("helper_text")){
                    field.setHelperText(f.getString("helper_text"));
                }
                if (f.has("order")){
                    field.setOrder(f.getInt("order"));
                }
                if (f.has("collection")){
                    field.setCollectionName(f.getString("collection"));
                }

                db.insertOrUpdateCustomField(field);
            }

            if (!json.has("collections")){
                // If there are no defined collections, we are finished
                return;
            }
            JSONArray collections = json.getJSONArray("collections");
            for (int i = 0; i<collections.length(); i++){
                JSONObject col = collections.getJSONObject(i);
                String collectionName = col.getString("collection_name");
                JSONArray items = col.getJSONArray("items");
                List<Pair<String, String>> collectionItems = new ArrayList<>();

                for (int j = 0; j<items.length(); j++){
                    JSONObject item = items.getJSONObject(j);
                    String key = item.getString("id");
                    String value = item.getString("value");
                    collectionItems.add( new Pair<>(key, value));
                }
                db.insertOrUpdateCustomFieldCollection(collectionName, collectionItems);
            }

        } catch (JSONException e) {
            Mint.logException(e);
        }
    }
}
