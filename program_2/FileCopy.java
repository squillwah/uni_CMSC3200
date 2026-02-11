
// [CMSC3200] Technical Computing Using Java
// Program 2: FileCopy
//
//  FileCopy program.
//  Parse and count the words/digits in an input file, write statistics to an output file.
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu

import java.io.*;
import java.util.*;

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
    private FileTools() {}
    
    public static void rename_file(String old_name, String new_name) {
        if (file_exists(new_name)) {
            System.out.println("File '" + new_name + "' already exists, backing up to '" + new_name + ".bak'.");
            rename_file(new_name, new_name+".bak");
        }

        // rename the file
    }
    
    //  POTENTIAL MEMORY LEAK IN VERY NEICHE EDGE CASE  
    public static boolean file_exists(String file_name) {
        return (new File(file_name)).exists();
    }
    
    public static BufferedReader open_ifstream(String file_name) {
        BufferedReader inFile = null;

        try {
            inFile = new BufferedReader(new FileReader(file_name));
        } catch (IOException e) {
            System.err.println("Error creating BufferedReader for '" + file_name + "': " + e.getMessage());
            System.out.print("Couldn't open input file for reading, ");
            if (FileTools.file_exists(file_name)) System.out.println("though file exists. Are your permissions correct?");
            else System.out.println("was the file deleted or moved after selection?");
        }
        return inFile;
    }

    public static PrintWriter open_ofstream(String file_name) {
        PrintWriter outFile = null;

        try {
            outFile = new PrintWriter(new FileWriter(file_name));
        } catch (IOException e) {
            System.err.println("Error creating PrintWriter for '" + file_name + "': " + e.getMessage());
            System.out.println("Couldn't open output file for writing.");
        }
        return outFile;
    }

    public static boolean close_ofstream(PrintWriter ofstream) {
        boolean closed = false;
        //try {                     not needed
            ofstream.close(); 
            closed = true;
        //}
        //catch (IOException e) { 
        //    System.out.println("Err: IOException during input file close: " + e); // Should we continue?
        //}
        return closed;
    }
    public static boolean close_ifstream(BufferedReader ifstream) {
        boolean closed = false;
        try { 
            ifstream.close(); 
            closed = true;
        }
        catch (IOException e) { 
            System.out.println("Err: IOException during output file close: " + e); // Should we continue?
        }
        return closed;
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
    private WordTools() {} // Should we have these empty constructors for purely functional classes?
    
    public static boolean is_int(String word) {
        boolean is = true;
        try {
            Integer.parseInt(word);
        } catch (NumberFormatException e) {
            is = false;
        }
        return is;
    }

    // @todo: more processing?
    public static String normalize_line(String line_dirty) {
        String line_clean = line_dirty.trim().toLowerCase();
        return line_clean;
    }

//    // Safely read line from a BufferedReader, return normalized string.
//    public static String read_normalized_line(BufferedReader buffer)
//        safe_buffered_read

    public static int find_word(String word, Word[] words, int words_size) {
        int index = -1;
        for (int i = 0; i < words_size && index < 0; i++)
            if (words[i].get_text().equals(word))
                index = i;
        return index;
    }

    public static String token_line(String line) {
        return null;
    }
}

class Word {
    String word;
    int count;

    public Word(String word){
        this.word = word;
        this.count = 1;
    }

    public void incriment() {
        count++;
    }

    public String get_text() { return word; }
    public int get_count() { return count; }
}

/* ======================================================================================
 * IOFileNameState
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
 *   - get_<input/output>()
 *      Return the internal input/output file name.
 *   - get_status()
 *      Returns the status code.
 *
 * ====================================================================================== */
class IOFileNameState {
    private int status;
    private String input_file;
    private String output_file;
    
    public static final int INPUT_NOTGIVEN  = 0b00001;
    public static final int INPUT_NOEXIST   = 0b00010;
    public static final int OUTPUT_NOTGIVEN = 0b00100;
    public static final int OUTPUT_DOEXIST  = 0b01000;
    public static final int OUTPUT_IS_INPUT = 0b10000;

    public IOFileNameState() { this(""); }
    public IOFileNameState(String in) { this(in, ""); }
    public IOFileNameState(String in, String out) {
        status = 0;
        set_input(in); 
        set_output(out); 
    }

    public void set_input(String file_name) { 
        input_file = file_name;
        check_input();
    }
    public void set_output(String file_name) { 
        output_file = file_name;
        check_output();
    }

    public void check_input() {
        status &= ~(INPUT_NOTGIVEN + INPUT_NOEXIST + OUTPUT_IS_INPUT);
        if (input_file.isEmpty()) status |= INPUT_NOTGIVEN;
        else if (!FileTools.file_exists(input_file)) status |= INPUT_NOEXIST;
        else if (input_file.equals(output_file)) status |= OUTPUT_IS_INPUT; // could comparing against initial null output_file cause issues?
    }
    public void check_output() {
        status &= ~(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST + OUTPUT_IS_INPUT);
        if (output_file.isEmpty()) status |= OUTPUT_NOTGIVEN;
        else if (FileTools.file_exists(output_file)) status |= OUTPUT_DOEXIST;
        else if (input_file.equals(output_file)) status |= OUTPUT_IS_INPUT; 
    }

