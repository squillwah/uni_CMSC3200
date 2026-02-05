import java.io.*;

// [CMSC3200] Technical Computing Using Java
// Program 1: Average
//
//  FileCopy program.
//  Parse and count the words/digits in an input file, write statistics to an output file.
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu


/* ======================================================================================
 * CLog
 *
 *  Wrapper for printing to System.out.
 *  Provides toggles & formatting for plain/log/error/warning messages.
 *
 *  Methods:
 *   - set_mode(int m) 
 *      Allows toggling of logs, errors, and warnings.
 *   - say(String msg)
 *      Prints message to System.out and flushes buffer.
 *   - sayln(String msg)
 *      Prints message to System.out with new line.
 *   - log(String msg)
 *      Prints message with log formatting
 *   - warn(String msg)
 *      Prints message with warning formatting
 *   - error(String msg)
 *      Prints message with error formatting
 *
 * ====================================================================================== */

class CLog {
    private static int mode;
    
    public static final int LOGS     = 0b001;
    public static final int ERRORS   = 0b010;
    public static final int WARNINGS = 0b100;

    private CLog() {
        mode = LOGS+ERRORS+WARNINGS;
    }

    public static void set_mode(int m) { mode = m; }
    
    public static void say(String msg) { 
        System.out.print("> " + msg);
        System.out.flush();
    }
    public static void sayln(String msg) {
        System.out.println("> " + msg);
    }

    public static void log(String msg) { if (mode & LOGS) sayln("log: " + msg) }
    public static void warn(String msg) { if (mode & WARNINGS) sayln("war: " + msg) }
    public static void error(String msg) { if (mode & ERRORS) sayln("err: " + msg) }
}


/* ======================================================================================
 * FileTools
 *
 *  Defines functions for file operations needed in this program.
 *
 *  Methods:
 *   - backup(String file_name)
 *      Makes a backup of the given file, appended with .bak
 *      If a backup already exists, it creates another backup of the backup.
 *
 * ====================================================================================== */

class FileTools {
    private FileTools();
    
    public static void backup_file(String file_name) {
        //
    }
    
    public static boolean file_exists(String file_name) {

    }
    
    public static BufferedReader open_if(String file_name) {

    }
    
    public static PrintWriter open_of(String file_name) {

    }
}


/* ======================================================================================
 * WordTools
 *
 *  Word processing procedures.
 *
 *  Methods:
 *   - is_int(String word)
 *      Returns true if given string is an integer, false otherwise.
 *
 * ====================================================================================== */

class WordTools {
    private WordTools();
    
    public static boolean is_int(String word) {
        boolean is = true;
        try {
            Integer.parseInt(word);
        } catch (NumberFormatExtension e) {
            is = false;
        }
        return is;
    }
}


/* ======================================================================================
 * IOFileState
 *  
 *  Takes filename strings and evaluates them.
 *  Provides functionality to signal and explain why an input/output filename is invalid.
 *  
 *  Does checks on file names:
 *   - Was a filename given?
 *   - Does a corresponding file exist?
 *   - Are the input and output filenames different?
 *  
 *  Maintains a 'status' integer, whose bits represent each check.
 *   - Failed checks result in a positively flipped bit.
 *   - The nominal state is a status of '0', all checks passed.
 *
 *  Methods:
 *   - set_<input/output>(String file_name)
 *      Set the interal input or output file name and run checks.
 *      Attempt creation of an input or output file object, given filename.
 *   - get_<input/output>()
 *      Return the internal input/output file name.
 *      Returns reference to IFile/OFile object, or null if it could not be created. 
 *   - get_status()
 *      Returns the status code.
 *
 * ====================================================================================== */

class IOFileState {
    private int status;
    private String input_file;
    private String output_file;
    
    public static final int INPUT_NOTGIVEN  = 0b00001;
    public static final int INPUT_NOEXIST   = 0b00010;
    public static final int OUTPUT_NOTGIVEN = 0b00100;
    public static final int OUTPUT_DOEXIST  = 0b01000;
    public static final int OUTPUT_IS_INPUT = 0b10000;

    public IOFileState() { this(""); }
    public IOFileState(String in) { this(in, ""); }
    public IOFileState(String in, String out) {
        status = 0;
        set_input(in); 
        set_output(out); 
    }

    public void set_input(String file_name) { 
        input_file = file_name;
        status &= ~(INPUT_NOTGIVEN + INPUT_NOEXIST + OUTPUT_IS_INPUT);
        if (file_name.isEmpty()) status |= INPUT_NOTGIVEN;
        else if (!File(file_name).exists()) status |= INPUT_NOEXIST;
        else if (input_file == output_file) status |= OUTPUT_IS_INPUT;
    }
    public void set_output(String file_name) { 
        output_file_name = file_name;
        status &= ~(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST + OUTPUT_IS_INPUT);
        if (file_name.isEmpty()) status |= OUTPUT_NOTGIVEN;
        else if (!File(file_name).exists()) status |= OUTPUT_DOEXIST;
        else if (input_file == output_fille) status |= OUTPUT_IS_INPUT;
    }

    public int get_status() { return status; }
    public String get_input() { return input_file_name; }
    public String get_output() { return output_file_name; }
}


/* ======================================================================================
 * FileCopy
 *
 *  Parses words in input file and prints statistics to output file.
 *
 * ====================================================================================== */

public class FileCopy {
    public static void main(String args[]) {
        // Construct IOFileState from args.
        IOFileState files = null;
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
            // Handle empty file names on first occurences, then set as exit states for future occurences.
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
            CLog.log("Cancelling FileCopy.");
        } else {
            CLog.error("Couldn't recover from error state '" + files_status + "'.");   // Non-exit error state maintained, shutdown unintended.
        }
        
        CLog.sayln("Exiting program, goodbye!");
    }
}





