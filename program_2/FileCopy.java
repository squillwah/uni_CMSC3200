
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

    public static int parse_args(String args[]) {

    }

    // @todo: make this less redundant between input and output
    public void set_input(String file_name) { 
        input_file_name = null;
        status ^= !(INPUT_NOTGIVEN + INPUT_NOEXIST);
        if (!file_name.is_empty) status | INPUT_NOTGIVEN;
        else if (!checkexist(file_name)) status | INPUT_NOEXIST;
        else input_file_name = file_name;
    }
    // zzz
    public void set_output(String file_name) { 
        input_file_name = null;
        status ^= !(INPUT_NOTGIVEN + INPUT_NOEXIST);
        if (!file_name.is_empty) status | INPUT_NOTGIVEN;
        else if (!checkexist(file_name)) status | INPUT_NOEXIST;
        else input_file_name = file_name;
    }

    public void set_output(String file_name) { output_file_name = file_name; }

    public int get_status()

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
