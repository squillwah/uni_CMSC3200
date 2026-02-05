
import java.io.*;


/* ======================================================================================
 * IOFileState
 *  
 *  Takes filename strings and attempts to create input and output File objects.
 *  Provides functionality to signal and explain why some Files cannot be created.
 *  
 *  Does checks on files:
 *   - Is the filename valid?
 *   - Does the file exist?
 *   - Are the input and output files different?
 *  
 *  Maintains a 'status' integer, whose bits correspond to each check.
 *   - Failed checks result in a positively flipped bit.
 *   - The nominal state is a status of '0', all checks passed.
 *
 *  Methods:
 *   - set_<input/output>(String file_name)
 *      Attempt creation of an input or output file object, given filename.
 *   - get_<input/output>()
 *      Returns reference to IFile/OFile object, or null if it could not be created. 
 *   - get_status()
 *      Returns the status code.
 *
 * ====================================================================================== */

class IOFileState {
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
        status &= ~(INPUT_NOTGIVEN + INPUT_NOEXIST);
        if (file_name.isEmpty()) status |= INPUT_NOTGIVEN;
        if (!check_exists(file_name)) status |= INPUT_NOEXIST;
    }
    // zzz
    public void set_output(String file_name) { 
        output_file_name = file_name;
        status &= ~(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST);
        if (file_name.isEmpty()) status |= OUTPUT_NOTGIVEN;
        if (check_exists(file_name)) status |= OUTPUT_DOEXIST;
    }

    public int get_status() { return status; }
    public String get_input() { return input_file_name; }
    public String get_output() { return output_file_name; }

    private static boolean check_exists(String file_name) {
        return true; // placeholder
    }
}

public class FileCopy {
    public static void main(String args[]) {
        // Construct IOFileState from args.
        IOFileState files;
        switch (args.length) {
            case 0:
                files = new IOFileState();
                break;
            case 1:
                files = new IOFileState(args[0]);
                break;
            default:
                files = new IOFileState(args[0], args[1]);
                break;
        }

        // Attempt to fix error states.
        int files_status = files.get_status();
        int files_status_exit_states = 0;
        while ((files_status & ~files_status_exit_states) > 0) {    // Disregard exit states. 
            // Handle on empty IO file names on first occurences, then set as exit states for following occurences.
            if ((files_status & IOFileState.INPUT_NOTGIVEN) > 0) {
                files_status_exit_states |= IOFileState.INPUT_NOTGIVEN;
            }
            if ((files_status & IOFileState.OUTPUT_NOTGIVEN) > 0) {
                files_status_exit_states |= IOFileState.OUTPUT_NOTGIVEN;
            }
            // Reprompt non-existent input files.
            if ((files_status & IOFileState.INPUT_NOEXIST) > 0) {
                //
            }
            // Reprompt pre-existent output files, allow backup+overwrite, or quit.
            if ((files_status & IOFileState.OUTPUT_DOEXIST) > 0) {
                if (quit) {
                    files_status_exit_states |= IOFileState.OUTPUT_DOEXIST; // or files_status_exit_states = files.get_status(); for a more clear "exit now"
                }
            }
            files_status = files.get_status();
        }
        
        // Proceed with tokenization/file copy if status is nominal.
        if (files_status == 0) {  
 
        } else if (files_status & files_status_exit_states > 0) {                               // Exit state encountered, program shutdown is deliberate.
            System.println("Cancelling FileCopy.");
        } else {
            System.println("Err: couldn't recover from error state '" + files_status + "'.");   // Non-exit error state maintained, shutdown unintended.
        }
        
        System.println("Exiting program.");

        //files.closeem();
    }
}





