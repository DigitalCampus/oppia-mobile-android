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

public class CustomFieldsUIManager {

    private List<CustomField> fields;
    private List<Pair<CustomField, ValidableTextInputLayout>> inputs = new ArrayList<>();
    private Context ctx;

    public CustomFieldsUIManager(Context ctx, List<CustomField> fields){
        this.fields = fields;
        this.ctx = ctx;
    }

    public void createFieldsInContainer(ViewGroup container){
        for (CustomField field : fields){
            EditText editText = new EditText(ctx);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            editText.setHint(field.getLabel());
            if (field.isInteger() || field.isFloat()){
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            ValidableTextInputLayout inputLayout = new ValidableTextInputLayout(ctx, field.isRequired());
            if (!TextUtils.isEmpty(field.getHelperText())){
                inputLayout.setHelperText(field.getHelperText());
            }
            inputLayout.setLayoutParams(params);
            inputLayout.addView(editText, params);
            container.addView(inputLayout);
            inputLayout.initialize();
            inputs.add(new Pair<>(field, inputLayout));
        }
    }

    public void fillWithUserData(User user){
        for (Pair<CustomField, ValidableTextInputLayout> formField : inputs){
            CustomValue value = user.getCustomField(formField.first.getKey());
            if ((value != null) && !formField.first.isBoolean()){
                formField.second.setText(value.toString());
            }

        }
    }

    public boolean validateFields(){
        boolean valid = true;
        for (Pair<CustomField, ValidableTextInputLayout> formField : inputs){
            valid = formField.second.validate() && valid;
        }
        return valid;
    }

    public  Map<String, CustomValue>  getCustomFieldValues(){
        Map<String, CustomValue> values = new HashMap<>();
        for (Pair<CustomField, ValidableTextInputLayout> formField : inputs){
            CustomField field = formField.first;
            if (field.isInteger()){
                String value = formField.second.getCleanedValue();
                if (!TextUtils.isEmpty(value)){
                    values.put(field.getKey(), new CustomValue<>(Integer.parseInt(value)));
                }
            }
            else{
                values.put(field.getKey(), new CustomValue<>(formField.second.getCleanedValue()));
            }
        }
        return values;
    }
}
