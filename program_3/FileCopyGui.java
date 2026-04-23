
// [CMSC3200] Technical Computing Using Java
// Program 3: GUI FileCopy
//
//  GUI FileCopy and browser program.
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu


// For GUI
import java.awt.*;
import java.awt.event.*;
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

    public File get_file(String filename) {
        File file = null;
        Iterator<File> it = iterator();
        while (it.hasNext()) {
            File search = it.next();
            if (search.getName().equals(filename)) 
                file = search;
        }
        return file;
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
    public static final int STAT_TARGET_ISSRC = 0b1000000;

    // Possible copy error, could allow copy.
    public static final int STAT_TARGET_EXIST = 0b0100000; // Will allow this to bypass with a popup.

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
        else if (target_file != null) {
            // Update target_file to this directory
            set_target_name(get_target_file().getName());
        }
    }
    
    // Set the target filenamee. 
    public void set_target_name(String filename) { 
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

    // Set target directory and filename by full path
    public void set_target_path(String path) {
        int lastslash = path.lastIndexOf('/');
        if (lastslash == -1) lastslash = path.lastIndexOf('\\');
        if (lastslash == -1) {
            set_target_dir(new File("/"));
            set_target_name(path);
            status |= STAT_TGTDIR_EMPTY;
        } else {
            set_target_dir(new File(path.substring(0,lastslash)));
            set_target_name(path.substring(lastslash+1));
        }
    }

    // Copies source file to target file.
    // Returns true if copy succeeded, false otherwise (null source/target files)
    public boolean copy() throws IOException {
        boolean copied = false;
        // Copy is impossible if source or target files are null or are directories, or if target is source.
        if ((status & (STAT_SOURCE_EMPTY | STAT_SOURCE_ISDIR | STAT_TARGET_EMPTY | STAT_TARGET_ISDIR | STAT_TARGET_ISSRC)) == 0) {
            BufferedReader ifstream = null;
            PrintWriter ofstream = null;
            ifstream = new BufferedReader(new FileReader(source_file));
            ofstream = new PrintWriter(new FileWriter(target_file));
            String line;
            while ((line = ifstream.readLine()) != null)
                ofstream.println(line);
            ifstream.close();
            ofstream.close();
            status |= STAT_TARGET_EXIST;    // Target file now definitely exists.
            copied = true;
        }
        return copied;
    }

    public int get_status() { return status; }
    public File get_source_file() { return source_file; }
    public File get_target_file() { return target_file; }
    public File get_target_dir() { return target_dir; }

    public void clear_source_file() { 
        source_file = null;
        status |= STAT_SOURCE_EMPTY;
        status &= ~STAT_SOURCE_ISDIR;
    }
    public void clear_target_file() {
        target_file = null;
        status |= STAT_TARGET_EMPTY;
        status &= ~(STAT_TARGET_ISDIR | STAT_TARGET_ISSRC | STAT_TARGET_EXIST);
    }
    public void clear_target_dir() {
        target_dir = null;
        status |= STAT_TGTDIR_EMPTY;
    }
}

public class FileCopyGui extends Frame implements WindowListener, ActionListener{
    // File and directory management tools. 
    private static DirMover files;
    private static FileCopier copier;
    //  layout
    private static GridBagConstraints gbc = new GridBagConstraints();
    private static GridBagLayout gbl = new GridBagLayout();
    //  buttons
    private static Button target;
    private static Button confirm;
    //  labels
    private static Label source;
    private static Label currSource;
    private static Label currTarget;
    private static Label fileName;
    private static Label status_label;
    //  list 
    private static List fileList;
    //  textFild
    private static TextField copyTo;

    // False in source select mode, true in target select.
    private static boolean target_mode;

    public static void main(String[] args) throws IOException {
        // Initialize DirMover to execution directory or first argument.
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
        files = new DirMover(root); 
        
        // Instantiate FileCopier utility.
        copier = new FileCopier();

        // Create the window.
        new FileCopyGui();
    }

