package tracker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tracker.CoursesDataStore.*;

public class Tracker {
    private static final String nameRegex = "^(?<!['-])[a-zA-Z][a-zA-Z' -]+(?<!['-])$";
    private static final String adjacentRegex = "^(?!.*['-]{2})[A-Za-z' -]+$";
    private static final String emailRegex = "^[\\w.]+@\\w+\\.\\w+$";

    private final Map<String, Runnable> commandMap;
    private final List<Student> students;
    private final Scanner scanner;
    private boolean isGoing;


    public Tracker() {
        isGoing = true;
        scanner = new Scanner(System.in);
        students = new ArrayList<>();
        commandMap = new HashMap<>();
        commandMap.put("exit", this::exit);
        commandMap.put("start", this::start);
        commandMap.put("back", () -> back(-1));
        commandMap.put("add students", this::addStudents);
        commandMap.put("list", this::printStudents);
        commandMap.put("add points", this::addPointsToStudent);
        commandMap.put("find", this::findStudentAndPrintPoints);
        commandMap.put("statistics", this::showStatistics);
        commandMap.put("notify", this::notifyStudentsAndSendAcknowledge);
    }

    private void notifyStudentsAndSendAcknowledge() {
        Set<Student> notifiedStudents = new HashSet<>();
        List<Course> courseList = Arrays.stream(values()).map(CoursesDataStore::getInstance).toList();
        students.forEach(student -> {
            courseList.forEach(course -> {
                if(student.getPointsForCourse(course) == course.getTotalNumberOfPoints()) {
                    notifyStudent(student, course);
                    notifiedStudents.add(student);
                    course.removeStudent(student);
                    student.removeCourseFromEnrolledList(course);
                }
            });
        });
        System.out.printf("Total %d students have been notified.%n", notifiedStudents.size());
    }

    private void notifyStudent(Student student, Course course) {
        System.out.printf("To: %s%n", student.getEmail());
        System.out.println("Re: Your Learning Progress");
        System.out.printf("Hello, %s %s! You have accomplished our %s course!%n", student.getFirstName(), student.getLastName(), course.getName());
    }

    private void showStatistics() {
        System.out.println("Type the name of a course to see details or 'back' to quit");

        List<Course> courseList = Arrays.stream(values()).map(CoursesDataStore::getInstance).toList();

        String mostPopular = courseList.stream()
                .filter(course -> course.getTotalEnrolledStudents() > 0)
                .filter(course ->
                        course.getTotalEnrolledStudents() == courseList.stream()
                                .max(Comparator.comparingInt(Course::getTotalEnrolledStudents)).get().getTotalEnrolledStudents())
                .map(Course::getName).collect(Collectors.joining(", "));

        String leastPopular = courseList.stream()
                .filter(course -> course.getTotalEnrolledStudents() > 0)
                .filter(course ->
                        course.getTotalEnrolledStudents() == courseList.stream()
                                .min(Comparator.comparingInt(Course::getTotalEnrolledStudents)).get().getTotalEnrolledStudents())
                .map(Course::getName).collect(Collectors.joining(", "));

        String highestActivity = courseList.stream()
                .filter(course -> course.getTotalNumberOfCompletedTasks() > 0)
                .filter(course -> course.getTotalNumberOfCompletedTasks() == courseList.stream()
                        .max(Comparator.comparingInt(Course::getTotalNumberOfCompletedTasks)).get().getTotalNumberOfCompletedTasks())
                .map(Course::getName).collect(Collectors.joining(", "));

        String lowestActivity = courseList.stream()
                .filter(course -> course.getTotalNumberOfCompletedTasks() > 0)
                .filter(course -> course.getTotalNumberOfCompletedTasks() == courseList.stream()
                        .min(Comparator.comparingInt(Course::getTotalNumberOfCompletedTasks)).get().getTotalNumberOfCompletedTasks())
                .map(Course::getName).collect(Collectors.joining(", "));

        String easiestCourse = courseList.stream()
                .filter(course -> course.getAveragePerAssignment() > 0)
                .filter(course -> course.getAveragePerAssignment() == courseList.stream()
                        .max(Comparator.comparingDouble(Course::getAveragePerAssignment)).get().getAveragePerAssignment())
                .map(Course::getName).collect(Collectors.joining(", "));

        String hardestCourse = courseList.stream()
                .filter(course -> course.getAveragePerAssignment() > 0)
                .filter(course -> course.getAveragePerAssignment() == courseList.stream()
                        .min(Comparator.comparingDouble(Course::getAveragePerAssignment)).get().getAveragePerAssignment())
                .map(Course::getName).collect(Collectors.joining(", "));

        if (mostPopular.isEmpty()) mostPopular = "n/a";
        if (leastPopular.isEmpty() || leastPopular.equals(mostPopular)) leastPopular = "n/a";

        if (highestActivity.isEmpty()) highestActivity = "n/a";
        if (lowestActivity.isEmpty() || lowestActivity.equals(highestActivity)) lowestActivity = "n/a";

        if (easiestCourse.isEmpty()) easiestCourse = "n/a";
        if (hardestCourse.isEmpty() || hardestCourse.equals(easiestCourse)) hardestCourse = "n/a";


        System.out.println("Most popular: " + mostPopular);
        System.out.println("Least popular: " + leastPopular);
        System.out.println("Highest activity: " + highestActivity);
        System.out.println("Lowest activity: " + lowestActivity);
        System.out.println("Easiest course: " + easiestCourse);
        System.out.println("Hardest course: " + hardestCourse);

        boolean stopLoop = false;
        while (!stopLoop) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("back")) {
                stopLoop = true;
                continue;
            }

