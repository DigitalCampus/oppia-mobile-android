package org.digitalcampus.oppia.utils.course_status;

public class CourseStatusBadgeData {

    private int text;
    private int icon;
    private int color;

    public CourseStatusBadgeData(int text, int icon, int color) {
        this.text = text;
        this.icon = icon;
        this.color = color;
    }

    public int getText() {
        return text;
    }

    public void setText(int text) {
        this.text = text;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
