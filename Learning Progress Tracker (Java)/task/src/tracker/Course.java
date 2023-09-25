package tracker;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Course {
    public static final int TOTAL_POINTS_JAVA = 600;
    public static final int TOTAL_POINTS_DSA = 400;
    public static final int TOTAL_POINTS_DATABASE = 480;
    public static final int TOTAL_POINTS_SPRING = 550;

    private final String name;
    private String description;
    private final int totalNumberOfPoints;
    private int totalNumberOfCompletedTasks;
    private int totalNumberOfCompletedTasksPoint;

    private final Set<String> enrolledStudentsID;

    public Course(String name, int totalNumberOfPoints) {
        this.name = name;
        this.totalNumberOfPoints = totalNumberOfPoints;
        enrolledStudentsID = new HashSet<>();
        description = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return totalNumberOfPoints == course.totalNumberOfPoints && Objects.equals(name, course.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, totalNumberOfPoints);
    }

    public int getTotalEnrolledStudents() {
        return enrolledStudentsID.size();
    }

    public boolean hasAlreadyEnrolled(String ID) {
        return enrolledStudentsID.contains(ID);
    }

    public void enrollStudent(Student student) {
        enrolledStudentsID.add(student.getID());
    }

    public int getTotalNumberOfCompletedTasks() {
        return totalNumberOfCompletedTasks;
    }

    public void updateTotalNumberOfCompletedTasks(int tasksCount, int point) {
        this.totalNumberOfCompletedTasks += tasksCount;
        this.totalNumberOfCompletedTasksPoint += point;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalNumberOfPoints() {
        return totalNumberOfPoints;
    }

    public double getAveragePerAssignment() {
        if(totalNumberOfCompletedTasks < 1) return 0;
        return (double) totalNumberOfCompletedTasksPoint / totalNumberOfCompletedTasks;
    }

    public void removeStudent(Student student) {
        enrolledStudentsID.remove(student.getID());
    }
}
