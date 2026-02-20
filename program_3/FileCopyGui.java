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

// Might benefit from a stripped down representation of a dirent like this (for list printing?):
// But then why not just provide the full File object?
class DirEntInfo {
    public String path;
    public boolean is_dir;
}

// ================
//  FileEnvironment
//  
//   Represents the current folder.
//   Holds root directory File and array of Files contained within.
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

    // Return the file object in the current directory by relative name.
    public File get_file(String filename) {
        File desired = null;
        for (int i = 0; i < dirents.length && desired == null; i++) 
            if (dirents[i].getName().equals(filename))
                desired = dirents[i];
        return desired;
    }

    // Return the present path of the file environment.
    public String get_path() { return rootdir.getPath(); }

    /*                      May just want to do this with a PrintWriter. Or maybe not.
    // Create a file 
    public boolean make_file(String filename) {

    }
    */

    // Check if at file system root.
    public boolean at_root() { return rootdir.getParent() == null; }
   
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

    // Move the file environment down into a subdirectory.
    public boolean cd_down(String filename) {              // Should this take filename string or file object?
        boolean dir_changed = false;
        File new_dir = get_file(filename);
        if (new_dir != null && new_dir.isDirectory()) {
            rootdir = new_dir;
            refresh_dirents();
            dir_changed = true;
        }
        return dir_changed;
    }

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
// CopyFileStatus
//
//  Runs checks between the target/source file object and returns a code
//  describing any issues which should preventing prevent the file copy.
//
// ===============
class CopyFileStatus {
    private CopyFileStatus() {}
     
    public static final int SOURCE_NOTGIVEN  = 0b00001;
    public static final int SOURCE_NOEXIST   = 0b00010;
    public static final int TARGET_NOTGIVEN  = 0b00100;
    public static final int TARGET_DOEXIST   = 0b01000;
    public static final int TARGET_IS_SOURCE = 0b10000;
    
    public static int check(File source, File target) {
        int status = 0;
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
