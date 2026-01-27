
import java.io.*;

public class Average {
    private static final int MIN_GRADE = 0;
    private static final int MAX_GRADE = 100;
    
    public static void main(String args[]) throws IOException {
        System.out.println("\n CMSC3200 / Group 2\n" +
                             " Grade Averaging Program 1\n" +
                             " Brandon Schwartz, Joshua Staffen, Ravi Dressler\n" +
                             " SCH81594@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu\n");
        System.out.println("Welcome to the grade averaging program.\n" +
                           "Input grades between " + MIN_GRADE + " and " + MAX_GRADE + ".\n" +
                           "Enter a number outside those bounds to quit the program and display your average.\n");

        // Open character stream from standard input
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        
        int grade_count = 0;
        double grade_total = 0;
        double grade = get_grade(stdin);

        // Accumulate grades while within bounds
        while (grade >= MIN_GRADE && grade <= MAX_GRADE) {
            grade_count++;
            grade_total += grade;
            grade = get_grade(stdin);
        }

        // Display results 
        System.out.println("\nYou entered " + grade_count + " grades, totaling " + grade_total + ".\n" +
                             "Calculating average now...\n");
        
        double average = grade_total / grade_count;

        if (Double.isNaN(average)) {
            System.out.println("Err: No grades entered, cannot divide by zero. Exiting...");
        } else if (Double.isInfinite(average)) {
            System.out.println("Err: Result is infinite, how did that happen? Exiting...");
        } else {
            System.out.println("The average grade is: " + average);
        }

        stdin.close();
    }

    // Return grade input (as double) from BufferedReader
    public static double get_grade(BufferedReader stdin) throws IOException{
        try {
            System.out.print("Enter grade [" + MIN_GRADE + '-' + MAX_GRADE + "]: ");
            System.out.flush();
            return Double.parseDouble(stdin.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Grade must be a number!");
        }
        // Repeat until input can be parsed as double
        return get_grade(stdin);
    }
}


