package androidTestFiles.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;

import java.util.ArrayList;

public class MockUtils {


    public static void givenThereAreSomeCourses(CoursesRepository coursesRepository, int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }
}
