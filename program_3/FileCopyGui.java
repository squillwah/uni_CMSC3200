// For GUI
import java.awt.*;
import java.awt.event.*;
// For testing? Could remove at end, I think.
import java.io.*;
// For sorting file lists alphabetically
import java.util.Arrays;
// For iterating over files within a directory
import java.util.Iterator;
import java.util.NoSuchElementException;

// ================
//  FileEnvironment
//  
//   Represents the current folder.
//   Supports changing directory (up to parent and given as argument).
//   Implements an iterator for traversal of Files within directory.
// 
// ================
class DirMover implements Iterable<File> {
    private File dir;

    public DirMover(File root) {
        dir = root.getAbsoluteFile();
    }

    // Return the current directory File object. 
    public File get_dir() { return dir; }
    
    // Check if at system root.
    public boolean at_root() { return dir.getParent() == null; }

    // Move to up into parent directory. 
    // Returns true if directory changed successfully.
    public boolean cd_up() {
        boolean dir_changed = false;
        if (!at_root()) {
            dir = dir.getParentFile();
            dir_changed = true;
        }
        return dir_changed;
    }

    // Change directory to different directory.
    // Returns true if directory changed successfully.
    public boolean cd(File new_dir) {              // Should this take filename string or file object?
        boolean dir_changed = false;
        if (new_dir != null && new_dir.isDirectory()) {
            dir = new_dir;
            dir_changed = true;
        }
        return dir_changed;
    }

    // Minimal implementation, disallow modification of file array.
    public DirIterator iterator() { return new DirIterator(); }
    private class DirIterator implements Iterator<File> {
        private int pos;
        private File[] files;
        public DirIterator() { 
            pos = 0; 
            files = dir.listFiles();
            // Restricted folders return null, safer to assign empty array.
            if (files == null) files = new File[0];
            else Arrays.sort(files); // Sort files by filename in alphabetical order.
        }
        public boolean hasNext() { return pos < files.length; }
        public File next() { 
            if (!hasNext()) throw new NoSuchElementException("End of directory.");
            return files[pos++];
        }
    }
}

// ===============
// FileCopier
//  
//  File copying utility.
//  Stores source and target Files, and implements the copy procedure.
//  Runs checks on source and target files similar to program 2, though not
//  all checks need be passed for the copy procedure to proceed.
//  Status code describes status of all checks on currently set source/target Files.
//
// ===============
class FileCopier {

    // Codes for possible bad states.
    
    // Prevent a copy, impossible copy.
    public static final int STAT_SOURCE_EMPTY = 0b0000001;
    public static final int STAT_SOURCE_ISDIR = 0b0000010;
    public static final int STAT_TGTDIR_EMPTY = 0b0000100;
    public static final int STAT_TARGET_EMPTY = 0b0001000;
    public static final int STAT_TARGET_ISDIR = 0b0010000;

    // Possible copy error, could allow copy.
    public static final int STAT_TARGET_EXIST = 0b0100000; // Will allow this to bypass with a popup.
    public static final int STAT_TARGET_ISSRC = 0b1000000; // Will allow this to bypass with a popup, consider merging with EXIST.

    private int status;
    private File source_file;
    private File target_file;
    private File target_dir;

    public FileCopier() { 
        source_file = null; 
        target_file = null;
        target_dir = null;
        status = 0 | (STAT_SOURCE_EMPTY | STAT_TGTDIR_EMPTY | STAT_TARGET_EMPTY);
    }

    // Set the source file.
    public void set_source_file(File source) {
        source_file = source;
        status &= ~(STAT_SOURCE_EMPTY | STAT_SOURCE_ISDIR);
        if (source == null || source.getName().isEmpty())
            status |= STAT_SOURCE_EMPTY;
        else {
            if (source.isDirectory())
                status |= STAT_SOURCE_ISDIR;
            if (target_file != null && source_file.getAbsolutePath().equals(target_file.getAbsolutePath())) // Might not need the null check here, or in set_target_file.
                status |= STAT_TARGET_ISSRC;
        }
    }
   
    // Set target file directory. 
    public void set_target_dir(File dir) { 
        target_dir = dir; 
        status &= ~(STAT_TGTDIR_EMPTY);
        if (!dir.isDirectory())
            status |= STAT_TGTDIR_EMPTY;    // We should never reach a state where target_dir isn't a dir, but I'm setting something just in case.
    }
    