    public FileCopyGui() {
        //  establishing how buttons and labels go onto the screen
        double colWeight[] = {2,4,4,15,1,3,1};   //  MESSING WITH THESE, DONT HAVE AN
        double rowWeight[] = {25,1,1,1,1 };   //  INTUITIVE FEEL FOR EM
        int colWidth[] = {2,4,4,15,1,3,1};
        int rowHeight[] = {25,1,1,1};

        gbl.rowHeights = rowHeight;
        gbl.columnWidths = colWidth;
        gbl.rowWeights = rowWeight;
        gbl.columnWeights = colWeight;

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;

        //  setting up frame settings
        this.setBounds(20,20,1000,500);
        this.setLayout(gbl);
        this.addWindowListener(this);
        this.setLocationRelativeTo(null);   //  setting starting pos to center screen, likes to start on my left monitor and the fix isnt universal so did this
        this.setTitle("ERROR: title not specified!");          
        initFrame();
        this.setVisible(true);

        // Start in source select mode.
        target_mode = false;
    }

    // Refresh every element of the window with present state of files and copier.
    public void update_window() {
        set_errmsg("");
        updateList();
        updateTitle(files.get_dir().getPath());
        
        // Bottom status/error message & target/source paths
        if (!target_mode) {
            if (copier.get_source_file() != null)
                currSource.setText(copier.get_source_file().getPath());

            if ((copier.get_status() & FileCopier.STAT_SOURCE_EMPTY) != 0)
                set_errmsg("Please select a source file") ;
            if ((copier.get_status() & FileCopier.STAT_SOURCE_ISDIR) != 0)
                set_errmsg("Err: source is directory");
        } else {
            if (copier.get_target_file() != null) {
                copyTo.setText(copier.get_target_file().getPath());
                currTarget.setText(copier.get_target_file().getPath());
            } else 
                copyTo.setText(copier.get_target_dir().getPath());
           
            confirm.setEnabled(true); 
            if ((copier.get_status() & FileCopier.STAT_TARGET_EXIST) != 0) 
                set_errmsg("Warn: target exists, will be overwritten.");
            
            if ((copier.get_status() & FileCopier.STAT_TGTDIR_EMPTY) != 0) {
                set_errmsg("Err: No directory for target file set");
                confirm.setEnabled(false); 
            } 
            if ((copier.get_status() & FileCopier.STAT_TARGET_EMPTY) != 0) {
                set_errmsg("Please select a target file");
                confirm.setEnabled(false); 
            }
            if ((copier.get_status() & FileCopier.STAT_TARGET_ISDIR) != 0) {
                set_errmsg("Err: target is a directory");
                confirm.setEnabled(false); 
            }
            if ((copier.get_status() & FileCopier.STAT_TARGET_ISSRC) != 0) {
                set_errmsg("Err: target cannot be source");
                confirm.setEnabled(false); 
            }
        }
    }

