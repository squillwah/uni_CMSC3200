
// [CMSC3200] Technical Computing Using Java
// Program 2: FileCopy
//
//  FileCopy program.
//  Parse and count the words/digits in an input file, write findings to an output file.
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu

import java.io.*;
import java.util.*;

// ======================================================================================
// FileTools
//
//  Defines functions for file operations needed in this program.
//
//   - rename_file(String, String)
//   - file_exists(String)
//   - open_ifstream()/open_ofstream(String)
//   - close_ifstream(BufferedReader)/close_ofstream(PrintWriter)
//
// ====================================================================================== */
class FileTools {
    private FileTools() {}
   
    // Rename files given name and new name strings. Appends .bak to new_names which exist. 
    public static void rename_file(String old_name, String new_name) {
        System.out.println("Renaming '" + old_name + "' to '" + new_name + "'.");
        if (file_exists(new_name)) {
            System.out.println("File '" + new_name + "' already exists, backing up to '" + new_name + ".bak'.");
            rename_file(new_name, new_name+".bak");
        }
        (new File(old_name)).renameTo(new File(new_name));
    }
   
    // Return true if file corresponding to file_name exists, false if not. 
    public static boolean file_exists(String file_name) {
        return (new File(file_name)).exists();
    }
   
    // Open BufferedReaders & PrintWriters given filename. Catch and describe IOExceptions if they occur.
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

    // Close PrintWriters/BufferedReaders, catch and describe IOExceptions when thrown.
    public static boolean close_ofstream(PrintWriter ofstream) {
        boolean closed = false;
        ofstream.close(); 
        closed = true;
        return closed;
    }
    public static boolean close_ifstream(BufferedReader ifstream) {
        boolean closed = false;
        try { 
            ifstream.close(); 
            closed = true;
        }
        catch (IOException e) { 
            System.out.println("Err: IOException during output file close: " + e); 
        }
        return closed;
    }
}

// ======================================================================================
// WordTools
//
//  Word processing procedures.
//
//   - normalize_line(String)
//   - find_word(String, Word[], int)
//
// ====================================================================================== */
class WordTools {
    private WordTools() {}
    
    // Lowercase, trim whitespace, remove back to back hyphens/apostrophes, add separation.
    public static String normalize_line(String line_dirty) {
        String line = line_dirty.trim().toLowerCase();
        
        // Delete back to back hyphens and apostrophes
        String delete_doubles = "'-";
        int index = 0;
        while (index < line.length()-1) {
            char symbol = line.charAt(index);
            // Delete char if followed by duplicate
            if (delete_doubles.indexOf(symbol) > -1 && line.charAt(index+1) == symbol)
                line = line.substring(0, index) + line.substring(index+1);
            // Advance to next char otherwise
            else index++;
        }
        
        // Insert a space if:
        //  a number and letter are next to eachother, 
        //  a hyphen follows a number, 
        //  a hyphen is followed by a non alphanumeric
        for (int i = 0; i < line.length()-1; i++) {
            char at = line.charAt(i);
            char next = line.charAt(i+1);
            if ((Character.isAlphabetic(at) && Character.isDigit(next)) || 
                (Character.isDigit(at) && (Character.isAlphabetic(next) || next == '-')) ||
                (at == '-' && !(Character.isDigit(next) || Character.isAlphabetic(next)))) {

                line = line.substring(0, i+1) + ' ' + line.substring(i+1);
                i++;
            }
        }
        return line;
    }

    // Return index of Word object in words array matching string
    public static int find_word(String word, Word[] words, int words_size) {
        int index = -1;
        for (int i = 0; i < words_size && index < 0; i++)
            if (words[i].get_text().equals(word))
                index = i;
        return index;
    }
}

