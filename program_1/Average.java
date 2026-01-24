
import java.io.*;

public class Average {
    public static void main(String args[]) throws IOException {
        System.out.println("\n CMSC3200 / Group ?\n Grade Averaging Program\n Brandon Schwartz, Joshua Staffen, Ravi Dressler\n");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        final int MIN_GRADE = 0;
        final int MAX_GRADE = 100;
        int grade_count = 0;
        double grade_total = 0;
        double grade = get_grade(stdin);

        while (grade >= MIN_GRADE && grade <= MAX_GRADE) {
            grade_count++;
            grade_total += grade;
            grade = get_grade(stdin);
        }

        if (grade_total > 0) {
            System.out.println("The average of your " + grade_count + " grades is: " + grade_total/grade_count);
        } else {
            System.out.println("No grades entered, there is nothing to do.");
        }
    }

    public static double get_grade(BufferedReader stdin) throws IOException{
        try {
            System.out.print("Enter grade: ");
            System.out.flush();
            return Double.parseDouble(stdin.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Grade must be a number!");
        }
        return get_grade(stdin);
    }
}