    // Set the target file, by filename. 
    public void set_target_file(String filename) { 
        target_file = null;
        status |= STAT_TARGET_EMPTY;
        status &= ~(STAT_TARGET_ISDIR | STAT_TARGET_EXIST | STAT_TARGET_ISSRC);
        if (!filename.isEmpty() && (status & STAT_TGTDIR_EMPTY) == 0) {
            target_file = new File(target_dir.getPath()+'/'+filename);
            status &= ~STAT_TARGET_EMPTY;
            if (target_file.exists()) {
                status |= STAT_TARGET_EXIST;
                if (source_file != null && target_file.getAbsolutePath().equals(source_file.getAbsolutePath()))
                    status |= STAT_TARGET_ISSRC;
                if (target_file.isDirectory())
                    status |= STAT_TARGET_ISDIR;
            }
        }
    }

    // Copies source file to target file.
    // Returns true if copy succeeded, false otherwise (null source/target files)
    public boolean copy() throws IOException {
        boolean copied = false;
        // Copy is impossible if source or target files are null or are directories.
        if ((status & (STAT_SOURCE_EMPTY | STAT_SOURCE_ISDIR | STAT_TARGET_EMPTY | STAT_TARGET_ISDIR)) == 0) {
            BufferedReader ifstream = null;
            PrintWriter ofstream = null;
            ifstream = new BufferedReader(new FileReader(source_file));
            ofstream = new PrintWriter(new FileWriter(target_file));
            String line;
            while ((line = ifstream.readLine()) != null)
                ofstream.println(line);
            ifstream.close();
            ofstream.close();
            copied = true;
        }
        return copied;
    }

    public int get_status() { return status; }
    public File get_source_file() { return source_file; }
    public File get_target_file() { return target_file; }
    public File get_target_dir() { return target_dir; }
}

