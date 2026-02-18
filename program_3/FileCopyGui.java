import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class FileCopyGui {

    public static void main(String[] args) {
        Window win = new Window("Test", 500, 500);

    }
}

//  handles all GUI init and updates
class Window {
    //  frame data
    private JFrame frame;

    //  label data

    //  gridbag data
    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    

    public Window(String title, int x, int y) {

        //  frame construction
        this.frame = new JFrame();
        this.frame.setTitle(title);
        this.frame.setSize(x, y);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);

        //  gridbag setup
        this.gbl = new GridBagLayout();
        this.gbc = new GridBagConstraints();
    }
}