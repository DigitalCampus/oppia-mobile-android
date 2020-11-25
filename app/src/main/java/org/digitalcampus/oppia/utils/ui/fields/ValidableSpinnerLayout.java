package org.digitalcampus.oppia.utils.ui.fields;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.CustomField;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class ValidableSpinnerLayout extends LinearLayout implements ValidableField, AdapterView.OnItemSelectedListener {

    private boolean required = false;
    private Spinner input;
    private TextView helperText;
    private TextView errorText;
    private TextView labelText;
    private List<CustomField.CollectionItem> items;
    private List<CustomField.CollectionItem> uiItems;
    private String label;
    private boolean selected = false;
    private ArrayAdapter<CustomField.CollectionItem> adapter;

    private CustomValidator validator;
    private List<onChangeListener> valueChangelisteners = new ArrayList<>();

    public ValidableSpinnerLayout(Context context){
        super(context);
    }

    public ValidableSpinnerLayout(Context context, Spinner input, String label, List<CustomField.CollectionItem> items) {
        super(context);
        this.setOrientation(VERTICAL);
        this.addView(input);
        this.input = input;
        this.items = items;
        this.label = label;

        errorText = new TextView(getContext());
        errorText.setLayoutParams(CustomFieldsUIManager.getLinearParams());
        errorText.setPadding(input.getPaddingLeft(), 0, input.getPaddingRight(), 0);
        errorText.setText(getResources().getString(R.string.field_required));
        errorText.setVisibility(GONE);

        labelText = new TextView(getContext());
        LayoutParams params = CustomFieldsUIManager.getLinearParams();
        params.setMargins(0, 10, 0, -20);
        labelText.setLayoutParams(params);
        labelText.setPadding(input.getPaddingLeft(), 0,0, 0);
        updateLabelText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            errorText.setTextAppearance(R.style.Oppia_CustomField_TextInputLayoutError);
            labelText.setTextAppearance(R.style.Oppia_CustomField_TextInputLayoutLabel);
        }
        else{
            labelText.setTextSize(getResources().getDimension(R.dimen.hint_text_size));
            errorText.setTextSize(getResources().getDimension(R.dimen.hint_text_size));
            labelText.setTextColor(ContextCompat.getColor(getContext(), R.color.theme_secondary));
            errorText.setTextColor(ContextCompat.getColor(getContext(), R.color.text_error));
        }

        addView(errorText);
        addView(labelText, 0);
    }

    private void updateLabelText(){
        String inputLabel = label;
        if (required && input != null){
            inputLabel += " *";
        }
        labelText.setText(inputLabel);
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
        updateLabelText();
    }

    public void setSelection(String key){
        if (key == null || items == null){
            return;
        }
        for (int i=0; i<items.size(); i++){
            if (TextUtils.equals(items.get(i).getKey(), key)){
                int position = i + (selected ? 0 : 1);
                input.setSelection(position);
                onItemSelected(null, null, position, 0);
                return;
            }
        }
    }

    public void updateCollection(List<CustomField.CollectionItem> items){
        this.items = items;
        initialize();
    }

    private void setDisabledTextColor(View view, int position){
        TextView tv = (TextView) view;
        if (tv == null){
            return;
        }
        tv.setTextColor(ContextCompat.getColor(getContext(), !selected && position == 0 ? R.color.grey_dark : R.color.text_dark));
        tv.invalidate();
    }

    @Override
    public void initialize() {

        uiItems = new ArrayList<>();
        uiItems.add(new CustomField.CollectionItem(null, label));
        if (items != null){
            uiItems.addAll(items);
        }
        adapter = new ArrayAdapter<CustomField.CollectionItem>(this.getContext(), R.layout.view_spinner_dropdown_item, uiItems){
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                setDisabledTextColor(view, position);
                return view;
            }
        };
        input.setAdapter(adapter);
        input.setOnItemSelectedListener(this);
        input.setSelection(0);
        selected = false;

        LayoutParams params = (LayoutParams) input.getLayoutParams();
        params.topMargin = -10;
        input.setLayoutParams(params);

        params = (LayoutParams) getLayoutParams();
        params.bottomMargin = getContext().getResources().getDimensionPixelOffset(R.dimen.margin_medium);
        setLayoutParams(params);

    }

    @Override
    public boolean validate() {
        if (input == null || this.getVisibility() == GONE){
            return true;
        }

        boolean valid = !required || selected;
        if (valid && validator != null){
            valid = validator.validate(this);
        }

        errorText.setVisibility(valid ? GONE : VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            labelText.setTextAppearance( valid ?
                    R.style.Oppia_CustomField_TextInputLayoutLabel :
                    R.style.Oppia_CustomField_TextInputLayoutError);
        }

        return valid;
    }

    @Override
    public void setHelperText(CharSequence text) {
        if (helperText == null) {
            helperText = new TextView(getContext());
            helperText.setLayoutParams(CustomFieldsUIManager.getLinearParams());
            helperText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            helperText.setPadding(input.getPaddingLeft(),0,input.getPaddingRight(),0);
            this.addView(helperText);
        }
        helperText.setText(text);
    }

    @Override
    public String getCleanedValue() {
        if (!selected || items == null || items.isEmpty()){
            return null;
        }
        int pos = input.getSelectedItemPosition();
        return items.get(pos).getKey();
    }

    @Override
    public void addChangeListener(onChangeListener listener) {
        this.valueChangelisteners.add(listener);
    }

    private void notifyListeners(){
        for (onChangeListener listener : valueChangelisteners){
            listener.onValueChanged(getCleanedValue());
        }
    }

    @Override
    public void invalidateValue() { notifyListeners(); }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setCustomValidator(CustomValidator v) {
        validator = v;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (view != null){
            setDisabledTextColor(view, position);
        }
        if (!selected){
            selected = position !=0;
            if (selected){
                uiItems.remove(0);
                adapter.notifyDataSetChanged();
                // As we removed the first item, we need to reselect the element in the dropdown
                input.setSelection(position-1);
            }
        }

        if(selected){
            notifyListeners();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        notifyListeners();
    }
}
