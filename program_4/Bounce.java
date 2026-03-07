
package Bounce;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Bounce extends Frame implements WindowListener, ComponentListener, ActionListener, AdjustmentListener {
    private static final long serialVersionUID = 10L;
 
    // Dimension/sizing values. 
    private Insets margins;
    private int window_width;   // Window dimensions, entire frame.
    private int window_height;
    private int conpan_size;    // Vertical pixels allocated to bottom control panel. Viewscreen fills rest above.
    private int conpan_sepa;    // Horizontal pixel separation of each control panel element.
    
    private int dim_button_w;   // Dimensions of buttons
    private int dim_button_h;
    private int dim_scroll_w;   // Dimensions of scrollbars
    private int dim_scroll_h;

    // Control panel components.
    private Button bt_start, bt_shape, bt_clear, bt_tail, bt_quit;
    private Scrollbar sb_speed, sb_size;
    private Label sb_speed_lbl, sb_size_lbl;

    // The bouncing ball canvas
    private BounceScreen screen;

    public Bounce(int w, int h) {
        setLayout(null);

        set_dimensions(w, h);       // Set dimension variables relative to given window size.
        try { init_components(); }  // Initialize and add all components to the window.
        catch (Exception e) { e.printStackTrace(); }
        size_components();          // Adjust component sizes/positions on screen according to dimension variables. 
       
        // Set visible, after all components are initialized and sized. 
        // Doing so beforehand risks null exceptions (undefined getWidth/getHeight in errant componentResized events).
        setVisible(true); 

        // Start the graphics.
        start();
    }

    // Set dimension variables relative frame width and height.
    public void set_dimensions(int w, int h) {
        margins = getInsets();

        window_width  = w;
        window_height = h;

        // Get true window width (minus margins) for accurate button/scroll width calculations.
        int marginalized_w = window_width - margins.left - margins.right;

        conpan_size   = 55;
        conpan_sepa   = marginalized_w/60;   

        dim_button_w  = marginalized_w/11; 
        dim_button_h  = 20;                     // Button and scroll heights are static.
        dim_scroll_w  = marginalized_w/5; 
        dim_scroll_h  = 20;
    }

    public void init_components() {
        // Add self as component/window event listener
        this.addComponentListener(this);
        this.addWindowListener(this);
        
        // Configure frame, set size to match dimension variables.
        setPreferredSize(new Dimension(window_width, window_height));
        setMinimumSize(getPreferredSize());
        setBounds(100, 100, window_width, window_height);  // @? What does this one do?
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
            bar.setMaximum(120);   // Instead of directly relating scrollbar positions to values, use a 1->100 percentage. (120 to account for thumb)
            bar.setMinimum(1);
            bar.setUnitIncrement(5);
            bar.setBlockIncrement(10);
            bar.setValue(50);
            bar.setVisibleAmount(20);
            bar.setBackground(Color.gray);
        }
        // Scrollbar labels, update with current value: 
        sb_speed_lbl = new Label((""), Label.CENTER);   add(sb_speed_lbl);  update_speed_label();
        sb_size_lbl = new Label((""), Label.CENTER);    add(sb_size_lbl);   update_size_label();

        // Graphics:
        screen = new BounceScreen(window_width-margins.left-margins.right, window_height-margins.top-margins.bottom);
        screen.setBackground(Color.white);
        
        validate();
    }

    public void size_components() {
        // Place control row slightly above center of control panel height, accounting for the bottom margin.
        int conpan_y = window_height - (conpan_size+margins.bottom - conpan_size/8); 
        // Begin row at left of center (center being calculated with margins subtracted for true area). Left starting position 
        // is center subtracted by half of the combined widths of all buttons and scrollbars, plus the left margin, for accurate centering.
        int conpan_x = ((window_width-margins.left-margins.right)/2)+margins.left - (dim_button_w*5 + dim_scroll_w*2 + conpan_sepa*6)/2; 

        // Speed bar        
        sb_speed.setLocation(conpan_x, conpan_y);
        sb_speed.setSize(dim_scroll_w, dim_scroll_h);
        sb_speed_lbl.setLocation(conpan_x, conpan_y + dim_scroll_h);
        sb_speed_lbl.setSize(dim_scroll_w, dim_scroll_h);
        conpan_x += dim_scroll_w + conpan_sepa;

        // Buttons
        Button[] butts = {bt_start, bt_shape, bt_tail, bt_clear, bt_quit};
        for (Button butt : butts) {
            butt.setLocation(conpan_x, conpan_y);
            butt.setSize(dim_button_w, dim_button_h);
            conpan_x += dim_button_w + conpan_sepa;
        }
        
        // Size bar 
        sb_size.setLocation(conpan_x, conpan_y);
        sb_size.setSize(dim_scroll_w, dim_scroll_h);
        sb_size_lbl.setLocation(conpan_x, conpan_y + dim_scroll_h);
        sb_size_lbl.setSize(dim_scroll_w, dim_scroll_h);
        
        System.out.println("hello");
    }

    public void start() {

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
    
    public void windowClosing(WindowEvent e) { stop(); }

    public void componentResized(ComponentEvent e) {
        set_dimensions(getWidth(), getHeight());
        size_components();
    }

    public void actionPerformed(ActionEvent e) { 
        // scrollbar.value = ball.set_value(scrollbar.value) [should return best it could do, percentage]
        
        Object source = e.getSource(); 

        if (source == bt_start) {
            // This should be done a different way. Using the text itself feels wrong.
            if (bt_start.getLabel() == "Pause") bt_start.setLabel("Run");    
            else bt_start.setLabel("Pause");
        } else 
        if (source == bt_shape) {
            if (bt_shape.getLabel() == "Circle") bt_shape.setLabel("Square");
            else bt_shape.setLabel("Circle");
        } else
        if (source == bt_tail) {
            if (bt_tail.getLabel() == "Tail") bt_tail.setLabel("No Tail");
            else bt_tail.setLabel("Tail");
        } else
        if (source == bt_clear) {
            System.out.println("clear");
        } else
        if (source == bt_quit) {
            stop();
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        Scrollbar bar = (Scrollbar)e.getSource();
        if (bar == sb_speed) {
            bar.setValue((int)(screen.gradiate_speed(bar.getValue()/100.0)*100));
            update_speed_label();
        } else
        if (bar == sb_size) {
            bar.setValue((int)(screen.gradiate_size(bar.getValue()/100.0)*100));
            update_size_label();
        }
    }

    private void update_speed_label() {
        int val = sb_speed.getValue();
        if (val == 100) sb_speed_lbl.setText(("Speed (MAX%)"));
        else if (val == 1) sb_speed_lbl.setText(("Speed (MIN%)"));
        else sb_speed_lbl.setText(("Speed (" + val + "%)"));
    }
    private void update_size_label() {
        int val = sb_size.getValue();
        if (val == 100) sb_size_lbl.setText(("Size (MAX%)"));
        else if (val == 1) sb_size_lbl.setText(("Size (MIN%)"));
        else sb_size_lbl.setText(("Size (" + val + "%)"));
    }
    
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}

    public static void main(String args[]) {
        new Bounce(640, 400);
    }
}

