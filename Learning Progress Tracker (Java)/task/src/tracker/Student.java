package tracker;

import java.util.*;

import static tracker.Course.*;
import static tracker.CoursesDataStore.*;

public class Student {
    private final String ID = UUID.randomUUID().toString().substring(0, 5);
    private String firstName;
    private String lastName;
    private String email;

    private final Map<Course, Integer> enrolledCourses;
    public Student(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        enrolledCourses = new LinkedHashMap<>();
        enrolledCourses.put(JAVA.getInstance(), 0);
        enrolledCourses.put(DSA.getInstance(), 0);
        enrolledCourses.put(DATABASE.getInstance(), 0);
        enrolledCourses.put(SPRING.getInstance(), 0);
    }

    public boolean addCourse(Course course) {
        if(!enrolledCourses.containsKey(course)) {
            enrolledCourses.put(course, 0);
            return true;
        }
        return false;
    }

    public void updatePoints(Course course, int point) {
        if(enrolledCourses.containsKey(course)) {
            if(point > 0) {
                enrolledCourses.put(course, enrolledCourses.getOrDefault(course, 0) + point);
                course.updateTotalNumberOfCompletedTasks(1, point);
                course.enrollStudent(this);
            }
        }
    }

    public void printPoints() {
        List<String> output = new ArrayList<>();
        enrolledCourses.forEach((course, integer) -> {
            if(integer >= 0) {
                output.add(course.getName() + "=" + integer);
            }
        });
        String joinedString = String.join("; ", output);
        System.out.printf("%s points: %s%n", ID, joinedString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(lastName, student.lastName) && Objects.equals(firstName, student.firstName) && Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastName, firstName, email);
    }

    @Override
    public String toString() {
        return this.ID;
    }

    public String getID() {
        return ID;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPointsForCourse(Course course) {
        return enrolledCourses.getOrDefault(course, 0);
    }

    public double getCompletionPercentage(Course course) {
        // Percentage of completion = (Points of completed tasks / Total points for course) * 100
        double percentageOfCompletion = (double) enrolledCourses.get(course) / course.getTotalNumberOfPoints() * 100;
        return (double) Math.round(percentageOfCompletion * 10) / 10;
    }

    public void removeCourseFromEnrolledList(Course course) {
        enrolledCourses.remove(course);
    }
}
