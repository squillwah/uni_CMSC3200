
public class IOFileState {
    private int status;
    private String input_file_name;
    private String output_file_name;
    
    // Possible error states 
    public static final int INPUT_NOTGIVEN  = 0b0001;
    public static final int INPUT_NOEXIST   = 0b0010;
    public static final int OUTPUT_NOTGIVEN = 0b0100;
    public static final int OUTPUT_DOEXIST  = 0b1000;

    public IOFileState() { this("",""); }
    public IOFileState(String in, String out) {
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

    private static boolean check_exists(String file_name);

    // should IOFileState also open file streams, or is that outside its scope?
}

public class FileCopy {
    public static void main(String args[]) {

        IOFileState files = new IOFileState();

        int files_status = files.get_status();
        while (!(files_status & IOFileState.INPUT_NOTGIVEN)) {      // Blank input file is exit condition.
            while (files_status) {      // Correct error states, if any.
                if (files_status & IOFileState.INPUT_NOEXIST) {
                    // Reprompt for input
                    // @bug: How can we quit from here? Empty input won't break the loop.
                    // zzz
                }
                if (files_status & IOFileState.OUTPUT_NOTGIVEN) {
                    // Reprompt for output
                }
                if (files_status & IOFileState.OUTPUT_DOEXIST) {
                    // Prompt for reentry, backup/overwrite, or quit
                }
                files_status = files.get_status();


                // @bug: Potential stuck state here, if INPUT_NOTGIVEN flipped and bypassed the outer loop.
                //        Dunno how that could happen though, excluding cosmic radiation.
                //        Although, maybe not if INPUT_NOEXIST is always set when INPUT_NOTGIVEN is set. Should they be the same? No, probs not.
            }

            // Copy files and do the thing

            // Get new input/output and refresh status
            files.set_input(/*prompt*/); 
            files.set_input(/*prompt*/); // might want to find some way to merge these prompts with initial?

            files_status = files.get_status(); // feels weird to call this in twice, but no other way?
        }



        /* old
        // Parse argument array for valid file names
        
        int arguments_status = ArgStatus.parse_args(args);
        boolean good_arguments = ArgStatus.check(arguments_status)

        while (!good_arguments) {
            // reprompt for input

            if (command_code & CommandStatusCodes.)*/

    }
}
