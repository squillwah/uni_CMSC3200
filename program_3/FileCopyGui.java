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
    GridBagConstraints gbc = new GridBagConstraints();
    GridBagLayout gbl = new GridBagLayout();

    //  buttons
    Button target;
    Button confirm;

    //  labels
    Label source;
    Label currSource;
    Label currTarget;
    Label fileName;

    public Window() {

        //  establishing how buttons and labels go onto the screen
        double colWeight[] = {1,10,1};   //  MESSING WITH THESE, DONT HAVE AN
        double rowWeight[] = {12,1,1,1};   //  INTUITIVE FEEL FOR EM
        int colWidth[] = {1,10,1};
        int rowHeight[] = {12,1,1,1};

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

    public void initFrame() {

        //  buttons
        target = new Button("Target: ");
        target.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbl.setConstraints(target, gbc);
        this.add(target);

        confirm = new Button("Confirm");
        confirm.addActionListener(this);
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbl.setConstraints(confirm, gbc);
        this.add(confirm);

        //  labels
        source = new Label("Source: ");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbl.setConstraints(source, gbc);
        this.add(source);

        currSource = new Label("THIS IS A TEST OF THE CURRENT SOURCE LABEL");
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbl.setConstraints(currSource, gbc);
        this.add(currSource);

        currTarget = new Label("THIS IS A TEST OF THE CURRENT TARGET LABEL");
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbl.setConstraints(currTarget, gbc);
        this.add(currTarget);

        fileName = new Label("File Name: ");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbl.setConstraints(fileName, gbc);
        this.add(fileName);
        
    }

    //  update labels to reflect current selections
    public void updateLabels() {

    }

    public void actionPerformed(ActionEvent e) {

    }
}