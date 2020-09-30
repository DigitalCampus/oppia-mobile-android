package org.digitalcampus.oppia.utils.ui.fields;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import org.digitalcampus.mobile.learning.R;

import androidx.core.text.HtmlCompat;

public class ValidableTextInputLayout extends TextInputLayout implements ValidableField, View.OnFocusChangeListener {

    private static final String REQUIRED_SPANNED_HINT = "<string>%s <span style=\"color:red;\">*</span></string>";

    private boolean required = false;
    private boolean cantContainSpaces = false;
    private CustomValidator validator;

    public ValidableTextInputLayout(Context context) {
        super(context);
    }

    public ValidableTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateAttrs(context, attrs);
    }

    public ValidableTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateAttrs(context, attrs);
    }

    private void updateAttrs(Context context, AttributeSet attrs){
        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ValidableTextInputLayout,
                0, 0);
        try {
            required = styledAttrs.getBoolean(R.styleable.ValidableTextInputLayout_required, false);
            cantContainSpaces = styledAttrs.getBoolean(R.styleable.ValidableTextInputLayout_cantContainSpaces, false);
        } finally {
            styledAttrs.recycle();
        }
        initialize();
    }

    public void setRequired(boolean required){
        this.required = required;
    }

    public void initialize(){
        if (required && this.getEditText() != null){
            String html = String.format(REQUIRED_SPANNED_HINT, this.getHint());
            Spanned requiredHint = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
            this.setHint(requiredHint);
        }

        if (!TextUtils.isEmpty(getHelperText())){
            // We add some additional bottom margin
            LayoutParams params = (LayoutParams) getLayoutParams();
            if (params != null){
                params.bottomMargin = getContext().getResources().getDimensionPixelOffset(R.dimen.margin_medium);
                setLayoutParams(params);
            }
        }
        initializeLabelColorHintSelector();

    }

    public boolean validate(){
        EditText input = getEditText();
        if (input == null || this.getVisibility() == GONE){
            return true;
        }
        String text = input.getText().toString().trim();
        boolean valid = true;
        if (required && (text.length() == 0)){
            this.setErrorEnabled(true);
            this.setError(getContext().getString(R.string.field_required));
            valid = false;
        }
        else if (cantContainSpaces && (text.contains(" ") )){
            this.setErrorEnabled(true);
            this.setError(getContext().getString(R.string.field_spaces_error));
            valid = false;
        }
        if (valid && validator != null){
            valid = validator.validate(this);
        }

        if (valid){
            this.setError(null);
            this.setErrorEnabled(false);
        }
        return valid;
    }

    public String getCleanedValue(){
        EditText input = getEditText();
        if (input == null){
            return null;
        }
        return input.getText().toString().trim();
    }

    private void initializeLabelColorHintSelector() {

        addOnEditTextAttachedListener(textInputLayout -> {
            getEditText().setFocusable(true);
            getEditText().setFocusableInTouchMode(true);
            getEditText().setOnFocusChangeListener(ValidableTextInputLayout.this);
            setEditTextSelected();
        });
    }


    @Override
    public void addChangeListener(onChangeListener listener) {
        // do nothing
    }

    @Override
    public void invalidateValue() {
        // do nothing
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setCustomValidator(CustomValidator v) {
        validator = v;
    }

    // Small hack to be able to show different label colors when the field is empty or filled
    // using the color selector based in the "selected" state.
    private void setEditTextSelected(){
        EditText input = getEditText();
        if (input != null){
            boolean selected = !TextUtils.isEmpty(input.getText().toString());
            this.setSelected(selected);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        setEditTextSelected();
    }

    public void setText(String text) {
        EditText input = getEditText();
        if (input != null){
            input.setText(text);
            setEditTextSelected();
        }
    }



}
