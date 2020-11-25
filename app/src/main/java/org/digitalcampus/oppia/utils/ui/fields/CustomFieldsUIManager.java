package org.digitalcampus.oppia.utils.ui.fields;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.SwitchCompat;

public class CustomFieldsUIManager {

    private HashMap<String, ValidableField> baseFields;
    private List<CustomField> fields;
    private List<Pair<CustomField, ValidableField>> inputs = new ArrayList<>();
    private Context ctx;

    public CustomFieldsUIManager(Context ctx, HashMap<String, ValidableField> baseFields, List<CustomField> fields){
        this.baseFields = baseFields;
        this.fields = fields;
        this.ctx = ctx;
    }

    private void addAndConfigureInput(CustomField field, ValidableField input){
        input.setRequired(field.isRequired());
        if (!TextUtils.isEmpty(field.getHelperText())){
            input.setHelperText(field.getHelperText());
        }
        inputs.add(new Pair<>(field, input));
    }

    public ValidableField getInputByKey(String key){
        if (baseFields.containsKey(key)){
            return baseFields.get(key);
        }

        for (final Pair<CustomField, ValidableField> formField : inputs){
            if (TextUtils.equals(formField.first.getKey(), key)) {
                return formField.second;
            }
        }
        return null;
    }

    public void populateAndInitializeFields(ViewGroup container){

        for (CustomField field : fields){
            View inputView;
            if (field.isBoolean()){
                inputView = addSwitchLayout(field);
            }
            else if (field.isChoices()){
                inputView = addSpinnerLayout(field);
            }
            else{
                inputView = addEditTextLayout(field);
            }
            container.addView(inputView);
        }

        initializeFields();
        container.invalidate();
    }

    private void initializeFields(){
        for (final Pair<CustomField, ValidableField> dependField : inputs){
            final CustomField field = dependField.first;
            final ValidableField input = dependField.second;

            LinearLayout.LayoutParams params = getDefaultLayoutParams();
            if (field.isDependantOnField() && !field.isNegativeDependency()) {
                // We remove the bottom separation from the field it depends on
                // (unless is a negative dependecy)
                params.topMargin = -ctx.getResources().getDimensionPixelSize(R.dimen.margin_medium)/2;
            }
            input.setLayoutParams(params);
            input.initialize();

            if (field.isDependantOnField()){
                input.setVisibility(View.GONE);
                configureDepencency(input, field);
            }
        }
    }

    private void setDependencyVisibility(CustomField field, ValidableField input, String value){
        boolean visible = assertValue(field.getValueVisibleBy(), value);
        input.setVisibility(visible ? View.VISIBLE : View.GONE);
        input.invalidateValue();
    }

    private void configureDepencency(final ValidableField input, final CustomField field){

        ValidableField formField = getInputByKey(field.getFieldVisibleBy());
        if (formField != null) {
            formField.addChangeListener(newValue -> {

                if (formField.getView().getVisibility() != View.VISIBLE){
                    // If the dependant field is not currently visible, hide it as well
                    input.setVisibility(View.GONE);
                    input.invalidateValue();
                    return;
                }

                setDependencyVisibility(field, input, newValue);
                ValidableField collectionField = getInputByKey(field.getCollectionNameBy());
                if (collectionField != null && field.isChoices()) {
                    String collectionName = collectionField.getCleanedValue();
                    List<CustomField.CollectionItem> collection = DbHelper.getInstance(ctx).getCollection(collectionName);
                    ((ValidableSpinnerLayout) input).updateCollection(collection);
                }
            });
        }
    }

    private View addSwitchLayout(CustomField field){
        SwitchCompat switchInput = new SwitchCompat(ctx);
        switchInput.setLayoutParams(getLinearParams());
        switchInput.setHint(field.getLabel());
        ValidableSwitchLayout input = new ValidableSwitchLayout(ctx, switchInput);
        addAndConfigureInput(field, input);
        return input;
    }

    private View addSpinnerLayout(CustomField field){
        Spinner spinner = (Spinner) LayoutInflater.from(ctx).inflate(R.layout.view_customfield_spinner, null);
        ValidableSpinnerLayout input = new ValidableSpinnerLayout(ctx, spinner, field.getLabel(), field.getCollection());
        addAndConfigureInput(field, input);
        return input;
    }

