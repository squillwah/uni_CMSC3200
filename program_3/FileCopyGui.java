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
//   Holds root directory File and array of Files contained within.
//   Implements an iterator for traversal of Files within directory.
// 
// ================
class FileEnvironment implements Iterable<File> {
    private File rootdir;
    private File[] dirents;

    public FileEnvironment(File root) {
        rootdir = root.getAbsoluteFile();
        refresh_dirents();
    }

    // ---------------
    // Checks and gets
    // ---------------
    
    // Return the present path of the file environment.
    public String get_path() { return rootdir.getPath()+'/'; } // !!! This slash might cause issues on windows. Added because of open_ofstream opening files in the dir above.
    
    // Check if at system root.
    public boolean at_root() { return rootdir.getParent() == null; }

/*  Is any of this really needed if get_dirent returns null & File.exists() exists?
 *
 *  // Check if entry exists within directory, given filename string or file object.
 *  public boolean dirent_exists(String filename) { 
 *      File dirent = get_dirent(filename);
 *      return !(dirent == null) && dirent_exists(dirent); 
 *  }
 *  public boolean dirent_exists(File file) { return file.exists(); }
 */
   
//    // Find and return first dirent with matching filename. 
//    // Returns nonexistent File if not found.
    // Returns a File object with the given relative name within the present FileEnvironment path.
    public File get_dirent(String filename) {
//        File dirent = null;
//        for (int i = 0; i < dirents.length && dirent == null; i++) 
//            if (dirents[i].getName().equals(filename))
//                dirent = dirents[i];
//        return dirent;
        return new File(get_path()+filename);
    }

    // ---------------------------
    // Modify the file environment
    // ---------------------------

    // Move the file environment up a directory. 
    // Returns true if directory changed successfully.
    public boolean cd_up() {
        boolean dir_changed = false;
        if (!at_root()) {
            rootdir = rootdir.getParentFile();
            refresh_dirents();
            dir_changed = true;
        }
        return dir_changed;
    }

    // Move the file environment into a subdirectory, given its name.
    // Returns true if directory changed successfully.
    public boolean cd_down(String filename) {              // Should this take filename string or file object?
        boolean dir_changed = false;
        File new_dir = get_dirent(filename);
        if (new_dir != null && new_dir.isDirectory()) {
            rootdir = new_dir;
            refresh_dirents();
            dir_changed = true;
        }
        return dir_changed;
    }

    // Refresh the file list.
    private void refresh_dirents() {
        dirents = rootdir.listFiles();
        // Restricted folders return null
        if (dirents == null) dirents = new File[0]; // Safer to assign empty array.
        else Arrays.sort(dirents);
    }

    // --------------------------------------
    // File operations within the environment
    // --------------------------------------

//      decouple FileCopier from FileEnvironment 1/3
//
//    // Find file by filename within current directory, rename to new filename.
//    // Returns true if file renamed successfully. Will not rename directories, or overwrite files.
//    public boolean rename_file(String original_fn, String new_fn) {
//        boolean renamed = false;
//        File file = get_dirent(original_fn);
//        if (file != null && !file.isDirectory() && get_dirent(new_fn) == null)
//            file.renameTo(new File(new_fn));
//        refresh_dirents();  // Might not be needed.
//        return renamed;
//    } // !! Just realized that the ruberic doesn't actually ask for this.
//
//    // Open a BufferedReader for a file.
//    // Returns null if it cannot be opened (any IOException).
//    public static BufferedReader open_ifstream(File file) {
//        BufferedReader ifstream = null;
//        try { ifstream = new BufferedReader(new FileReader(file.getPath())); }
//        catch (IOException e) {} // Do nothing, allow caller to handle the null. 
//        return ifstream;
//    }
//
//    // alternatively, this restricts opening files to current directory and matches usage with open_ofstream
//    //    public static BufferedReader open_ifstream(String filename) {
//    //        BufferedReader ifstream = null;
//    //        try { ifstream = new BufferedReader(new FileReader(get_path()+filename)); }
//    //        catch (IOException e) {} // Do nothing, allow caller to handle the null. 
//    //        return ifstream;
//    //    }
//
//    // Open a PrintWriter to a file in the current directory, given relative filename.
//    // Will create the file if no matching file exists, or overwrite if one does.
//    // Returns null if the PrintWriter cannot be opened (any IOException).
//    public PrintWriter open_ofstream(String filename) {
//        PrintWriter ofstream = null;
//        try { ofstream = new PrintWriter(new FileWriter(get_path()+filename)); } // This caused issues since get_path() didn't add the trailing slash. Might be better to use a method on the root File instead to create a new file first and then write into that.
//        catch (IOException e) {} // Could add flag or dump exception to a log file if this becomes inconvenient.
//        return ofstream;
//    }