    // More readable method of resetting input/output filenames.
    public void clear_input() { set_input(""); }
    public void clear_output() { set_output(""); }

    public int get_status() { return status; }
    public String get_input() { return input_file; }
    public String get_output() { return output_file; }
}


// ====================================================================================================
// Prompts
//
//  Collection of static procedures for fixing IOFileNameState errors with command line prompts.
//  Each method exacts changes upon the IOFileNameState object supplied in its arguments.
//  All methods return IOFileNameState status codes which should exit the program (user chose to quit).
//   - Typically being the state which they're fixing (and the user chose to quit instead), but
//     can be a different state for more complex prompts like bad_output().
//
// ======================================================================================================
class Prompts {
    private static BufferedReader stdin = null;
    
    public static void open_stdin() {
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    public static void close_stdin() {
        try {
            stdin.close();
            stdin = null;
        } catch (IOException e) {
            System.out.println("An IOException occurred during BufferedReader close: " + e);
        }
    }

    public static int no_input(IOFileNameState filenames) {
        int quit_states = 0;
        filenames.set_input(catchLine("Please specify an input file (or nothing to quit): "));
        quit_states = filenames.get_status() & IOFileNameState.INPUT_NOTGIVEN; // If nothing was given, set INPUT_NOTGIVEN as quit state.
        return quit_states;
    }
    public static int no_output(IOFileNameState filenames) {
        int quit_states = 0;
        filenames.set_output(catchLine("Please specify an output file (or nothing to quit): "));
        quit_states = filenames.get_status() & IOFileNameState.OUTPUT_NOTGIVEN;
        return quit_states;
    }

    // Alternatively could handle the reinput itself, but currently clears input_file (throwing the prompt to no_input later on)
    public static int bad_input(IOFileNameState filenames) {
        int quit_states = 0;
        System.out.println("The input file '" + filenames.get_input() + "' doesn't exist.");
        //filenames.clear_input(); // Resets INPUT_NOEXIST and sets INPUT_NOTGIVEN, allow Prompts.no_input() to handle reentry.
        filenames.set_input(catchLine("Please enter a different one (or nothing to quit): "));
        quit_states = filenames.get_status() & IOFileNameState.INPUT_NOTGIVEN;
        return quit_states;
    }

    public static int bad_output(IOFileNameState filenames) {
        int quit_states = 0;
        System.out.println("The output file '" + filenames.get_output() + "' already exists.");
        System.out.println("You can\n 1. Pick Another\n 2. Backup & Overwrite\n 3. Quit");
        String choice = catchLine("What would you like to do? (pick/backup/quit): ").toLowerCase();
        boolean choosing = true;
        while (choosing) {
            switch (choice) {
                case "pick":
                    filenames.clear_output(); // Clear output, allow no_output() to handle reentry.
                    break;
                case "backup":
                    // Simpler to rename original, rather than copying everything only to overwrite.
                    FileTools.rename_file(filenames.get_output(), filenames.get_output()+".bak"); // ! what happens if output is set as input? should check that case first
                    filenames.check_output(); // Should clear OUTPUT_DOEXIST
                    break;
                case "quit":
                    quit_states |= IOFileNameState.OUTPUT_DOEXIST;
                    break;
                default:
                    choice = catchLine("Please enter 'pick', 'backup', or 'quit': ").toLowerCase();
                    continue;
            }
            choosing = false;
        } 
        return quit_states;
    }

    // Will probably never reach this, since bad_output detects preexisting files and is handled first.    
    public static int io_same(IOFileNameState filenames) {
        int quit_states = 0;
        System.out.println("Your input file and output file are the same ('" + filenames.get_input() + "').");
        filenames.clear_input();
        filenames.clear_output();
        return quit_states;
    }
   
    // Helper for getting input with a prompt. 
    // Prints prompt, opens stdin, reads a line, closes stdin, and catches any IOExceptions. 
    private static String catchLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        String line = "";
        try {
            line = stdin.readLine().trim();
        } catch (IOException e) {
            System.out.println("An IOException occurred during line read: " + e);
        }
        return line;
    }
}

// Could have a more complex OOP class
/*
class WordsAndSum {
    public static final int VOCAB_SIZE = 100;
   
    private int integer_sum; 
    private int word_count;
    private Word words[];

    public WordsAndSum() {
        integer_sum = 0;
        word_count = 0;
        words = new Word[VOCAB_SIZE];
    }

    public int get_integer_sum() { return integer_sum; }
    public int get_word_count() { return word_count; }

    public int add_word(
}*/

