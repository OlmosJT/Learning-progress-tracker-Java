package tracker;

import static tracker.Course.*;

public enum CoursesDataStore {
    JAVA(new Course("Java", TOTAL_POINTS_JAVA)),
    DSA(new Course("DSA", TOTAL_POINTS_DSA)),
    DATABASE(new Course("Databases", TOTAL_POINTS_DATABASE)),
    SPRING(new Course("Spring", TOTAL_POINTS_SPRING));

    private final Course instance;

    CoursesDataStore(Course instance) {
        this.instance = instance;
    }

    public Course getInstance() {
        return instance;
    }
}
