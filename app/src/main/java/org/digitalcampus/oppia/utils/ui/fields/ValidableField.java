package org.digitalcampus.oppia.utils.ui.fields;

import android.view.ViewGroup;

public interface ValidableField {
    void setRequired(boolean required);
    void initialize();
    boolean validate();
    void setHelperText(CharSequence text);
    String getCleanedValue();
    void addChangeListener(onChangeListener listener);
    void setVisibility(final int VISIBLE_MODE);
    void setLayoutParams(ViewGroup.LayoutParams params);
    void setCustomValidator(CustomValidator v);
    interface CustomValidator{
        boolean validate(ValidableField field);
    }
    interface onChangeListener{
        void onValueChanged(String newValue);
    }
}
