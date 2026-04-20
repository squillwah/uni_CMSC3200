//package program_7;

import java.awt.*;
import java.awt.event.*;

public class Chat implements ActionListener, AdjustmentListener, ComponentListener, ItemListener, WindowListener {
    
    private static final long serialVersionUID = 1111L;
    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    
    private Frame window;

    private MenuBar mbar;
    private Menu mnu_user, mnu_help;
    private MenuItem mi_exit, mi_about;

    public static void main(String[] args) {
        new Chat();
    }

    public Chat() {
        window = new Frame("Chat");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setLayout(new BorderLayout());

        ;   // menu bar
        mbar = new MenuBar();

        mnu_user = new Menu("User");
        mnu_help = new Menu("Help");

        mi_exit = new MenuItem("Exit");
        mi_about = new MenuItem("About");

        mi_exit.addActionListener(this);
        mi_about.addActionListener(this);

        mnu_user.add(mi_exit);
        mnu_help.add(mi_about);

        mbar.add(mnu_user);
        mbar.add(mnu_help);

        window.setMenuBar(mbar);

        ;   // simple content
        Label title = new Label("AWT Chat Window", Label.CENTER);
        window.add(title, BorderLayout.CENTER);

        ;   // listeners
        window.addWindowListener(this);
        window.addComponentListener(this);

        ;   // show window
        window.setVisible(true);
    }

    //  Listeners
    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
        window.dispose();
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void itemStateChanged(ItemEvent e) {}

    public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}

    public void componentResized(ComponentEvent e) {}

    public void componentShown(ComponentEvent e) {}

    public void adjustmentValueChanged(AdjustmentEvent e) {}

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == mi_exit) {
            window.dispose();
            System.exit(0);
        }

        if (src == mi_about) {
            Frame about = new Frame("About");
            about.setSize(300, 150);
            about.setLayout(new BorderLayout());
            about.add(new Label("Basic AWT window is working.", Label.CENTER), BorderLayout.CENTER);

            about.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    about.dispose();
                }
            });

            about.setVisible(true);
        }
    }
}