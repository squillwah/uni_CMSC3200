
public class IOFileState {
    private int status;
    private String input_file_name;
    private String output_file_name;
    
    // Possible error states 
    public static final int INPUT_NOTGIVEN  = 0b0001;
    public static final int INPUT_NOEXIST   = 0b0010;
    public static final int OUTPUT_NOTGIVEN = 0b0100;
    public static final int OUTPUT_DOEXIST  = 0b1000;

    public IOFileState(String in = "", String out = "") {
        status = 0;
        set_input(in);
        set_output(out);
    }

    public void set_input(String file_name) { 
        input_file_name = file_name;
        status ^= !(INPUT_NOTGIVEN + INPUT_NOEXIST);
        status |= !file_name.is_empty() * INPUT_NOTGIVEN;
        status |= !check_exists(file_name) * INPUT_NOEXIST;
    }
    // zzz
    public void set_output(String file_name) { 
        output_file_name = file_name;
        status ^= !(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST);
        status |= !file_name.is_empty() * OUTPUT_NOTGIVEN;
        status |= check_exists(file_name) * OUTPUT_DOEXIST;
    }

    public int get_status() { return status; }
}

public class FileCopy {
    public static void main(String args[]) {
        // Parse argument array for valid file names
        
        int arguments_status = ArgStatus.parse_args(args);
        boolean good_arguments = ArgStatus.check(arguments_status)

        while (!good_arguments) {
            // reprompt for input

            if (command_code & CommandStatusCodes.)*/

    }
}
