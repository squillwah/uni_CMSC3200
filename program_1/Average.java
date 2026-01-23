
import java.io.*;

public class Average {
    public static void main(String args[]) throws IOException {
        System.out.println(" CMSC3200 / Group ?\n Grade Averaging Program\n Brandon Schwartz, Joshua Staffen, Ravi Dressler");

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        int grade_total = 0;
        int grade_count = -1;

        Double grade = new Double(0.0); 

        do {
            grade_total += grade;
            grade_count++;
            
            while (!get_grade_from_stdin(stdin, grade))
                System.out.println(" Bad value, must be a number.");

            System.out.println(grade);
        } while (grade >= 0 && grade <= 100);
            
        System.out.println(grade_total);
        System.out.println(grade_count);
    }

    public static boolean get_grade_from_stdin(BufferedReader stdin, Double grade) throws IOException {
        System.out.print("Enter a grade: ");
        System.out.flush();
        try {
            grade.set = Double.parseDouble(stdin.readLine());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}


