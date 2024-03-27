package org.digitalcampus.oppia.utils.ui.fields;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import org.digitalcampus.mobile.learning.R;

public class ValidableNestedTextInputLayout extends ValidableTextInputLayout  {

    public ValidableNestedTextInputLayout(Context context) {
        super(context);
    }

    public ValidableNestedTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidableNestedTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Nullable
    @Override
    public EditText getEditText() {
        return getNestedTextInputLayout() != null ? getNestedTextInputLayout().getEditText() : null;
    }

    @Override
    public void setError(@Nullable CharSequence errorText) {
        TextInputLayout nestedTextInputLayout = getNestedTextInputLayout();
        if (nestedTextInputLayout != null) {
            nestedTextInputLayout.setError(errorText);
        }
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        TextInputLayout nestedTextInputLayout = getNestedTextInputLayout();
        if (nestedTextInputLayout != null) {
            nestedTextInputLayout.setErrorEnabled(enabled);
        }
    }

    private TextInputLayout getNestedTextInputLayout() {
        return findViewById(R.id.ccp_text_input_layout);
    }
}
