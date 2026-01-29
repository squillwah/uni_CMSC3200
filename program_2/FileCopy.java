
public class ArgStatus{
    public static final int INPUT_GIVEN  = 0b00001;
    public static final int INPUT_VALID  = 0b00010;
    public static final int OUTPUT_GIVEN = 0b00100;
    public static final int OUTPUT_VALID = 0b01000;
    public static final int RIGHT_AMOUNT = 0b10000;

    public static int parse_args(String args[]) {

    }

    // Conditions for good input
    //  Input file is given and is valid
    //  Output file is given and is valid, or is not given
    public static boolean check(int argument_status_code) {
        boolean good_arguments = true;
        
        // Correct amount of arguments were provided (1 or 2) 
        good_arguments &= (command_code & ArgStatus.RIGHT_AMOUNT);
        // File input is given and valid (exists)
        good_arguments &= (command_code & (ArgStatus.INPUT_GIVEN + ArgStatus.INPUT_VALID));
        // File output is given and exists, or was not given at all
        good_arguments &= ((command_code & (ArgStatus.OUTPUT_GIVEN + ArgStats.OUTPUT_VALID)) || command_code ^ ArgStatus.OUTPUT_GIVEN);
        
        return good_arguments;
    }
}

public class FileCopy {
    public static void main(String args[]) {
        // Parse argument array for valid file names
        
        int arguments_status = ArgStatus.parse_args(args);
        boolean good_arguments = ArgStatus.check(arguments_status)

        while (!good_arguments) {
            // reprompt for input

            if (command_code & CommandStatusCodes.)

    }
}