            if (!validateInputInCourseNames(input)) {
                System.out.println("Unknown course.");
                continue;
            }

            Course courseInfo =
                    Objects.requireNonNull(
                                    Arrays.stream(values())
                                            .filter(coursesDataStore ->
                                                    coursesDataStore
                                                            .getInstance()
                                                            .getName()
                                                            .equalsIgnoreCase(input)
                                            )
                                            .findFirst()
                                            .orElse(null))
                            .getInstance();


            System.out.println(courseInfo.getName());
            System.out.println("id\tpoints\tcompleted");
            students.stream()
                    .filter(student -> courseInfo.hasAlreadyEnrolled(student.getID()))
                    .filter(student -> student.getPointsForCourse(courseInfo) > 0)
                    .sorted((s1, s2) -> {
                        if(s1.getPointsForCourse(courseInfo) > s2.getPointsForCourse(courseInfo)) {
                            return -1;
                        } else if (s1.getPointsForCourse(courseInfo) < s2.getPointsForCourse(courseInfo)) {
                            return 1;
                        } else {
                            return s1.getID().compareTo(s2.getID());
                        }
                    } )
                    .forEach(student -> {
                        System.out.println(student.getID() + "\t" + student.getPointsForCourse(courseInfo) + "\t" + student.getCompletionPercentage(courseInfo) + "%");
                    });
        }
    }

    private boolean isAllCoursesCountZeroBy(List<Course> courses, Predicate<Course> predicate) {
        for (Course course : courses) {
            if (predicate.test(course)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateInputInCourseNames(String input) {
        for (CoursesDataStore courseEnum : CoursesDataStore.values()) {
            if (input.equalsIgnoreCase(courseEnum.getInstance().getName())) {
                return true;
            }
        }
        return false;
    }

    private void findStudentAndPrintPoints() {
        System.out.println("Enter an id or 'back' to return:");
        boolean stop = false;
        while (!stop) {
            String id = scanner.nextLine();
            if (id.equalsIgnoreCase("back")) {
                stop = true;
                continue;
            }
            Student student = students.stream()
                    .filter(s -> s.getID().equals(id))
                    .findFirst()
                    .orElse(null);
            if (student == null) {
                System.out.printf("No student is found for id=%s.%n", id);
                continue;
            }

            Objects.requireNonNull(student).printPoints();
        }

    }

    private void addPointsToStudent() {
        System.out.println("Enter an id and points or 'back' to return:");
        boolean stop = false;
        while (!stop) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("back")) {
                stop = true;
                continue;
            }

            String[] inputs = input.split("\\s+");

            if (!validateAddPoints(inputs)) {
                continue;
            }

            Student student = students.stream()
                    .filter(s -> s.getID().equals(inputs[0]))
                    .findFirst()
                    .orElse(null);
            if (student == null) {
                System.out.printf("No student is found for id=%s.%n", inputs[0]);
                continue;
            }

            for (int i = 1; i < inputs.length; i++) {
                if (inputs[i].matches("^[0-9]+$")) {
                    int point = Integer.parseInt(inputs[i]);
                    switch (i) {
                        case 1 -> student.updatePoints(JAVA.getInstance(), point);
                        case 2 -> student.updatePoints(DSA.getInstance(), point);
                        case 3 -> student.updatePoints(DATABASE.getInstance(), point);
                        case 4 -> student.updatePoints(SPRING.getInstance(), point);
                    }
                }
            }
            System.out.println("Points updated.");
        }

    }

    private boolean validateAddPoints(String[] inputs) {
        if (inputs.length != 5) {
            System.out.println("Incorrect points format.");
            return false;
        }

        for (int i = 1; i < inputs.length; i++) {
            if (!inputs[i].matches("^[0-9]+$")) {
                System.out.println("Incorrect points format.");
                return false;
            }
        }

        if (students.stream().noneMatch(student -> student.getID().equals(inputs[0]))) {
            System.out.printf("No student is found for id=%s.%n", inputs[0]);
            return false;
        }

        return true;
    }

    private void printStudents() {
        if (students.isEmpty()) {
            System.out.println("No students found");
            return;
        }
        System.out.println("Students:");
        students.forEach(System.out::println);

    }

    private void back(int addedStudentsCount) {
        if (addedStudentsCount == -1) {
            System.out.println("Enter 'exit' to exit the program.");
        } else {
            System.out.printf("Total %d students have been added.%n", addedStudentsCount);
        }
    }

    private void addStudents() {
        System.out.println("Enter student credentials or 'back' to return:");
        int addedStudentCount = 0;
        boolean isAdding = true;
        while (isAdding) {
//            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("back")) {
                back(addedStudentCount);
                isAdding = false;
                continue;
            } else if (input.isEmpty()) {
                System.out.println("Incorrect credentials.");
                continue;
            }

            String[] words = input.split("\\s+");
            if (words.length < 3) {
                System.out.println("Incorrect credentials.");
                continue;
            }

            String firstName = words[0];
            String lastName = Stream.of(words)
                    .skip(1)
                    .limit(words.length - 2)
                    .reduce("", (a, b) -> a + " " + b)
                    .trim();
            String email = words[words.length - 1];


            if (!validateStudent(firstName, lastName, email)) {
                continue;
            }

            if (isStudentWithGivenEmailPresent(email)) {
                System.out.println("This email is already taken");
                continue;
            }

            Student student = new Student(firstName, lastName, email);
            students.add(student);
            addedStudentCount++;
            System.out.println("The student has been added.");
        }

    }

    private boolean validateStudent(String firstName, String lastName, String email) {
        if (firstName.length() < 2 || !firstName.matches(nameRegex) || !firstName.matches(adjacentRegex)) {
            System.out.println("Incorrect first name.");
            return false;
        } else if (lastName.length() < 2 || !lastName.matches(nameRegex) || !lastName.matches(adjacentRegex)) {
            System.out.println("Incorrect last name.");
            return false;
        } else if (!email.matches(emailRegex)) {
            System.out.println("Incorrect email.");
            return false;
        }
        return true;
    }

    private boolean isStudentWithGivenEmailPresent(String email) {
        return students.stream().anyMatch(student -> student.getEmail().equals(email));
    }

    private void exit() {
        isGoing = false;
        System.out.println("Bye!");
    }

    public void start() {
        isGoing = true;
        System.out.println("Learning Progress Tracker");
        while (isGoing) {
//            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.isEmpty()) {
                System.out.println("No input!");
            } else if (!commandMap.containsKey(command)) {
                System.out.println("Error: unknown command!");
            } else {
                commandMap.get(command).run();
            }
        }
    }
}
