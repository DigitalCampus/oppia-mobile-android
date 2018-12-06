package org.digitalcampus.oppia.utils.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.digitalcampus.mobile.learning.R;

public class ValidableTextInputLayout extends TextInputLayout {

    private static final String REQUIRED_SPANNED_HINT = "<string>%s <span style=\"color:red;\">*</span></string>";

    private boolean required = false;
    private boolean cantContainSpaces = false;

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

    public void initialize(){
        if (required && this.getEditText() != null){
            String html = String.format(REQUIRED_SPANNED_HINT, this.getHint());
            Spanned requiredHint = Html.fromHtml(html);
            this.setHint(requiredHint);
        }
    }

    public boolean validate(){
        String text = getEditText().getText().toString().trim();
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
        if (valid){
            this.setError(null);
            this.setErrorEnabled(false);
        }
        return valid;
    }

    public String getCleanedValue(){
        return getEditText().getText().toString().trim();
    }



}
