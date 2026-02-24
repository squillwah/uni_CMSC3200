import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class FileCopyGui {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        Window w = new Window();
    }
}

//  handles all GUI init and updates
class Window extends Frame implements WindowListener, ActionListener {

    //  layout
    private GridBagConstraints gbc = new GridBagConstraints();
    private GridBagLayout gbl = new GridBagLayout();

    //  buttons
    private Button target;
    private Button confirm;

    //  labels
    private Label source;
    private Label currSource;
    private Label currTarget;
    private Label fileName;

    // list 
    private List fileList;



    public Window() {

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
        this.setBounds(20,20,500,500);
        this.setLayout(gbl);
        this.addWindowListener(this);
        this.setLocationRelativeTo(null);   //  setting starting pos to center screen, likes to start on my left monitor and the fix isnt universal so did this
        this.setTitle("ERROR: title not specified!");          
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

    //  init window when opened
    public void windowOpened(WindowEvent e) {

        initFrame();
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void initFrame() {

        //  title
        //this.setTitle(get_dir().getPath());                   UNCOMMENT WHEN MERGED WITH MAIN

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
        gbl.setConstraints(confirm, gbc);
        this.add(confirm);

        //  labels
        source = new Label("Source: ");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(source, gbc);
        this.add(source);

        currSource = new Label("Source not specified");
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbl.setConstraints(currSource, gbc);
        this.add(currSource);

        currTarget = new Label("Target not specified");
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

    public void updateList(File currDir) {
        //  depending on backend format might just have to update, or make this an itteritive loop
        fileList.removeAll();

        File[] files = currDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                fileList.add(files[i].getName());
            }
        }
    }
}