/*  ignore

    // Status codes for source/target file states.
    public static final int CHCK_SOURCE_NOTGIVEN  = 0b0000001;  // File name is empty
    public static final int CHCK_SOURCE_NOEXIST   = 0b0000010;  // File doesn't exist
    public static final int CHCK_SOURCE_ISDIR     = 0b0000100;  // File refers to a directory
    public static final int CHCK_TARGET_NOTGIVEN  = 0b0001000;
    public static final int CHCK_TARGET_EXISTS    = 0b0010000;
    public static final int CHCK_TARGET_ISDIR     = 0b0100000;
    public static final int CHCK_TARGET_ISSOURCE  = 0b1000000;  // Target/source absolute paths are equal.

    // Error codes for the copy operation.
    public static final int COPY_CHECK_FAIL       = 0b00001;    // 
    public static final int COPY_READ_OPENERR     = 0b00010;
    public static final int COPY_WRITE_OPENERR    = 0b00100;
    public static final int COPY_READLINE_FAIL    = 0b01000;
    public static final int COPY_READ_CLOSEERR    = 0b10000;

    private int status;
    private File source;
    private File target;
    
    public FileCopier() {
        status = 0;
        source = null;
        target = null;
    }

    // Set the source file, given it's filename and directory.
    public void set_source(String filename, FileEnvironment env) {
        source = null;
        if (filename.isEmpty())
            status |= CHCK_SOURCE_NOTGIVEN;
        else {


   
    // Takes two source/target filenames.
    // Compares filenames and runs checks within file environment to determine copy status. 
    // Returns a check status code describing the result of all checks.
    //
    // !!!!!!! @todo: many of these are redundant, will never be triggered, and need to be removed.         maybe not
    //
    public static int check(File source, File target) {
        int check_status = 0;
        if (source == null || source.getName().equals(""))  // No need to compare against null anymore, since get_dirent returns a File always now (even if it doesn't exist).
            check_status |= CHCK_SOURCE_NOTGIVEN;
        else if (!source.exists())
            check_status |= CHCK_SOURCE_NOEXIST;
        else if (source.isDirectory()) 
            check_status |= CHCK_SOURCE_ISDIR;
        if (target == null || target.getName().equals(""))
            check_status |= CHCK_TARGET_NOTGIVEN;
        else {
            if ((check_status & CHCK_SOURCE_NOEXIST) == 0 && target.getAbsolutePath().equals(source.getAbsolutePath())) // Might need to use canonical path if issues occur.
                check_status |= CHCK_TARGET_ISSOURCE;
            if (target.exists()) {
                check_status |= CHCK_TARGET_EXISTS;
                if (target.isDirectory()) 
                    check_status |= CHCK_TARGET_ISDIR;
            }
        }
        
        // decouple FileCopier from FileEnvironment 2/3
        //
        //if (source_fn == null || source_fn.equals(""))      
        //    check_status |= CHCK_SOURCE_NOTGIVEN;
        //else {
        //    File source_file = env.get_dirent(source_fn);
        //    if (source_file == null)                        
        //        check_status |= CHCK_SOURCE_NOEXIST;
        //    else if (source_file.isDirectory())             
        //        check_status |= CHCK_SOURCE_ISDIR;
        //}
        //if (target_fn == null || target_fn.equals(""))      
        //    check_status |= CHCK_TARGET_NOTGIVEN;
        //else {
        //    if (target_fn.equals(source_fn)) 
        //        check_status |= CHCK_TARGET_ISSOURCE;
        //    File target_file = env.get_dirent(target_fn);
        //    if (target_file != null) {
        //        check_status |= CHCK_TARGET_EXISTS;
        //        if (target_file.isDirectory()) 
        //            check_status |= CHCK_TARGET_ISDIR;
        //    }
        //}
        return check_status;
    }

    // Finds source file in file environment, creates target file and copies all lines.
    // First runs a check, will not copy source to target if check doesn't pass.
    // Returns a copy status code describing the success of the copy operation.
    public static int copy(File source, File target) {
        int copy_status = 0;
        BufferedReader ifstream = null;
        PrintWriter ofstream = null;
        try { ifstream = new BufferedReader(new FileReader(source)); }
        catch (IOException e) { copy_status |= COPY_READ_OPENERR; }
        try { ofstream = new PrintWriter(new FileWriter(target)); }
        catch (IOException e) { copy_status |= COPY_WRITE_OPENERR; }
        if (copy_status == 0) {
            try {
                String line;
                while ((line = ifstream.readLine()) != null)
                    ofstream.println(line);
            } catch (IOException e) { copy_status |= COPY_READLINE_FAIL; }
            try { ifstream.close(); }
            catch (IOException e) { copy_status |= COPY_READ_CLOSEERR; }
            ofstream.close();
        } 
        return copy_status;
        

        // decouple FileCopier from FileEnvironment 3/3
        //
        //if (check(source_fn, target_fn, env) == 0) {
        //    BufferedReader source = env.open_ifstream((env.get_dirent(source_fn))); // Are we sure this should take a File, not another filename? Does open possibility of copying from outside directory into current, which could be good or bad.
        //    PrintWriter target = env.open_ofstream(target_fn);
        //    if (source == null) copy_status |= COPY_READ_OPENERR;
        //    if (target == null) copy_status |= COPY_WRITE_OPENERR;
        //    if (copy_status == 0) 
        //        try {
        //            String line;
        //            while ((line = source.readLine()) != null) 
        //                target.println(line);
        //        } catch (IOException e) { copy_status |= COPY_READLINE_FAIL; }
        //    target.close();
        //    try { source.close(); } // Do we need to check this exception? Why doesn't closing PrintWriter throw anything?
        //    catch (IOException e) { copy_status |= COPY_READ_CLOSEERR; }
        //} else
        //    copy_status |= COPY_CHECK_FAIL;
        //return copy_status;
    }
}*/

