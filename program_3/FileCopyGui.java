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
//  DirMover
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
        }

        stdin.close();

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
