package org.digitalcampus.oppia.utils.ui;

public interface ValidableField {
    void setRequired(boolean required);
    void initialize();
    boolean validate();
    void setHelperText(CharSequence text);
    String getCleanedValue();
}
