
package Bounce;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Bounce extends Frame implements WindowListener, ComponentListener, ActionListener, AdjustmentListener {
    private static final long serialVersionUID = 10L;
 
    // Dimension/sizing values. 
    private Insets margins;
    private int window_width ;// = 640;  // Window dimensions, entire frame.
    private int window_height;// = 400;
    private int conpan_size  ;// = 100;//window_width/12;   // Vertical pixels allocated to bottom control panel. Viewscreen fills rest above.
    private int conpan_sepa  ;// = window_width/48;   // Horizontal pixel separation of each control panel element.
    
    private int dim_button_w ;// = window_width/12;   // Dimensions of buttons
    private int dim_button_h ;// = 20;
    private int dim_scroll_w ;// = window_width/6;    // Dimensions of scrollbars
    private int dim_scroll_h ;// = 20;

    // Control panel components.
    private Button bt_start, bt_shape, bt_clear, bt_tail, bt_quit;
    private Scrollbar sb_speed, sb_size;
    private Label sb_speed_lbl, sb_size_lbl;
    //enum Butt {start 0, shape 1, clear 2, tail 3, quit 4}
    //Button butts[];

    public Bounce(int w, int h) {
        setLayout(null);

        // Set dimension variables relative to given window size.
        set_dimensions(w, h);   
        // Initialize and add all components to the window.
        try { init_components(); }
        catch (Exception e) { e.printStackTrace(); }
        // Adjust component sizes/positions on screen according to dimension variables. 
        size_components();
       
        // Set visible, after all components are initialized and sized. 
        // Doing so beforehand risks null exceptions (undefined getWidth/getHeight in errant componentResized events).
        setVisible(true); 
    }

    // Set dimension variables relative frame width and height.
    public void set_dimensions(int w, int h) {
        margins = getInsets();

        window_width  = w;
        window_height = h;
        conpan_size   = 55; // Control panel height is static.
        conpan_sepa   = window_width/60;   
        
        dim_button_w  = window_width/11; 
        dim_button_h  = 20; // Button and scroll heights are static.
        dim_scroll_w  = window_width/5; 
        dim_scroll_h  = 20;
    }

    public void init_components() {
        // Add self as component/window event listener
        this.addComponentListener(this);
        this.addWindowListener(this);
        
        // Configure frame, set size to match dimension variables.
        setPreferredSize(new Dimension(window_width, window_height));
        setMinimumSize(getPreferredSize());
        setBounds(10, 10, window_width, window_height);  // @? What does this one do?
        setBackground(Color.lightGray);
        
        // Create buttons, add to frame, attach action listeners:
        bt_start = new Button("Run");       add(bt_start);  bt_start.addActionListener(this);
        bt_shape = new Button("Circle");    add(bt_shape);  bt_shape.addActionListener(this);
        bt_clear = new Button("Clear");     add(bt_clear);  bt_clear.addActionListener(this);
        bt_tail = new Button("No Toil");    add(bt_tail);   bt_tail.addActionListener(this);
        bt_quit = new Button("Quit");       add(bt_quit);   bt_quit.addActionListener(this);

        // Create scrollbars, add to frame, attach adjustment listeners, configure scrollbars:
        sb_speed = new Scrollbar(Scrollbar.HORIZONTAL);     add(sb_speed);  sb_speed.addAdjustmentListener(this);
        sb_size = new Scrollbar(Scrollbar.HORIZONTAL);      add(sb_size);   sb_size.addAdjustmentListener(this);
        Scrollbar[] bars = {sb_speed, sb_size};
        for (Scrollbar bar : bars) {
            bar.setMaximum(110);   // Instead of directly relating scrollbar positions to values, use a percentage. (110 to account for thumb)
            bar.setMinimum(1);
            bar.setUnitIncrement(5);
            bar.setBlockIncrement(10);
            bar.setValue(50);
            bar.setVisibleAmount(10);
            bar.setBackground(Color.gray);
        }
        // Scrollbar labels: 
        sb_speed_lbl = new Label("Speed", Label.CENTER);    add(sb_speed_lbl);
        sb_size_lbl = new Label("Size", Label.CENTER);      add(sb_size_lbl);

        validate();
    }

    public void size_components() {
        // ! these calculations do not account for insets, @todo

        int conpan_y = window_height - (conpan_size - conpan_size/8);  // All controls in line at center control panel height.
        int conpan_x = window_width/2 - (dim_button_w*5 + dim_scroll_w*2 + conpan_sepa*6)/2;  // Start at left end of control panel width, centered along window width.

        // Speed bar        
        sb_speed.setLocation(conpan_x, conpan_y);
        sb_speed.setSize(dim_scroll_w, dim_scroll_h);
        sb_speed_lbl.setLocation(conpan_x, conpan_y + dim_scroll_h);
        sb_speed_lbl.setSize(dim_scroll_w, dim_scroll_h);
        conpan_x += dim_scroll_w + conpan_sepa;
        // Start button
        bt_start.setLocation(conpan_x, conpan_y);
        bt_start.setSize(dim_button_w, dim_button_h);
        conpan_x += dim_button_w + conpan_sepa;
        // Shape button
        bt_shape.setLocation(conpan_x, conpan_y);
        bt_shape.setSize(dim_button_w, dim_button_h);
        conpan_x += dim_button_w + conpan_sepa;
        // Tail button 
        bt_tail.setLocation(conpan_x, conpan_y);
        bt_tail.setSize(dim_button_w, dim_button_h);
        conpan_x += dim_button_w + conpan_sepa;
        // Clear button
        bt_clear.setLocation(conpan_x, conpan_y);
        bt_clear.setSize(dim_button_w, dim_button_h);
        conpan_x += dim_button_w + conpan_sepa;
        // Quit button 
        bt_quit.setLocation(conpan_x, conpan_y);
        bt_quit.setSize(dim_button_w, dim_button_h);
        conpan_x += dim_button_w + conpan_sepa;
        // Size bar 
        sb_size.setLocation(conpan_x, conpan_y);
        sb_size.setSize(dim_scroll_w, dim_scroll_h);
        
        System.out.println("hello");
    }

    public void stop() {
        // Remove listeners, dispose frame, kill.
        bt_start.removeActionListener(this);
        bt_shape.removeActionListener(this);
        bt_clear.removeActionListener(this);
        bt_tail.removeActionListener(this);
        bt_quit.removeActionListener(this);
        sb_speed.removeAdjustmentListener(this);
        sb_size.removeAdjustmentListener(this);
        this.removeComponentListener(this);
        this.removeWindowListener(this);
        dispose();
        System.exit(0);
    }
    
    public void windowClosing(WindowEvent e) {
        stop();
    }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}

    public void componentResized(ComponentEvent e) {
        set_dimensions(getWidth(), getHeight());
        size_components();
    }
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}

    public void actionPerformed(ActionEvent e) {}

    public void adjustmentValueChanged(AdjustmentEvent e) {}

    public static void main(String args[]) {
        new Bounce(640, 400);
    }
}


