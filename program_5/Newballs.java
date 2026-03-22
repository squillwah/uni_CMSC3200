
import java.awt.*;
import java.awt.event.*;

public class Newballs extends Frame {
    private static final long SerialVersionUID = 101L;

    private Dimension dim_frame;
    private Dimension dim_screen;   // Necessary?
    private Dimension dim_controls;

    private Panel pnl_screen;
    private Panel pnl_controls;
    
    private GridBagConstraints gbc; // or pnl_controls_gbl?

    private Button bt_start, bt_pause, bt_quit;
    private Scrollbar sb_tickrate, sb_velocity, sb_size;
    private Label sb_tickrate_lbl, sb_velocity_lbl, sb_size_lbl;

    public Newballs(int w, int h) {
        set_dimensions(new Dimension(w, h));
        try { init_components(); } 
        catch (Exception e) { e.printStackTrace(); }
        size_components();  // May not be necessary, with the new layout managers.

        setVisible(true);
    }

    public void set_dimensions(Dimension dim) {
        dim_frame = dim.getSize();      // Copy, don't allow multiple handles of dim_frame.
        dim_screen = null;     
        dim_controls = null;    // Do we actually need to keep track of these, if using borderLayout()?
    }

    public void init_components() {
        // Configure main frame:
        setTitle("Program 5: Bouncing Ball");
        setLayout(new BorderLayout());
        setPreferredSize(dim_frame);
        setMinimumSize(dim_frame);
        setBounds(10, 10, (int)dim_frame.getWidth(), (int)dim_frame.getHeight());
        setBackground(Color.lightGray);
        // Create screen and control panels:
        pnl_screen = new Panel();
        pnl_screen.setLayout(new BorderLayout());
        pnl_controls = new Panel();
        pnl_controls.setLayout(new GridBagLayout());
        add("Center", pnl_screen);
        add("South", pnl_controls);
        // Initialize GUI components:  
        sb_tickrate = new Scrollbar(Scrollbar.HORIZONTAL);
        sb_tickrate_lbl = new Label(("Tickrate"), Label.CENTER);    
        sb_velocity = new Scrollbar(Scrollbar.HORIZONTAL);
        sb_velocity_lbl = new Label(("Speed"), Label.CENTER);       
        sb_size = new Scrollbar(Scrollbar.HORIZONTAL);
        sb_size_lbl = new Label(("Size"), Label.CENTER);            
        bt_start = new Button("START");
        bt_pause = new Button("PAUSE");
        bt_quit = new Button("QUIT");
        Scrollbar[] bars = {sb_size, sb_tickrate, sb_velocity};
        for (Scrollbar bar : bars) { 
            bar.setMinimum(1); 
            bar.setValue(1);
            bar.setMaximum(1200);           // 1,000 values, .001 normalized precision.
            bar.setVisibleAmount(200); 
            bar.setUnitIncrement(5); 
            bar.setBlockIncrement(50); 
            bar.setBackground(Color.gray); 
        } 
        // GridBag Shenanigans: 
        GridBagConstraints gbc = new GridBagConstraints();  
        gbc.gridx = -1;  gbc.gridy = 0;  
        gbc.fill = GridBagConstraints.HORIZONTAL;           
        // | sb | sb | b | b | b | sb |
        // | lb | lb |   |   |   | lb |
        // Tickrate 
        gbc.weightx = .2;
        gbc.insets = new Insets(5,10,0,5);
        gbc.gridx++;
        pnl_controls.add(sb_tickrate, gbc);
        gbc.gridy++; 
        gbc.insets.top = 0;
        pnl_controls.add(sb_tickrate_lbl, gbc);
        gbc.gridy--;
        // Velocity 
        gbc.weightx = .2;
        gbc.insets = new Insets(5,5,0,5);
        gbc.gridx++;  
        pnl_controls.add(sb_velocity, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        pnl_controls.add(sb_velocity_lbl, gbc);
        gbc.gridy--;
        // Buttons 
        gbc.weightx = .1;
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridheight = 2;
        gbc.gridx++;  
        pnl_controls.add(bt_start, gbc);
        gbc.gridx++;  
        pnl_controls.add(bt_pause, gbc);
        gbc.gridx++;  
        pnl_controls.add(bt_quit, gbc);
        gbc.gridheight = 1;
        // Size 
        gbc.weightx = .3;
        gbc.insets = new Insets(5,5,0,10);
        gbc.gridx++;
        pnl_controls.add(sb_size, gbc);
        gbc.gridy++;  
        gbc.insets.top = 0;
        pnl_controls.add(sb_size_lbl, gbc);
        gbc.gridy--;
        
        pnl_screen.setBackground(Color.black);
        pnl_controls.setBackground(Color.red);
        //pnl_controls.setBounds(10, 10, 100, 100);
    }

    public void size_components() {}

    public static void main(String[] args) {
        new Newballs(600, 400);
    }
}