class BounceScreen {
    private final int SPEED_MIN = 1;
    private final int SPEED_MAX = 100;
    private final int SIZE_MIN = 10;
    private final int SIZE_MAX = 200;

    int width, height;

    private int size, size_constraint;
    private int pos_x, pos_y; 
    private int speed;

    public BounceScreen(int w, int h) {
        width = w; height = h;
        size = 0; size_constraint = SIZE_MAX;
        pos_x = 0; pos_y = 0;
        speed = 0; 

        gradiate_speed(.5); // Initialize size and speed to their middle values.
        gradiate_size(.5);
    }

    // Adjust speed between SPEED_MIN and SPEED_MAX, given the percentage between them. 
    // Returns the new speed (or should it be the percentage?).
    //  (0.01 == SPEED_MIN, 1 == SPEED_MAX) 
    public double gradiate_speed(double percentage) { 
        if (percentage >= 1) percentage = 1;
        else if (percentage <= 0.01) percentage = 0.01; 
        speed = (int)((SPEED_MAX-SPEED_MIN) * percentage + SPEED_MIN);

        System.out.println("Speed: " + speed + " | " + percentage + "%");

        return percentage; // or speed? Could calculate percentage at scrollbar set.
    }
    // Adjust size between MIN and MAX, given the percentage between them. 
    // Restricts size to size_constraint. Returns new size;
    public double gradiate_size(double percentage) {

        // Could caluclate size constraint here, with ball pos and screen w/h
        // would be better than storing the variable and updating every move

        if (percentage >= 1) percentage = 1;
        else if (percentage <= 0.01) percentage = 0.01; 
        size = (int)((SIZE_MAX-SIZE_MIN) * percentage + SIZE_MIN);
        if (size > size_constraint) {
            size = size_constraint;
            percentage = (double)(size-SIZE_MIN) / (SIZE_MAX-SIZE_MIN);
        }

        System.out.println("Size: " + size + " | " + percentage + "%");

        return percentage;
    }
}


// Use update to draw graphics instead of paint (to remove screen wipes).

// Override paint to call update (os will trigger a paint occasionally).


/*
    
    public static double adjust_percent(double perc) {
        // Handle oob percentages
        int MIN = 235;
        int MAX = 512;

        // Set value to range between min and max:
        int value;
        if (perc >= 1) value = MAX;
        else if (perc <= 0) value = MIN;
        else value = (int)((MAX-MIN) * perc + MIN);

        // If requested percent is greater than the current constraint, constrain:
        int constraint = 420;
        if (value > constraint) {
            value = constraint;
            perc = (double)(constraint-MIN) / (MAX-MIN);
        }

        System.out.println(value);
        return perc;

        *
        int value = MIN + (MAX-MIN)/perc;

        int constraint = 420;

        if (value > constraint) {
            value = constraint;
            perc = (MAX-MIN)/(constraint-MIN);
        }

        System.out.println(value);
        return perc;
        *
    
*/
