
import java.io.*;

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
        status &= ~(INPUT_NOTGIVEN + INPUT_NOEXIST);
        status |= !file_name.isEmpty() * INPUT_NOTGIVEN;
        status |= !check_exists(file_name) * INPUT_NOEXIST;
    }
    // zzz
    public void set_output(String file_name) { 
        output_file_name = file_name;
        status &= ~(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST);
        status |= !file_name.isEmpty() * OUTPUT_NOTGIVEN;
        status |= check_exists(file_name) * OUTPUT_DOEXIST;
    }

    public int get_status() { return status; }
    public String get_input() { return input_file_name; }
    public String get_output() { return output_file_name; }
}

public class FileCopy {
    public static void main(String args[]) {

        IOFileState files = new IOFileState("", "");

        int files_status = files.get_status();
        int files_status_exit_states = IOFileState.INPUT_NOTGIVEN;      // End program if this state is reached.
        while ((files_status & files_status_exit_states) == 0) {
            // Fix error states if present.
            if (files_status > 0) { 
                while ((files_status & ~files_status_exit_states) > 0) {    // Disregard exit states.
                    //if ((files_status & IOFileState.INPUT_NOTGIVEN) > 0)  
                    if ((files_status & IOFileState.INPUT_NOEXIST) > 0) {
                        //
                    }
                    if ((files_status & IOFileState.OUTPUT_NOTGIVEN) > 0) {
                        //
                    }
                    if ((files_status & IOFileState.OUTPUT_DOEXIST) > 0) {
                        //
                    }
                    files_status = files.get_status();
                }
                continue;
            }

            // Copy files
            
            // Prompt new inputs

            files_status = files.get_status();
        }
    }
}

