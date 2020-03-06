package org.digitalcampus.oppia.utils.ui;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

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

    public void createFieldsInContainer(ViewGroup container){
        for (CustomField field : fields){

            ValidableField input = null;
            if (field.isBoolean()){
                input = addSwitchLayout(container, field);
            }
            else{
                input = addEditTextLayout(container, field);
            }
            if (!TextUtils.isEmpty(field.getHelperText())){
                input.setHelperText(field.getHelperText());
            }
            input.initialize();
            inputs.add(new Pair<>(field, input));
        }
    }

    private ValidableField addSwitchLayout(ViewGroup container, CustomField field){
        SwitchCompat input = new SwitchCompat(ctx);
        LinearLayout.LayoutParams wrap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(wrap);
        ValidableSwitchLayout switchLayout = new ValidableSwitchLayout(ctx, input);
        switchLayout.setRequired(field.isRequired());
        input.setHint(field.getLabel());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        switchLayout.setLayoutParams(params);
        container.addView(switchLayout);

        return switchLayout;
    }

    private ValidableField addEditTextLayout(ViewGroup container, CustomField field){
        EditText editText = new EditText(ctx);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        editText.setHint(field.getLabel());
        if (field.isInteger() || field.isFloat()){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        ValidableTextInputLayout inputLayout = new ValidableTextInputLayout(ctx, field.isRequired());
        inputLayout.setLayoutParams(params);
        inputLayout.addView(editText, params);
        container.addView(inputLayout);

        return inputLayout;
    }

    public void fillWithUserData(User user){
        for (Pair<CustomField, ValidableField> formField : inputs){
            CustomValue value = user.getCustomField(formField.first.getKey());
            if (value == null){
                continue;
            }
            if (!formField.first.isBoolean()){
                ValidableTextInputLayout input = (ValidableTextInputLayout) formField.second;
                input.setText(value.toString());
            }
            else{
                ValidableSwitchLayout input = (ValidableSwitchLayout) formField.second;
                input.setChecked((boolean) value.getValue());
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