// Or simple data container
class FileWordData {
    public static final int MAX_WORDS = 100;
    public int sum;
    public int word_count;
    public Word[] words;

    public FileWordData() { 
        words = new Word[MAX_WORDS]; 
        word_count = 0; 
        sum = 0; 
    }
}


/* ======================================================================================
 * FileCopy
 *
 *  Parses words/numbers in input file and prints occurence count to output file.
 *  Filenames can be supplied as arguments or prompted during runtime.
 *
 * ====================================================================================== */
public class FileCopy {
    public static void main(String args[]) {
        // Create a filename state object, with or without defaults from command line. 
        IOFileNameState filenames = null;
        switch (args.length) {
            case 0:
                filenames = new IOFileNameState();
                break;
            case 1:
                filenames = new IOFileNameState(args[0]);
                break;
            default:
                filenames = new IOFileNameState(args[0], args[1]);
                System.out.println("hello");
                break;
        }
       
        // If filename errors present, fix with interactive prompts
        int fn_status = filenames.get_status(); 
        int fn_quit_states = 0; // Allow for quitting by signaling an error state as a 'quit state'.
        System.out.println(fn_status);
        Prompts.open_stdin();
        while (fn_status != 0 && (fn_status & fn_quit_states) == 0) {
            if ((fn_status & IOFileNameState.INPUT_NOTGIVEN) != 0)
                fn_quit_states |= Prompts.no_input(filenames);
            else if ((fn_status & IOFileNameState.OUTPUT_NOTGIVEN) != 0)
                fn_quit_states |= Prompts.no_output(filenames);
            else if ((fn_status & IOFileNameState.INPUT_NOEXIST) != 0)
                fn_quit_states |= Prompts.bad_input(filenames);
            else if ((fn_status & IOFileNameState.OUTPUT_DOEXIST) != 0)
                fn_quit_states |= Prompts.bad_output(filenames);
            else if ((fn_status & IOFileNameState.OUTPUT_IS_INPUT) != 0)
                fn_quit_states |= Prompts.io_same(filenames);
            fn_status = filenames.get_status();
        }
        Prompts.close_stdin();

        // Proceed if filenames nominal
        if (fn_status == 0) {
            BufferedReader inFile = FileTools.open_ifstream(filenames.get_input());
            PrintWriter outFile = FileTools.open_ofstream(filenames.get_output());
            boolean files_ready = inFile != null && outFile != null;

            if (files_ready) {
                // Process words/integer sum from input file.
                FileWordData wordsums = process_input_file(inFile);
                FileTools.close_ifstream(inFile);

                // Write results to output file.
                outFile.println("Total Integer Sum: " + wordsums.sum + "\n-----------------------");
                for (int i = 0; i < wordsums.word_count; i++) 
                    outFile.println(wordsums.words[i].get_text() + ": " + wordsums.words[i].get_count());
                FileTools.close_ofstream(outFile);
            } else  
                System.out.println("An error occured which prevented the tokenization/file copy operation. Quitting."); 
        } else {
            if ((fn_status & fn_quit_states) != 0) 
                System.out.println("Quitting, goodbye!");
            else // Non quit state means unintentional (bugged) quit
                System.out.println("Err: couldn't recover from filename state '" + fn_status + "'. Exiting.");
        }
    }

    // Parse file, count word tokens and sum all integers. Return results in FileWordData 'struct'.
    public static FileWordData process_input_file(BufferedReader inFile) {
        FileWordData wordsums = new FileWordData();

        String line = null;
        try { line = inFile.readLine(); } 
        catch (IOException e) { System.out.println("Err: couldn't first line from input file."); }

        while (line != null) {
            // Debug, just print the line
            System.out.println(line);
            try { line = inFile.readLine(); } 
            catch (IOException e) { System.out.println("Err: couldn't line after '" + line + "' from input file."); }
            
            // @todo: the tokenize, parse words from numbers, store in WordsAndSum 
            /*String delimmiters = "\t\n\r";
                
            // Wordify tokens in line, advance to next line and repeat until eof or max words reached. 
            while (line != null && word_count < VOCAB_SIZE) {
                StringTokenizer tokens = new StringTokenizer(WordTools.normalize_line(line), delimmiters);
                while (tokens.hasMoreTokens() && word_count < VOCAB_SIZE) {
            }*/

            /*
            //  tokenize line by line
            try {
                WordTools.token_line(inFile.readLine().toLowerCase());
            } catch(IOException e) {
                System.err.println("error at line 310 someone should fix this");        //  IF ANYONE SEES THIS FIX IT, IT SHOULD BE FIXED BEFORE ITS SEEN BUT I HAVE BEEN KNOWN TO FORGET THINGS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            }*/
            
            //  write to output file
            // alternatively, maybe tokenize entire file, remove duplicates, then seperate words/numbers and add?
        }   

        return wordsums;
    }
}





