package org.digitalcampus.oppia.utils.ui.fields;

import android.content.Context;
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

import androidx.core.content.ContextCompat;

public class ValidableSpinnerLayout extends LinearLayout implements ValidableField, AdapterView.OnItemSelectedListener {

    private boolean required = false;
    private Spinner input;
    private TextView helperText;
    private TextView errorText;
    private List<CustomField.CollectionItem> items;
    private List<CustomField.CollectionItem> uiItems;
    private String label;
    private boolean selected = false;
    private ArrayAdapter<CustomField.CollectionItem> adapter;

    private onChangeListener valueChangelistener;

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
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        errorText.setLayoutParams(params);
        errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        errorText.setTextColor(ContextCompat.getColor(getContext(), R.color.text_error));
        errorText.setText(getResources().getString(R.string.field_required));
        errorText.setVisibility(GONE);
        addView(errorText);
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setSelection(String key){
        if (key == null){
            return;
        }
        for (int i=0; i<items.size(); i++){
            if (TextUtils.equals(items.get(i).getKey(), key)){
                input.setSelection(i + (selected ? 0 : 1));
                return;
            }
        }

    }

    private void setDisabledTextColor(View view, int position){
        TextView tv = (TextView) view;
        tv.setTextColor(getContext().getColor( !selected && position == 0 ? R.color.grey_dark : R.color.text_dark));
        tv.invalidate();
    }

    @Override
    public void initialize() {
        String inputLabel = label;
        if (required && input != null){
            inputLabel += " *";
        }
        uiItems = new ArrayList<>();
        uiItems.add(new CustomField.CollectionItem(null, inputLabel));
        uiItems.addAll(items);
        adapter = new ArrayAdapter<CustomField.CollectionItem>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, uiItems){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                setDisabledTextColor(view, position);
                return view;
            }
        };
        input.setAdapter(adapter);
        input.setOnItemSelectedListener(this);
        input.setSelection(0);

    }

    @Override
    public boolean validate() {
        boolean valid = !required || selected;
        errorText.setVisibility(valid ? GONE : VISIBLE);
        return valid;
    }

    @Override
    public void setHelperText(CharSequence text) {
        if (helperText == null) {
            helperText = new TextView(getContext());
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            helperText.setLayoutParams(params);
            helperText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            this.addView(helperText);
        }
        helperText.setText(text);

    }

    @Override
    public String getCleanedValue() {
        int pos = input.getSelectedItemPosition();
        return items.get(pos).getKey();
    }

    @Override
    public void setChangeListener(onChangeListener listener) {
        this.valueChangelistener = listener;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        setDisabledTextColor(view, position);
        if (!selected){
            selected = position !=0;
            if (selected){
                uiItems.remove(0);
                adapter.notifyDataSetChanged();
                //As we removed the first item, we need to reselect the element in the dropdown
                input.setSelection(position-1);
            }
        }

        if(selected && valueChangelistener != null){
            valueChangelistener.onValueChanged(getCleanedValue());
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
