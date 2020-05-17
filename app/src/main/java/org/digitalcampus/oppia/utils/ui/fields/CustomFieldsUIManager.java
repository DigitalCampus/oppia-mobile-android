package org.digitalcampus.oppia.utils.ui.fields;

import android.content.Context;
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
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.SwitchCompat;

public class CustomFieldsUIManager {

    private List<CustomField> fields;
    private List<Pair<CustomField, ValidableField>> inputs = new ArrayList<>();
    private Context ctx;

    public CustomFieldsUIManager(Context ctx, List<CustomField> fields){
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

    private LinearLayout.LayoutParams getDefaultLayoutParams(){
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void populateAndInitializeFields(ViewGroup container){

        for (CustomField field : fields){
            View input;
            if (field.isBoolean()){
                input = addSwitchLayout(field);
            }
            else if (field.isChoices()){
                input = addSpinnerLayout(field);
            }
            else{
                input = addEditTextLayout(field);
            }
            input.setLayoutParams(getDefaultLayoutParams());
            container.addView(input);
        }

        initializeFields();
        container.invalidate();

    }

    private void initializeFields(){
        for (final Pair<CustomField, ValidableField> dependField : inputs){
            final CustomField field = dependField.first;
            final ValidableField input = dependField.second;
            input.initialize();

            if (field.isDependantOnField()){
                input.setVisibility(View.GONE);
                // Find field it depends on
                for (final Pair<CustomField, ValidableField> formField : inputs){
                   if (TextUtils.equals(formField.first.getKey(), field.getFieldVisibleBy())) {
                       formField.second.setChangeListener(new ValidableField.onChangeListener() {
                           @Override
                           public void onValueChanged(String newValue) {
                               boolean visible = newValue != null && !TextUtils.isEmpty(newValue) &&
                                       (TextUtils.isEmpty(field.getValueVisibleBy()) ||
                                    TextUtils.equals(field.getValueVisibleBy(), newValue));
                               input.setVisibility(visible ? View.VISIBLE : View.GONE);
                           }
                       });
                       break;
                   }
                }
            }
        }
    }

    private View addSwitchLayout(CustomField field){
        SwitchCompat switchInput = new SwitchCompat(ctx);
        LinearLayout.LayoutParams wrap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        switchInput.setLayoutParams(wrap);
        switchInput.setHint(field.getLabel());

        ValidableSwitchLayout input = new ValidableSwitchLayout(ctx, switchInput);
        addAndConfigureInput(field, input);
        return input;
    }

    private View addSpinnerLayout(CustomField field){
        Spinner spinner = (Spinner) LayoutInflater.from(ctx).inflate(R.layout.view_underlined_spinner, null);
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
        ValidableTextInputLayout input = new ValidableTextInputLayout(ctx);
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
                ValidableSpinnerLayout input = (ValidableSpinnerLayout) formField.second;
                input.setSelection(value.toString());
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
                values.put(field.getKey(), new CustomValue<>(input.getCleanedValue()));
            }
            else{
                ValidableTextInputLayout input = (ValidableTextInputLayout) formField.second;
                if (field.isInteger()){
                    String value = input.getCleanedValue();
                    if (!TextUtils.isEmpty(value)){
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
}
