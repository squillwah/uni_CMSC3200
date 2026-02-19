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
    Label title;    //  POORLY NAMED RN IS NOT THE TITLE

    public Window() {

        //  establishing how buttons and labels go onto the screen
        double colWeight[] = {1};   //  MESSING WITH THESE, DONT HAVE AN
        double rowWeight[] = {1};   //  INTUITIVE FEEL FOR EM
        int colWidth[] = {1};
        int rowHeight[] = {1};

        gbl.rowHeights = rowHeight;
        gbl.columnWidths = colWidth;
        gbl.rowWeights = rowWeight;
        gbl.columnWeights = colWeight;

        //  setting up frame settings
        initFrame();
        this.setBounds(20,20,500,500);
        this.setLayout(gbl);
        this.addWindowListener(this);
        this.setLocationRelativeTo(null);
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
        confirm = new Button("Confirm");
        confirm.addActionListener(this);

        //  labels
        title = new Label("Test");

        //  setup window
        gbl.setConstraints(title, gbc);
        this.add(title);
    }

    public void actionPerformed(ActionEvent e) {

    }
}