package org.digitalcampus.oppia.model;

import android.content.Context;
import android.text.TextUtils;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.database.DbHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class CustomField {

    public static final String CUSTOMFIELDS_FILE = "custom_fields.json";

    private static final String TYPE_STRING = "str";
    private static final String TYPE_BOOLEAN = "bool";
    private static final String TYPE_INT = "int";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_CHOICES = "choices";

    private static final String STR_ORDER = "order";

    private static final String STR_HELPER_TEXT = "helper_text";
    private String key;
    private String label;
    private boolean required;
    private String type;
    private String helperText;
    private int order;
    private String collectionName;
    private List<CollectionItem> collection;

    private String fieldVisibleBy;
    private String valueVisibleBy;
    private String collectionNameBy;

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

    public String getCollectionNameBy() {
        return collectionNameBy;
    }

    public void setCollectionNameBy(String collectionNameBy) {
        this.collectionNameBy = collectionNameBy;
    }

    public List<CollectionItem> getCollection() {
        return collection;
    }

    public void setCollection(List<CollectionItem> collection) {
        this.collection = collection;
    }

    public boolean isDependantOnField(){
        return !TextUtils.isEmpty(fieldVisibleBy);
    }

    public boolean isNegativeDependency(){
        return !TextUtils.isEmpty(valueVisibleBy) && valueVisibleBy.startsWith("!");
    }

    public String getFieldVisibleBy() {
        return fieldVisibleBy;
    }

    public void setFieldVisibleBy(String fieldVisibleBy) {
        this.fieldVisibleBy = fieldVisibleBy;
    }

    public String getValueVisibleBy() {
        return valueVisibleBy;
    }

    public void setValueVisibleBy(String valueVisibleBy) {
        this.valueVisibleBy = valueVisibleBy;
    }

    public boolean isString(){ return TextUtils.equals(type, TYPE_STRING); }
    public boolean isBoolean(){ return TextUtils.equals(type, TYPE_BOOLEAN); }
    public boolean isInteger(){ return TextUtils.equals(type, TYPE_INT); }
    public boolean isFloat(){ return TextUtils.equals(type, TYPE_FLOAT); }
    public boolean isChoices(){ return TextUtils.equals(type, TYPE_CHOICES); }
    public boolean isExportedAsString(){ return isString() || isChoices(); }

    public static void loadCustomFields(Context ctx, String data){
        if (TextUtils.isEmpty(data)){
            return;
        }
        try {
            JSONObject json = new JSONObject(data);
            JSONArray fields = json.getJSONArray("fields");
            DbHelper db = DbHelper.getInstance(ctx);
            db.clearCustomFieldsAndCollections();

            for (int i = 0; i<fields.length(); i++){
                JSONObject f = fields.getJSONObject(i);
                db.insertOrUpdateCustomField(parseField(f));
            }

            if (!json.has("collections")){
                // If there are no defined collections, we are finished
                return;
            }
            JSONArray collections = json.getJSONArray("collections");
            for (int i = 0; i<collections.length(); i++){
                JSONObject col = collections.getJSONObject(i);
                insertCollection(db, col);
            }

        } catch (JSONException e) {
            Mint.logException(e);
        }
    }

    public static List<RegisterFormStep> parseRegisterSteps(String data){
        ArrayList<RegisterFormStep> steps = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(data);
            JSONArray regSteps = json.getJSONArray("register_steps");
            for (int i = 0; i<regSteps.length(); i++){
                JSONObject regStep = regSteps.getJSONObject(i);
                RegisterFormStep step = new RegisterFormStep();
                step.setTitle(regStep.getString("title"));
                step.setOrder(regStep.getInt(STR_ORDER));
                if (regStep.has(STR_HELPER_TEXT)){
                    step.setHelperText(regStep.getString(STR_HELPER_TEXT));
                }
                if (regStep.has("conditional_byfield")){
                    step.setConditionalByField(regStep.getString("conditional_byfield"));
                    if (regStep.has("conditional_byvalue")){
                        step.setConditionalByValue(regStep.getString("conditional_byvalue"));
                    }
                }
                JSONArray fields = regStep.getJSONArray("fields");
                List<String> f = new ArrayList<>();
                for (int j = 0; j<fields.length(); j++){
                    f.add(fields.getString(j));
                }
                step.setFields(f);
                steps.add(step);
            }
        } catch (JSONException e) {
            Mint.logException(e);
        }
        return steps;
    }

    private static CustomField parseField(JSONObject f) throws JSONException {
        CustomField field = new CustomField();
        field.setKey(f.getString("name"));
        field.setLabel(f.getString("label"));
        field.setRequired(f.getBoolean("required"));
        field.setType(f.getString("type"));
        if (f.has(STR_HELPER_TEXT)){
            field.setHelperText(f.getString(STR_HELPER_TEXT));
        }
        if (f.has(STR_ORDER)){
            field.setOrder(f.getInt(STR_ORDER));
        }
        if (f.has("collection")){
            field.setCollectionName(f.getString("collection"));
        }
        if (f.has("visible_byfield")){
            field.setFieldVisibleBy(f.getString("visible_byfield"));
        }
        if (f.has("visible_byvalue")){
            field.setValueVisibleBy(f.getString("visible_byvalue"));
        }
        if (f.has("collection_byfield")){
            field.setCollectionNameBy(f.getString("collection_byfield"));
        }
        return field;
    }


    private static void insertCollection(DbHelper db, JSONObject collection){
        try {
            String collectionName = collection.getString("collection_name");
            JSONArray items = collection.getJSONArray("items");
            List<CollectionItem> collectionItems = new ArrayList<>();

            for (int j = 0; j<items.length(); j++){
                JSONObject item = items.getJSONObject(j);
                String key = item.getString("id");
                String value = item.getString("value");
                collectionItems.add( new CollectionItem(key, value));
            }
            db.insertOrUpdateCustomFieldCollection(collectionName, collectionItems);

        } catch (JSONException e) {
            Mint.logException(e);
        }

    }

    public static class CollectionItem {
        private String key;
        private String label;

        public CollectionItem(String key, String label){
            this.key = key;
            this.label = label;
        }
        public String getKey() { return key; }
        public String getLabel() { return label; }
        public void setKey(String key) { this.key = key; }
        public void setLabel(String label) { this.label = label; }

        @NonNull
        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CollectionItem){
                CollectionItem item = (CollectionItem) obj;
                return item.getKey().equals(key);
            }
            return false;
        }

        @Override
        public int hashCode(){
            return key.hashCode();
        }
    }

    public static class RegisterFormStep {

        private int order;
        private String title;
        private String helperText;
        private String conditionalByField;
        private String conditionalByValue;
        private List<String> fields;

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getHelperText() {
            return helperText;
        }

        public void setHelperText(String helperText) {
            this.helperText = helperText;
        }

        public String getConditionalByField() {
            return conditionalByField;
        }

        public void setConditionalByField(String conditionalByField) {
            this.conditionalByField = conditionalByField;
        }

        public String getConditionalByValue() {
            return conditionalByValue;
        }

        public void setConditionalByValue(String conditionalByValue) {
            this.conditionalByValue = conditionalByValue;
        }

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }
    }
}