public class FileCopyGui {
    public static void main(String[] args) throws IOException {
        // Create file environment
        File root = null; 
        switch (args.length) {
            case 0:
                root = new File("");
                break;
            default:
                root = new File(args[0]);
                if (!root.isDirectory()) root = new File("");
                break;
        }
        DirMover files = new DirMover(root); 

        FileCopier copier = new FileCopier();

        // Simple test
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;
        while (running) {
            System.out.println("\nd: show files, u: move up, 'dirname': move into, q: quit, s: set source, t: set target, c: try a copy");
            String input = stdin.readLine();
            System.out.println();
            switch (input) {
                case "d":
                    break;
                case "u":
                    files.cd_up();
                    break;
                case "q":
                    running = false;
                    continue;
                case "s":
                    String fn = stdin.readLine();
                    for (File file : files) {
                        if (file.getName().equals(fn)) {
                            copier.set_source_file(file);
                            break;
                        }
                    }
                    break;
                case "t":
                    copier.set_target_dir(files.get_dir());
                    copier.set_target_file(stdin.readLine());
                    break;
                case "c":
                    System.out.println("copy success: " + copier.copy());
                    break;
                default:
                    File moveinto = null;
                    for (File file : files) {
                        if (file.getName().equals(input)) {
                            moveinto = file;
                            break;
                        }
                    }
                    if (!files.cd(moveinto)) {
                        System.out.println("dir '" + input + "' does not exist.\n");
                        continue;
                    }
                    break;
            }
            // Display, now with iterator instead of method.
            System.out.println(files.get_dir().getPath());
            for (File ent : files) {
                System.out.print(" * " + ent.getPath());
                if (ent.isDirectory() && ent.list() != null && ent.list().length > 0) { System.out.println(" +"); }
                else { System.out.println(); }
            }

            // Check the file copier

            System.out.println("source: " + copier.get_source_file());
            System.out.println("target: " + copier.get_target_file());
            System.out.println("target_dir: " + copier.get_target_dir());

            int status = copier.get_status();
            System.out.println("status: " + status);
            System.out.println(" STAT_SOURCE_EMPTY = " + (status & FileCopier.STAT_SOURCE_EMPTY));
            System.out.println(" STAT_SOURCE_ISDIR = " + (status & FileCopier.STAT_SOURCE_ISDIR));
            System.out.println(" STAT_TGTDIR_EMPTY = " + (status & FileCopier.STAT_TGTDIR_EMPTY));
            System.out.println(" STAT_TARGET_EMPTY = " + (status & FileCopier.STAT_TARGET_EMPTY));
            System.out.println(" STAT_TARGET_ISDIR = " + (status & FileCopier.STAT_TARGET_ISDIR));

            System.out.println("\n STAT_TARGET_EXIST = " + (status & FileCopier.STAT_TARGET_EXIST));
            System.out.println(" STAT_TARGET_ISSRC = "  + (status & FileCopier.STAT_TARGET_ISSRC));

            //String source = stdin.readLine();
            //String target = stdin.readLine();
            //int status = FileCopier.check(files.get_dirent(source), files.get_dirent(target));
            //System.out.println(status);
            //if (status == 0) {
            //    System.out.println("Copying contents of '" + source + "' to '" + target + "'.");
            //    System.out.println(FileCopier.copy(files.get_dirent(source), files.get_dirent(target)));
            //} else {
                //if ((status & FileCopier.CHCK_SOURCE_NOTGIVEN) > 0)  System.out.println("src not given");
                //if ((status & FileCopier.CHCK_SOURCE_NOEXIST) > 0)   System.out.println("src no exist");
                //if ((status & FileCopier.CHCK_SOURCE_ISDIR) > 0)     System.out.println("src is dir");
                //if ((status & FileCopier.CHCK_TARGET_NOTGIVEN) > 0)  System.out.println("tgt not given");
                //if ((status & FileCopier.CHCK_TARGET_EXISTS) > 0)    System.out.println("tgt already exists");
                //if ((status & FileCopier.CHCK_TARGET_ISSOURCE) > 0)  System.out.println("tgt is source");
                //if ((status & FileCopier.CHCK_TARGET_ISDIR) > 0)     System.out.println("tgt is dir");
            //}
        }

        stdin.close();

        // Note
        //
        //  The entire FileEnvironment/FileCopier structure needs a rework.
        //
        //  FileEnvironment represents the location, and merely provides functionality to 
        //  change that location and grab information about the files/dirs within it.
        //
        //  FileCopier should copy contents of source files into target files.
        //  It should support a system to make checks on those files like program 2. Though,
        //  the raw copy operation should be seperate from the checks and allow for overwrite.
        //
        //  The GUI should allow the user to pick source and target files by filename, irrespective
        //  of the current FileEnvironment (be able to copy trans directory). We need to be able to
        //  "lock" a source File (found using FileEnvironment) and "lock" a target File (whose location 
        //  was also selected in part using FileEnvironment). These source/target Files shouldn't be a 
        //  part of the FileEnvironment class, though it may make sense to turn FileCopier into an 
        //  instantable class which can hold a state (which then might include the source/target Files).

        //Window w = new Window();
    }
}

//  handles all GUI init and updates
class Window extends Frame implements WindowListener {

    private static final long serialVersionUID = 1L;
    Label title = new Label("Test");

    public Window() {
        
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagLayout gbl = new GridBagLayout();

        double colWeight[] = {1};
        double rowWeight[] = {1};
        int colWidth[] = {1};
        int rowHeight[] = {1};

        gbl.rowHeights = rowHeight;
        gbl.columnWidths = colWidth;
        gbl.rowWeights = rowWeight;
        gbl.columnWeights = colWeight;

        this.setBounds(20,20,200,100);
        this.setLayout(gbl);
        gbl.setConstraints(title, gbc);
        this.add(title);
        this.addWindowListener(this);
        this.setVisible(true);

    }

    //  window listeners
    public void windowClosing(WindowEvent e) {

        this.removeWindowListener(this);
        this.dispose();
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
}