    // -------------
    // File iterator
    // -------------

    // Minimal implementation, disallow modification of dirent array.
    public FileEnvIterator iterator() { return new FileEnvIterator(); }
    private class FileEnvIterator implements Iterator<File> {
        private int pos;
        public FileEnvIterator() { 
            refresh_dirents(); // Is it really a good idea to update the dirents here? Added because new files (copied files) don't show up, obviously.
            pos = 0; 
        }
        
        public boolean hasNext() { return pos < dirents.length; }
        public File next() { 
            if (!hasNext()) throw new NoSuchElementException("End of directory.");
            return dirents[pos++];
        }
    }
}

// ===============
// FileCopier
//  
//  File copying utility.
//  Creates copies of files given a source File object and target File object.
//  Runs checks between target/source files and returns status code. 
//  Will not copy files unless all checks pass.
//
// ===============
class FileCopier {
    private FileCopier() {}
     
    public static final int CHCK_SOURCE_NOTGIVEN  = 0b0000001;
    public static final int CHCK_SOURCE_NOEXIST   = 0b0000010;
    public static final int CHCK_SOURCE_ISDIR     = 0b0000100;
    public static final int CHCK_TARGET_NOTGIVEN  = 0b0001000;
    public static final int CHCK_TARGET_EXISTS    = 0b0010000;
    public static final int CHCK_TARGET_ISSOURCE  = 0b0100000;  // Ruberic said this should be allowed? It's already implemented, we should email Pyzdrowski. If we must disable it, just turning this const to a zero might work. Not sure if I checked this anywhere else.
    public static final int CHCK_TARGET_ISDIR     = 0b1000000;

    public static final int COPY_CHECK_FAIL       = 0b00001;
    public static final int COPY_READ_OPENERR     = 0b00010;
    public static final int COPY_WRITE_OPENERR    = 0b00100;
    public static final int COPY_READLINE_FAIL    = 0b01000;
    public static final int COPY_READ_CLOSEERR    = 0b10000;
   
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
}

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
        FileEnvironment files = new FileEnvironment(root); 

        // Simple test
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;
        while (running) {
            System.out.println("\nd: show files, u: move up, 'dirname': move into, q: quit");
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
                default:
                    if (!files.cd_down(input)) {
                        System.out.println("dir '" + input + "' does not exist.\n");
                        continue;
                    }
                    break;
            }
            // Display, now with iterator instead of method.
            System.out.println(files.get_path());
            for (File ent : files) {
                System.out.print(" * " + ent.getPath());
                if (ent.isDirectory() && ent.list() != null && ent.list().length > 0) { System.out.println(" +"); }
                else { System.out.println(); }
            }

            // Check the file copier
            String source = stdin.readLine();
            String target = stdin.readLine();
            int status = FileCopier.check(files.get_dirent(source), files.get_dirent(target));
            System.out.println(status);
            if (status == 0) {
                System.out.println("Copying contents of '" + source + "' to '" + target + "'.");
                System.out.println(FileCopier.copy(files.get_dirent(source), files.get_dirent(target)));
            } else {
                if ((status & FileCopier.CHCK_SOURCE_NOTGIVEN) > 0)  System.out.println("src not given");
                if ((status & FileCopier.CHCK_SOURCE_NOEXIST) > 0)   System.out.println("src no exist");
                if ((status & FileCopier.CHCK_SOURCE_ISDIR) > 0)     System.out.println("src is dir");
                if ((status & FileCopier.CHCK_TARGET_NOTGIVEN) > 0)  System.out.println("tgt not given");
                if ((status & FileCopier.CHCK_TARGET_EXISTS) > 0)    System.out.println("tgt already exists");
                if ((status & FileCopier.CHCK_TARGET_ISSOURCE) > 0)  System.out.println("tgt is source");
                if ((status & FileCopier.CHCK_TARGET_ISDIR) > 0)     System.out.println("tgt is dir");
            }
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
