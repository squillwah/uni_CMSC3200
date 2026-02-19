import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;


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
class FileEnvironment {
    private File rootdir;
    private File[] dirents;

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
        Arrays.sort(dirents);
    }

    public void display_dirents() {     // Will want to replace this with something else to work with GUI. 
                                        // Or maybe abandon the concept of keeping dirents private in the first place.
        System.out.println(rootdir.getPath());
        for (File ent : dirents) {
            System.out.print(" * " + ent.getPath());
            if (ent.isDirectory()) { System.out.println(" +"); }
            else { System.out.println(); }
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
                    files.display_dirents();    // May want to do this differently. Just hand over the file list instead of obfuscating.
                    break;
                case "u":
                    files.cd_up();
                    files.display_dirents();
                    break;
                case "q":
                    running = false;
                    break;
                default:
                    if (!files.cd_down(input))
                        System.out.println("dir '" + input + "' does not exist.\n");
                    files.display_dirents();
                    break;
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
