
package Bounce;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

// BounceSim should have a set_delay and set_size, which checks the bounds and returns the size it actually set too
// Bounce should then implement the gradiate_delay and such for the scrolls

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

    // The bouncing body canvas.
    private BounceSim bsim;

    public Bounce(int w, int h) {
        setLayout(null);
        set_dimensions(w, h);       // Set dimension variables relative to given window size.
        try { init_components(); }  // Initialize and add all components to the window.
        catch (Exception e) { e.printStackTrace(); }
        size_components();          // Adjust component sizes/positions on screen according to dimension variables. 
        setVisible(true); 
        start();                    // Start the graphics.
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
        // Configure frame, set size to match dimension variables.
        setPreferredSize(new Dimension(window_width, window_height));
        setMinimumSize(getPreferredSize());
        setBounds(100, 100, window_width, window_height);  // @? What does this one do?
        setBackground(Color.lightGray);
        // Create the graphics, initialize size within margins and above control panel:
        bsim = new BounceSim(window_width - margins.left - margins.right, window_height - margins.top - margins.bottom - conpan_size);
        bsim.setBackground(Color.white);
        add(bsim);
        // Create conpan elements, add to frame and attach related event listeners:
        bt_start = new Button("Run");       add(bt_start);  bt_start.addActionListener(this);
        bt_shape = new Button("Circle");    add(bt_shape);  bt_shape.addActionListener(this);
        bt_clear = new Button("Clear");     add(bt_clear);  bt_clear.addActionListener(this);
        bt_tail = new Button("No Toil");    add(bt_tail);   bt_tail.addActionListener(this);
        bt_quit = new Button("Quit");       add(bt_quit);   bt_quit.addActionListener(this);
        sb_speed = new Scrollbar(Scrollbar.HORIZONTAL); add(sb_speed);  sb_speed.addAdjustmentListener(this);
        sb_size = new Scrollbar(Scrollbar.HORIZONTAL);  add(sb_size);   sb_size.addAdjustmentListener(this);
        // Scrollbar value setting:
        Scrollbar[] bars = {sb_speed, sb_size};
        for (Scrollbar bar : bars) { bar.setMaximum(120); bar.setMinimum(1); bar.setUnitIncrement(5); bar.setBlockIncrement(10); bar.setVisibleAmount(20); bar.setBackground(Color.gray); }
        sb_speed.setValue((int)(gradiate_sim_delay(.5)*100));    // Attempt to set scrolls at 50%.
        sb_speed_lbl = new Label((""), Label.CENTER);   add(sb_speed_lbl);  update_speed_label();
        sb_size.setValue((int)(gradiate_body_size(.5)*100));
        sb_size_lbl = new Label((""), Label.CENTER);    add(sb_size_lbl);   update_size_label();
        // Add self as component/window event listener. Always do this last.
        this.addComponentListener(this);
        this.addWindowListener(this);
        
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

        // Size and place the canvas:
        bsim.setLocation(margins.left, margins.top); //margins.top, margins.left);
        bsim.resize_screen(window_width - margins.left - margins.right, window_height - margins.top - margins.bottom - conpan_size);
        
        System.out.println("hello");
    }

    public void start() {
        bsim.repaint();
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
        Object source = e.getSource(); 
        if (source == bt_start) {
            // This should be done a different way. Using the text itself feels wrong.
            if (bt_start.getLabel() == "Pause") bt_start.setLabel("Run");    
            else bt_start.setLabel("Pause");
        } else 
        if (source == bt_shape) {
            bsim.set_circle(!bsim.is_circle());
            if (bsim.is_circle()) bt_shape.setLabel("Square");
            else bt_shape.setLabel("Circle");
            bsim.repaint();   // Force repaint to update shape.
        } else
        if (source == bt_tail) {
            bsim.set_tail(!bsim.has_tail());
            if (bsim.has_tail()) bt_tail.setLabel("No Tail");
            else bt_tail.setLabel("Tail");
        } else
        if (source == bt_clear) {
            System.out.println("clear");
            bsim.clear();     // Wipe canvas and redraw.
            bsim.repaint();
        } else
        if (source == bt_quit) {
            stop();
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        Scrollbar bar = (Scrollbar)e.getSource();
        if (bar == sb_speed) {
            bar.setValue((int)(gradiate_sim_delay(bar.getValue()/100.0)*100));
            update_speed_label();
        } else
        if (bar == sb_size) {
            // Updates value within screen, and sets scrollbar to the returned percentage (size may be restricted).
            bar.setValue((int)(gradiate_body_size(bar.getValue()/100.0)*100));
            update_size_label();
            bsim.repaint();
        }
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

    // Set the simulation delay to the percentange 'p' between SIM_DELAY_MIN and SIM_DELAY_MAX.
    // Returns the percentage achieved, as depending on the body state things might not work out so good.
    public double gradiate_sim_delay(double p) {
        bsim.set_sim_delay((int)(bsim.SIM_DELAY_MIN+((bsim.SIM_DELAY_MAX-bsim.SIM_DELAY_MIN)*p)));
        System.out.println("Speed: " + bsim.get_sim_delay() + " | " + ((double)(bsim.get_sim_delay()-bsim.SIM_DELAY_MIN) / (bsim.SIM_DELAY_MAX-bsim.SIM_DELAY_MIN)) + "%");
        return (double)(bsim.get_sim_delay()-bsim.SIM_DELAY_MIN) / (bsim.SIM_DELAY_MAX-bsim.SIM_DELAY_MIN);
    }
    public double gradiate_body_size(double p) {
        bsim.set_body_size((int)(bsim.BODY_SIZE_MIN+((bsim.BODY_SIZE_MAX-bsim.BODY_SIZE_MIN)*p)));
        System.out.println("Size: " + bsim.get_body_size() + " | " + ((double)(bsim.get_body_size()-bsim.BODY_SIZE_MIN) / (bsim.BODY_SIZE_MAX-bsim.BODY_SIZE_MIN)) + "%");
        return (double)(bsim.get_body_size()-bsim.BODY_SIZE_MIN) / (bsim.BODY_SIZE_MAX-bsim.BODY_SIZE_MIN);
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

    // @todo Make the speedbar adjust body velocity, not the tickrate. Could make sim_delay/tickrate constant, or add a second scroll.
}

class BounceSim extends Canvas implements Runnable {
    private static final long serialVersionUID = 11L;

    public final int SIM_DELAY_MIN = 10;    // 100hz
    public final int SIM_DELAY_MAX = 100;   // 10hz
    public final int BODY_SIZE_MIN = 10;
    public final int BODY_SIZE_MAX = 150;

    private int screen_width, screen_height;
    
    private Thread sim_thread;  // Thread for bouncing simulation.
    private boolean sim_paused;
    private int sim_delay;      // Delay between ticks, milliseconds.

    // Render flags: 
    private boolean render_clear;   // Clear the canvas on next draw. Resets self.
    private boolean render_circle;  // Render the body as a circle. Otherwise drawn as square.
    private boolean render_tail;    // Keep tails (don't erase last frame's shape).

    // Bouncing body:
    private int size;   // Expands outward from center. 
    private Vec2 pos;   // Relative to center pixel.
    private Vec2 vel;   // Pixels per tick.

    public BounceSim(int w, int h) {
        sim_delay = SIM_DELAY_MIN;
        screen_width = w;
        screen_height = h;
        size = BODY_SIZE_MIN;       //size_constraint = SIZE_MAX; calculate constraint on setting of the size
        pos = new Vec2(screen_width/2, screen_height/2); 
        vel = new Vec2(5, 5);
        render_circle = false;  // Start as rect, with tails.
        render_tail = false;
        render_clear = false;
        sim_thread = new Thread(this);
        sim_thread.start();
    }

    public void run() {
        for (int i = 0; i < 1000; i++) {
            pos.add(vel);           // Add velocity, then allow collision detection to restrict bounds. Otherwise, collision frames would not be drawn.
            process_collisions();   
            // could have process collisions return the next position   // or could return vector with overstep from bounds, then just use that as the vel shrink and the flag to restrict pos
            /*Vec2 overstep = process_screen_collisions();
            if (overstep != null) {
                double shrink = Math.abs(Math.max(overstep.x/vel.x, overstep.y/vel.y)); // Add velocity to position, shrunk to the maximum magnitude which remains within bounds of the screen.
                pos.add((new Vec2(vel)).mul(shrink));
                if (overstep.x != 0) vel.x = -vel.x;
                if (overstep.y != 0) vel.y = -vel.y;
            } else { 
                pos.add(vel);
            }*/
            repaint();
            try { Thread.sleep(sim_delay); }
            catch (InterruptedException e) { System.out.println(e); } // should remove
        }
    }

    // Checks next position. 
    // If collision is detected, current position is moved to the farthest point possible along 
    // it's velocity vector, and the corresponding velocity components are reflected.
    // True is returned, to signify that a collision was detected and that the necessary
    // pos and vel adjustments have been made.
    public void process_collisions() {
        //Vec2 next_pos = Vec2.add(pos, vel);       what problem am I even trying to solve? We want check then move, not move then check.
        if (pos.x < 1+size+1) { pos.x = 1+size+1; vel.x = -vel.x; }
        else if (pos.x > screen_width-size-1-1) { pos.x = screen_width-size-1-1; vel.x = -vel.x; }

        if (pos.y < 1+size+1) { pos.y = 1+size+1; vel.y = -vel.y; }
        else if (pos.y > screen_height-size-1-1) { pos.y = screen_height-size-1-1; vel.y = -vel.y; }
    }

    public void resize_screen(int w, int h) { 
        screen_width = w; screen_height = h; 
        setSize(screen_width, screen_height);
    }    
    
    public int get_width() { return screen_width; }
    public int get_height() { return screen_height; }

    public void clear() { render_clear = true; }
    public boolean is_circle() { return render_circle; }  
    public void set_circle(boolean c) { render_circle = c; }
    public boolean has_tail() { return render_tail; }
    public void set_tail(boolean t) { render_tail = t; }

    // Use update to draw graphics instead of paint (remove screen wipes).
    public void update(Graphics g) {
        if (render_clear) {
            super.paint(g); // Call original paint function, which calls original update which clears the screen. ? maybe, not sure if thats how the stack works.
            render_clear = false;  // Clear the clear.
            g.setColor(Color.red);
            g.drawRect(0, 0, screen_width-1, screen_height-1);    // Redraw the red boarder.
        }

        int tl_x = (int)Math.round(pos.x-1-size);  // Top left position from center pixel, rounding subpixel to pixel precision. 
        int tl_y = (int)Math.round(pos.y-1-size);
        if (render_circle) {       
            g.setColor(Color.lightGray);
            g.fillOval(tl_x, tl_y, size*2+1, size*2+1); 
            g.setColor(Color.black);
            g.drawOval(tl_x, tl_y, size*2+1, size*2+1);
        } else {
            g.setColor(Color.lightGray);
            g.fillRect(tl_x, tl_y, size*2+1, size*2+1);
            g.setColor(Color.black);
            g.drawRect(tl_x, tl_y, size*2+1, size*2+1);
        }
    }

    // Override paint to draw the red boarder and call update (os will trigger a paint occasionally).
    public void paint(Graphics g) {
        g.setColor(Color.red);
        g.drawRect(0, 0, screen_width-1, screen_height-1);
        update(g);
    }

    // Set simulation delay and body size, with bounds checking.
    public void set_sim_delay(int ms) {
        if (ms < SIM_DELAY_MIN) ms = SIM_DELAY_MIN;
        else if (ms > SIM_DELAY_MAX) ms = SIM_DELAY_MAX;
        sim_delay = ms;
    }
    public void set_body_size(int px) {
        if (px < BODY_SIZE_MIN) px = BODY_SIZE_MIN;
        else if (px > BODY_SIZE_MAX) px = BODY_SIZE_MAX;
        size = px;  
        // Collision detection in the run thread should catch any boundery breaks.
        //process_collisions();
    }
    public int get_sim_delay() { return sim_delay; }
    public int get_body_size() { return size; }

}

// Two dimensional vector, for velocity and position data.  Should this be doubles (do we want subpixel movement?)
// Doubles (instead of ints) to support pixel movement slower than the tickrate.
class Vec2 {
    public double x;
    public double y;

    public Vec2(double x, double y) { this.x = x; this.y = y; }
    public Vec2(Vec2 copy) { x = copy.x; y = copy.y; }

    public void add(Vec2 addend) { x += addend.x; y += addend.y; }
    public void mul(double scalar) { x *= scalar; y*= scalar; }
    
    public static Vec2 add(Vec2 augend, Vec2 addend) {
        Vec2 resultant = new Vec2(augend);
        resultant.add(addend);
        return resultant;
    }

}