    //  window listeners
    public void windowClosing(WindowEvent e) {
        fileList.removeActionListener(this);
        target.removeActionListener(this);
        confirm.removeActionListener(this);
        copyTo.removeActionListener(this);
        this.removeWindowListener(this);
        this.dispose();
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource() == fileList) {
            String filename = fileList.getSelectedItem();
            if (filename.endsWith(" +")) filename = filename.substring(0,filename.length()-2); 
            File selected_file = files.get_file(filename);

            if (filename.equals("..")) {
                // Move up
                files.cd_up();
                if (target_mode) copier.set_target_dir(files.get_dir());
                update_window();
            } else if (selected_file == null) {
                // Error
                set_errmsg("Selected file does not exist within directory...");
                update_window();
            } else if (selected_file.isDirectory()) {
                // Move down
                files.cd(selected_file);
                if (target_mode) copier.set_target_dir(files.get_dir());
                update_window();
            } else {
                if (target_mode) {
                    copier.set_target_dir(files.get_dir());
                    copier.set_target_name(filename);
                    // Enable confirmable copy if no bad states except allowed target overwrite
                    if ((copier.get_status() & ~FileCopier.STAT_TARGET_EXIST) == 0)
                        confirm.setEnabled(true);
                    else
                        confirm.setEnabled(false);
                    update_window();
                } else {
                    copier.set_source_file(selected_file); // This one is a File.
                    if ((copier.get_status() & FileCopier.STAT_SOURCE_EMPTY) != 0) {
                        target.setEnabled(false);
                        set_errmsg("No source file selected"); // Can we even trigger these states in the GUI?
                    } else if ((copier.get_status() & FileCopier.STAT_SOURCE_ISDIR) != 0) {
                        target.setEnabled(false);
                        set_errmsg("Source file can't be a directory.");
                    } else
                        target.setEnabled(true); // Valid source file, enable target button.
                    update_window();
                }
            }
        } else if (e.getSource() == target) {
            // Only switch to target mode if source file isn't empty and not a directory
            if ((copier.get_status() & (FileCopier.STAT_SOURCE_EMPTY | FileCopier.STAT_SOURCE_ISDIR)) == 0) {
                confirm.setEnabled(true);
                target.setEnabled(false); // Are we going to allow switching out of target mode? (to allow selecting a different source)
                target_mode = true;
                copier.set_target_dir(files.get_dir());
            }
            update_window();
        } else if (e.getSource() == confirm) {
            // Copy and reset everything, if not in bad state
            if ((copier.get_status() & ~FileCopier.STAT_TARGET_EXIST) == 0) {
                if (copier.get_status() > 0) 
                    set_errmsg("Overwriting '" + copier.get_target_file().getName() + "'.");
                try { copier.copy(); }
                catch (IOException ioe) { set_errmsg("IOException occured during copy."); }
                copier.clear_source_file();
                copier.clear_target_file();
                copier.clear_target_dir();
                currSource.setText("");
                currTarget.setText("");
                target_mode = false;
                confirm.setEnabled(false);
                update_window();
            }
        } else if (e.getSource() == copyTo) {
            // Set target path with text box
            System.out.println(copyTo.getText());
            copier.set_target_path(copyTo.getText());
            update_window();
        }
    }
    public void initFrame() {

        //  title
        this.setTitle(files.get_dir().getPath());            

        //  buttons
        target = new Button("Target: ");
        target.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbl.setConstraints(target, gbc);
        this.add(target);

        confirm = new Button("Confirm");
        confirm.addActionListener(this);
        gbc.gridx = 6;
        gbc.gridy = 3;
        confirm.setEnabled(false);
        gbl.setConstraints(confirm, gbc);
        this.add(confirm);

        //  labels
        source = new Label("Source: ");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(source, gbc);
        this.add(source);

        currSource = new Label(" ");
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbl.setConstraints(currSource, gbc);
        this.add(currSource);

        currTarget = new Label(" ");
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbl.setConstraints(currTarget, gbc);
        this.add(currTarget);

        fileName = new Label("File Name: ");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbl.setConstraints(fileName, gbc);
        this.add(fileName);

        //  list
        fileList = new List();
        fileList.setSize(400,600);
        gbc.gridwidth = 8;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(fileList, gbc);
        this.add(fileList);
        fileList.addActionListener(this);
        fileList.add("...");
        fileList.add("Test1");
        fileList.add("Test2");

        //  textfield
        copyTo = new TextField();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbl.setConstraints(copyTo, gbc);
        copyTo.addActionListener(this);
        this.add(copyTo);

        status_label = new Label();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbl.setConstraints(status_label, gbc);
        this.add(status_label);

        target.setEnabled(false);
        confirm.setEnabled(false);
        
        update_window();
    }

    //  update the window to display correctly from backend
    public void updateTitle(String s) {
        this.setTitle(s);
    }

    public void updateTarget(String s) {
        currTarget.setText(s);
    }

    public void updateSource(String s) {
        currSource.setText(s);
    }

    public void updateList() {
        fileList.removeAll();
        if (!files.at_root()) fileList.add("..");

        for (File file : files) {
            if (file.isDirectory() && file.list() != null && file.list().length > 0)
                fileList.add(file.getName() + " +");
            else
                fileList.add(file.getName());
        }
    }

    public static void set_errmsg(String msg) {
        status_label.setText(msg);
    }
}
