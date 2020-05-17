package org.digitalcampus.oppia.utils.ui.fields;

public interface ValidableField {
    void setRequired(boolean required);
    void initialize();
    boolean validate();
    void setHelperText(CharSequence text);
    String getCleanedValue();
    void setChangeListener(onChangeListener listener);
    void setVisibility(final int VISIBLE_MODE);

    interface onChangeListener{
        void onValueChanged(String newValue);
    }
}
