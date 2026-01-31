
// Should we have one of these, composed inside of IOFileState instead of basic Strings?
public class File {
    String name;
    fstream stream;
}

// Maybe this should just check files, and not store anything about them?
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
        status &= !(INPUT_NOTGIVEN + INPUT_NOEXIST);
        status |= !file_name.is_empty() * INPUT_NOTGIVEN;
        status |= !check_exists(file_name) * INPUT_NOEXIST;
    }
    // zzz
    public void set_output(String file_name) { 
        output_file_name = file_name;
        status &= !(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST);
        status |= !file_name.is_empty() * OUTPUT_NOTGIVEN;
        status |= check_exists(file_name) * OUTPUT_DOEXIST;
    }

    public int get_status() { return status; }
    public String get_input() { return input_file_name; }
    public String get_output() { return output_file_name; }

    private static boolean check_exists(String file_name);

    // should IOFileState also open file streams, or is that outside its scope?
}

public class FileCopy {
    public static void main(String args[]) {

        IOFileState files = new IOFileState();


        // Outer loop uses a boolean variable, which can be changed within loop
        // Inner loop should be moved to a verify_io_files procedure


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


/*
 * boolean noquit = verify_io_files(files)  // Don't like this, not an intuitive return
 * while (noquit) {
 *   1. copy files and do the thing
 *   2. prompt for new input/output files
 *   3. noquit = verify_io_files(files)
 */

// Maybe the tests for empty input (quit signal) should be done outside of the IOFileState entirely, and occur before ever checking IOFileState for fixes?
//
// zzz
//



// Or extract the INPUT_NOTGIVEN bit from the status string?


// Takes the approriate actions to handle IOFileState error states.
// Returns true if all errors were fixed and the IOFileState is nominal, 
//  or false if the exit condition was met and the program should quit.
public /*bool?*/ void verify_io_files(IOFileState files) {
    int status = files.get_status();
    boolean exit_condition = status & IOFileState.INPUT_NOTGIVEN;
    if (status && !exit_condition) { 
        //if (status & IOFileState.INPUT_NOTGIVEN) {} (this is where my break would go, IF I HAD ONE!)
        if (status & IOFileState.INPUT_NOEXIST) {
            //
        }
        if (status & IOFileState.OUTPUT_NOTGIVEN) {
            //
        }
        if (status & IOFileState.OUTPUT_DOEXIST) {
            //
        }
        return true * verify_io_files(files);
    }
    return !exit_condition;
}