// ============================================================================
// Word
//
//  Class for words. Stores text string and occurence count.
//  Supports incrementing the count, getting string, and getting count.
//
// ============================================================================
class Word {
    private String word;
    private int count;

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

// ======================================================================================
// IOFileNameState
//  
//  Takes filename strings and evaluates them.
//  Provides functionality to signal and explain why an input/output filename is invalid.
//  Does checks on file names:
//   Was a filename given?
//   Does a corresponding file exist?
//   Are the input and output filenames different?
//  Maintains a 'status' integer, whose bits represent each check.
//   Failed checks result in a positively flipped bit.
//   The nominal state is a status of '0', all checks passed.
//  
//   - set_input(String)set_output(String): set internal filenames
//   - get_input()/get_output(): return internal filenames 
//   - check_input()/check_output(): run checks on internal names
//   - clear_input()/clear_output(): set internal names blank
//   - get_status(): Returns the status code.
//
// ======================================================================================
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
        else if (input_file.equals(output_file)) status |= OUTPUT_IS_INPUT;
    }
    public void check_output() {
        status &= ~(OUTPUT_NOTGIVEN + OUTPUT_DOEXIST + OUTPUT_IS_INPUT);
        if (output_file.isEmpty()) status |= OUTPUT_NOTGIVEN;
        else if (FileTools.file_exists(output_file)) {
            status |= OUTPUT_DOEXIST;
            if (output_file.equals(input_file)) status |= OUTPUT_IS_INPUT; 
        }
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
    private Prompts() {}
    
    private static BufferedReader stdin = null;
   
    // Open/close standard input for prompt input.  
    // Do not call a prompt without opening stdin first.
    public static void open_stdin() {
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    // Only close stdin once.
    public static void close_stdin() {
        try {
            stdin.close();
            stdin = null;
        } catch (IOException e) {
            System.out.println("An IOException occurred during BufferedReader close: " + e);
        }
    }

    // To fix blank filenames.
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

    // To fix a nonexistent input file.
    public static int bad_input(IOFileNameState filenames) {
        int quit_states = 0;
        System.out.println("The input file '" + filenames.get_input() + "' doesn't exist.");
        filenames.set_input(catchLine("Please enter a different one (or nothing to quit): "));
        quit_states = filenames.get_status() & IOFileNameState.INPUT_NOTGIVEN;
        return quit_states;
    }

    // To fix a preexisting output file (multiple options).
    public static int bad_output(IOFileNameState filenames) {
        int quit_states = 0;
        System.out.println("The output file '" + filenames.get_output() + "' already exists.");
        System.out.println("You can\n 1. Pick Another\n 2. Backup & Overwrite\n 3. Quit");
        String choice = catchLine("What would you like to do? (pick/backup/quit): ").toLowerCase();
        boolean choosing = true;
        while (choosing) {
            switch (choice) {
                case "1":
                case "pick":
                    filenames.clear_output(); // Clear output, allow no_output() to handle reentry.
                    break;
                case "2":
                case "replace":
                case "backup":
                    // Simpler to rename original, rather than copying everything only to overwrite.
                    FileTools.rename_file(filenames.get_output(), filenames.get_output()+".bak");
                    filenames.check_output(); // Should clear OUTPUT_DOEXIST
                    break;
                case "3":
                case "":
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

    // To fix an output file that is the input file.
    public static int io_same(IOFileNameState filenames) {
        int quit_states = 0;
        System.out.println("Your input file and output file are the same ('" + filenames.get_input() + "').");
        filenames.set_output(catchLine("Please enter a different output file (or nothing to quit): "));
        quit_states = filenames.get_status() & IOFileNameState.OUTPUT_NOTGIVEN;
        return quit_states;
    }
   
    // Helper for getting input with a prompt. 
    // Prints prompt, reads a line, and catches any IOExceptions. 
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

// ============================================================================
// FileWordData
//
//  Container for data parsed from input files.
//  Holds the sum of integers, array of word tokens, and the parsed word count.
//  Can only store MAX_WORDS words.
//
// ============================================================================
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

// ============================================================================
// FileCopy
//
//  Processes words/integers from input file, prints results to output file.
//  Filenames can be supplied as arguments or prompted during runtime.
//
//   - main(): Processes filenames, opens/closes files, writes to output file.
//   - process_input_file(BufferedReader): Parses FileWordData from ifstream.
//
// ============================================================================ 
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
            if ((fn_status & IOFileNameState.INPUT_NOTGIVEN) != 0)          // Precedence matters, do not reorder.
                fn_quit_states |= Prompts.no_input(filenames);
            else if ((fn_status & IOFileNameState.INPUT_NOEXIST) != 0)
                fn_quit_states |= Prompts.bad_input(filenames);
            else if ((fn_status & IOFileNameState.OUTPUT_NOTGIVEN) != 0)
                fn_quit_states |= Prompts.no_output(filenames);
            else if ((fn_status & IOFileNameState.OUTPUT_IS_INPUT) != 0)
                fn_quit_states |= Prompts.io_same(filenames);
            else if ((fn_status & IOFileNameState.OUTPUT_DOEXIST) != 0)
                fn_quit_states |= Prompts.bad_output(filenames);
            fn_status = filenames.get_status();
        }
        Prompts.close_stdin();

        // Proceed if filenames nominal
        if (fn_status == 0) {
            System.out.println("Opening file streams.");
            BufferedReader inFile = FileTools.open_ifstream(filenames.get_input());
            PrintWriter outFile = FileTools.open_ofstream(filenames.get_output());
            boolean files_ready = inFile != null && outFile != null;

            if (files_ready) {
                // Process words/integer sum from input file.
                System.out.println("Processing input file.");
                FileWordData wordsums = process_input_file(inFile);
                System.out.println("Closing input file.");
                FileTools.close_ifstream(inFile);

                // Write results to output file.
                System.out.println("Writing results to output file.");
                outFile.println("Total Integer Sum: " + wordsums.sum + "\n----------------------------");
                outFile.println("Parsed Words: " + wordsums.word_count + '/' + wordsums.MAX_WORDS + "\n----------------------------");
                for (int i = 0; i < wordsums.word_count; i++) 
                    outFile.println(" * " + wordsums.words[i].get_text() + ": " + wordsums.words[i].get_count());
                System.out.println("Closing output file.");
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
        catch (IOException e) { System.out.println("Error reading first line from input file: " + e); }
        
        String token = null;
        String delims = "\t\n\r\\\"|~`!@#$%^&*()_+=[]{};:/?.>,< ";
        while (line != null) {
            StringTokenizer tokens = new StringTokenizer(WordTools.normalize_line(line), delims); 
            while (tokens.hasMoreTokens()) {
                token = tokens.nextToken();

                // Remove any starting characters which aren't digits, letters, or hyphens.
                while (token.length() > 0 && !Character.isAlphabetic(token.charAt(0)) && !Character.isDigit(token.charAt(0)) && token.charAt(0) != '-')
                    token = token.substring(1);
                // If token isn't empty and isn't just a hyphen.
                if (token.length() > 0 && !(token.length() == 1 && token.charAt(0) == '-')) {
                    // Try to add token to sum.
                    try {
                        wordsums.sum += Integer.parseInt(token);
                    // Otherwise, attempt to add as word.
                    } catch (NumberFormatException e) {
                        int windex = WordTools.find_word(token, wordsums.words, wordsums.word_count);
                        if (windex < 0) {
                            if (wordsums.word_count < wordsums.MAX_WORDS) { // Only add new if space left.
                                wordsums.words[wordsums.word_count] = new Word(token);
                                wordsums.word_count++;
                            }
                        } else 
                            wordsums.words[windex].incriment(); 
                    }
                }
            }
            try { line = inFile.readLine(); } 
            catch (IOException e) { System.out.println("Error reading next line after '" + line + "' from input file: " + e); }
        }   
        return wordsums;
    }
}

