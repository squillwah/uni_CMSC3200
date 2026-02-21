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
    private int iterator_pos;

    public FileEnvironment(File root) {
        rootdir = root.getAbsoluteFile();
        refresh_dirents();
    }
    
    // Return the present path of the file environment.
    public String get_path() { return rootdir.getPath(); }
    
    // Check if at file system root.
    public boolean at_root() { return rootdir.getParent() == null; }

    // Check if directory entry exists within directory, given name.
    public boolean dirent_exists(String filename) { return !(get_dirent(filename) == null); }
   
    // Find and return first dirent with matching filename. 
    public File get_dirent(String filename) {
        File dirent = null;
        for (int i = 0; i < dirents.length && dirent == null; i++) 
            if (dirents[i].getName().equals(filename))
                dirent = dirents[i];
        return dirent;
    }

    // Move the file environment up a directory. 
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

    // Minimal iterator implementation.
    public FileEnvIterator iterator() { return new FileEnvIterator(); }
    private class FileEnvIterator implements Iterator<File> {
        private int pos;
        public FileEnvIterator() { pos = 0; }
        
        public boolean hasNext() { return pos < dirents.length; }
        public File next() { 
            if (!hasNext()) throw new NoSuchElementException("End of directory.");
            return dirents[pos++];
        }
    }
}

// ===============
// CopyStatus
//
//  Runs checks between target/source files and returns status code.
//  Code describes any issues which should prevent a source->target file copy.
//
// ===============
class CopyStatus {
    private CopyStatus() {}
     
    public static final int SOURCE_NOTGIVEN  = 0b000001;
    public static final int SOURCE_NOEXIST   = 0b000010;
    public static final int SOURCE_ISDIR     = 0b000100;
    public static final int TARGET_NOTGIVEN  = 0b001000;
    public static final int TARGET_DOEXIST   = 0b010000;
    public static final int TARGET_IS_SOURCE = 0b100000;
    // should we check if target is directory too? probably, if we support target backup like program 2.
   
    // Takes two source/target filename Strings and a FileEnvironment object.
    // Compares filenames and runs checks within file environment to determine copy status. 
    public static int check(String source_fn, String target_fn, FileEnvironment env) {
        int status = 0;
        if (source_fn == null || source_fn.equals(""))      status |= SOURCE_NOTGIVEN;
        else {
            File source_file = env.get_dirent(source_fn);
            if (source_file == null)                        status |= SOURCE_NOEXIST;
            else if (source_file.isDirectory())             status |= SOURCE_ISDIR;
        }
        if (target_fn == null || target_fn.equals(""))      status |= TARGET_NOTGIVEN;
        else {
            if (env.dirent_exists(target_fn))               status |= TARGET_DOEXIST;
            if (target_fn.equals(source_fn))                status |= TARGET_IS_SOURCE;
        }
        return status;
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
                if (ent.isDirectory()) { System.out.println(" +"); }
                else { System.out.println(); }
            }

            String source = stdin.readLine();
            String target = stdin.readLine();
            int status = CopyStatus.check(source, target, files);
            System.out.println(status);
    
            if ((status & CopyStatus.SOURCE_NOTGIVEN) > 0)  System.out.println("src not given");
            if ((status & CopyStatus.SOURCE_NOEXIST) > 0)   System.out.println("src no exist");
            if ((status & CopyStatus.SOURCE_ISDIR) > 0)     System.out.println("src is dir");
            if ((status & CopyStatus.TARGET_NOTGIVEN) > 0)  System.out.println("tgt not given");
            if ((status & CopyStatus.TARGET_DOEXIST) > 0)   System.out.println("tgt already exists");
            if ((status & CopyStatus.TARGET_IS_SOURCE) > 0) System.out.println("tgt is source");
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
