package org.digitalcampus.oppia.model;

import android.content.Context;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.utils.storage.StorageUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomField {
    private String key;
    private String label;
    private boolean required;
    private String type;
    private String helperText;
    private int order;

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

    public static void loadCustomFieldsFromAssets(Context ctx){
        String data = StorageUtils.readFileFromAssets(ctx, "custom_fields.json");
        try {
            JSONObject json = new JSONObject(data);
            JSONArray fields = json.getJSONArray("fields");
            DbHelper dbHelper = DbHelper.getInstance(ctx);
            for (int i = 0; i<fields.length(); i++){
                JSONObject f = fields.getJSONObject(i);
                CustomField field = new CustomField();
                field.setKey(f.getString("name"));
                field.setLabel(f.getString("label"));
                field.setRequired(f.getBoolean("required"));
                field.setType(f.getString("type"));
                field.setHelperText(f.getString("helper_text"));
                field.setOrder(f.getInt("order"));

                dbHelper.insertOrUpdateCustomField(field);
            }

        } catch (JSONException e) {
            Mint.logException(e);
        }
    }
}
