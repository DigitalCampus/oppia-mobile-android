package org.digitalcampus.oppia.model.responses;

import java.util.List;

public class CoursesServerResponse {
    private List<CourseServer> courses;

    public List<CourseServer> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseServer> courses) {
        this.courses = courses;
    }
}