    private View addEditTextLayout(CustomField field){
        EditText editText = new EditText(ctx);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        editText.setHint(field.getLabel());
        if (field.isInteger() || field.isFloat()){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        ValidableTextInputLayout input = (ValidableTextInputLayout) LayoutInflater.from(ctx).inflate(R.layout.view_customfield_text, null);
        input.addView(editText, getDefaultLayoutParams());
        addAndConfigureInput(field, input);
        return input;
    }


    public void fillWithUserData(User user){
        for (Pair<CustomField, ValidableField> formField : inputs){
            CustomValue value = user.getCustomField(formField.first.getKey());
            if (value == null){
                continue;
            }
            if (formField.first.isBoolean()){
                ValidableSwitchLayout input = (ValidableSwitchLayout) formField.second;
                input.setChecked((boolean) value.getValue());
            }
            else if(formField.first.isChoices()){
                final ValidableSpinnerLayout input = (ValidableSpinnerLayout) formField.second;
                ValidableField dependOnField = getInputByKey(formField.first.getCollectionNameBy());
                if (dependOnField == null){
                    input.setSelection(value.toString());
                }
                else{
                    new Handler(Looper.getMainLooper()).postDelayed(() -> input.setSelection(value.toString()), 150);
                }
            }
            else{
                ValidableTextInputLayout input = (ValidableTextInputLayout) formField.second;
                input.setText(value.toString());
            }

        }
    }

    public boolean validateFields(){
        boolean valid = true;
        for (Pair<CustomField, ValidableField> formField : inputs){
            valid = formField.second.validate() && valid;
        }
        return valid;
    }

    public  Map<String, CustomValue>  getCustomFieldValues(){
        Map<String, CustomValue> values = new HashMap<>();
        for (Pair<CustomField, ValidableField> formField : inputs){
            CustomField field = formField.first;
            if (field.isBoolean()){
                ValidableSwitchLayout input = (ValidableSwitchLayout) formField.second;
                values.put(field.getKey(), new CustomValue<>(input.isChecked()));
            }
            else if (field.isChoices()){
                ValidableSpinnerLayout input = (ValidableSpinnerLayout) formField.second;
                String value = input.getCleanedValue();
                if (value != null && !TextUtils.isEmpty(value)){
                    values.put(field.getKey(), new CustomValue<>(input.getCleanedValue()));
                }

            }
            else{
                ValidableTextInputLayout input = (ValidableTextInputLayout) formField.second;
                if (field.isInteger()){
                    String value = input.getCleanedValue();
                    if (value != null && !TextUtils.isEmpty(value)){
                        values.put(field.getKey(), new CustomValue<>(Integer.parseInt(value)));
                    }
                }
                else{
                    values.put(field.getKey(), new CustomValue<>(input.getCleanedValue()));
                }
            }

        }
        return values;
    }

    static LinearLayout.LayoutParams getLinearParams(){
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams getDefaultLayoutParams(){
        LinearLayout.LayoutParams params = getLinearParams();
        params.topMargin = ctx.getResources().getDimensionPixelSize(R.dimen.margin_medium);
        return params;
    }

    private boolean assertValue(String assertValue, String value){
        boolean negation = assertValue != null && assertValue.startsWith("!");
        if (negation){ // We have to remove the starting "!"
            assertValue = assertValue.substring(1);
        }
        boolean match = (TextUtils.isEmpty(assertValue) && !TextUtils.isEmpty(value)) ||
                TextUtils.equals(assertValue, value);
        return negation != match;
    }

    boolean isConditionMet(String assertField, String assertValue){
        ValidableField field = getInputByKey(assertField);
        return assertValue(assertValue, field.getCleanedValue());

    }

    public void moveAllFieldsTo(ViewGroup container) {
        for (ValidableField field : baseFields.values()){
            field.setVisibility(View.GONE);
            moveToView(field, container);
        }
        for (final Pair<CustomField, ValidableField> formField : inputs){
            formField.second.setVisibility(View.GONE);
            moveToView(formField.second, container);
        }
    }

    private void moveToView(ValidableField field, ViewGroup container){
        View fieldInput = field.getView();
        ((ViewGroup)fieldInput.getParent()).removeView(fieldInput);
        container.addView(fieldInput);
    }

    public void setVisibleInView(String fieldName, ViewGroup container){

        if (baseFields.containsKey(fieldName)){
            ValidableField field = baseFields.get(fieldName);
            field.setVisibility(View.VISIBLE);
            moveToView(field, container);
            return;
        }

        for (final Pair<CustomField, ValidableField> formField : inputs){
            CustomField field = formField.first;
            ValidableField input = formField.second;
            if (TextUtils.equals(field.getKey(), fieldName)) {
                input.setVisibility(View.VISIBLE);
                moveToView(input, container);
                ValidableField dependant = getInputByKey(field.getFieldVisibleBy());
                if (dependant != null) {
                    setDependencyVisibility(field, input, dependant.getCleanedValue());
                }
                // If it's found, we don't need to keep traversing the list
                break;
            }
        }
    }
}
