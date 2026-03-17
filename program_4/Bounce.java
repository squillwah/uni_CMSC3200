
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
    private Scrollbar sb_tickrate, sb_velocity, sb_size;
    private Label sb_tickrate_lbl, sb_velocity_lbl, sb_size_lbl;

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

    // Set dimension variables relative to frame width and height.
    public void set_dimensions(int w, int h) {
        margins = getInsets();
        window_width  = w;
        window_height = h;

        // Get true window width (minus margins) for accurate button/scroll width calculations.
        int marginalized_w = window_width - margins.left - margins.right;
        conpan_size   = 55;
        conpan_sepa   = marginalized_w/80;   
        dim_button_w  = marginalized_w/13; 
        dim_button_h  = 20;                     // Button and scroll heights are static.
        dim_scroll_w  = marginalized_w/6; 
        dim_scroll_h  = 20;
    }

    public void init_components() {
        // Configure frame, set size to match dimension variables.
        setPreferredSize(new Dimension(window_width, window_height));
        setMinimumSize(getPreferredSize());
        setBounds(100, 100, window_width, window_height);  // @? What does this one do?
        setBackground(Color.lightGray);
        // Create the graphics, initialize size within margins and above control panel:
        bsim = new BounceSim(window_width-margins.left-margins.right, window_height-margins.top-margins.bottom-conpan_size);
        bsim.setBackground(Color.white);
        add(bsim);
        // Create conpan elements, add to frame and attach related event listeners:
        bt_start = new Button("Start");                             add(bt_start);          bt_start.addActionListener(this);
        bt_shape = new Button("Circle");                            add(bt_shape);          bt_shape.addActionListener(this);
        bt_clear = new Button("Clear");                             add(bt_clear);          bt_clear.addActionListener(this);
        bt_tail = new Button("No Toil");                            add(bt_tail);           bt_tail.addActionListener(this);
        bt_quit = new Button("Quit");                               add(bt_quit);           bt_quit.addActionListener(this);
        sb_tickrate = new Scrollbar(Scrollbar.HORIZONTAL);          add(sb_tickrate);       sb_tickrate.addAdjustmentListener(this);
        sb_velocity = new Scrollbar(Scrollbar.HORIZONTAL);          add(sb_velocity);       sb_velocity.addAdjustmentListener(this);
        sb_size = new Scrollbar(Scrollbar.HORIZONTAL);              add(sb_size);           sb_size.addAdjustmentListener(this);
        sb_tickrate_lbl = new Label(("Tickrate"), Label.CENTER);    add(sb_tickrate_lbl); 
        sb_velocity_lbl = new Label(("Speed"), Label.CENTER);       add(sb_velocity_lbl); 
        sb_size_lbl = new Label(("Size"), Label.CENTER);            add(sb_size_lbl);
        // Scrollbar value setting:
        Scrollbar[] bars = {sb_size, sb_tickrate, sb_velocity};
        for (Scrollbar bar : bars) { 
            bar.setMinimum(1); 
            bar.setValue(1);
            bar.setMaximum(1200); 
            bar.setVisibleAmount(200); 
            bar.setUnitIncrement(5); 
            bar.setBlockIncrement(50); 
            bar.setBackground(Color.gray); 
        } 
        adjust_tickrate(.2475);
        adjust_velocity(.35);
        adjust_size(.25);
        
        // Add self as component/window event listener. Always do this last (avoid null exceptions in premature events).
        this.addComponentListener(this);
        this.addWindowListener(this);
        
        validate();
    }

    public void size_components() {
        // Place control row slightly above center of control panel height, accounting for the bottom margin.
        int conpan_y = window_height - (conpan_size+margins.bottom - conpan_size/8); 
        // Begin row at left of center (center being calculated with margins subtracted for true area). Left starting position 
        // is center subtracted by half of the combined widths of all buttons and scrollbars, plus the left margin, for accurate centering.
        int conpan_x = ((window_width-margins.left-margins.right)/2)+margins.left - (dim_button_w*5 + dim_scroll_w*3 + conpan_sepa*7)/2; 
        
        // Tickrate bar        
        sb_tickrate.setLocation(conpan_x, conpan_y);
        sb_tickrate.setSize(dim_scroll_w, dim_scroll_h);
        sb_tickrate_lbl.setLocation(conpan_x, conpan_y + dim_scroll_h);
        sb_tickrate_lbl.setSize(dim_scroll_w, dim_scroll_h);
        conpan_x += dim_scroll_w + conpan_sepa;
        // Velocity bar 
        sb_velocity.setLocation(conpan_x, conpan_y);
        sb_velocity.setSize(dim_scroll_w, dim_scroll_h);
        sb_velocity_lbl.setLocation(conpan_x, conpan_y + dim_scroll_h);
        sb_velocity_lbl.setSize(dim_scroll_w, dim_scroll_h);
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
        bsim.set_pause(true); // Start paused.
        bsim.start();
    }

    public void stop() {
        // Stop sim thread, remove listeners, dispose frame, kill.
        bsim.stop();
        bt_start.removeActionListener(this);
        bt_shape.removeActionListener(this);
        bt_clear.removeActionListener(this);
        bt_tail.removeActionListener(this);
        bt_quit.removeActionListener(this);
        sb_tickrate.removeAdjustmentListener(this);
        sb_velocity.removeAdjustmentListener(this);
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
        // Functions of buttons:
        //  bt_start - pause/resume bounce sim
        //  bt_shape - toggle circle/square
        //  bt_tail - toggle tail/no tail
        //  bt_clear - set clear and force a repaint
        //  bt_quit - call stop() (cleanup and exit)
        Object source = e.getSource(); 
        if (source == bt_start) {
            bsim.set_pause(!bsim.is_paused());
            if (bsim.is_paused()) bt_start.setLabel("Run");
            else bt_start.setLabel("Pause");
        } else 
        if (source == bt_shape) {
            bsim.set_circle(!bsim.is_circle());
            if (bsim.is_paused()) bsim.repaint();  // Force repaint if paused.
            if (bsim.is_circle()) bt_shape.setLabel("Square");
            else bt_shape.setLabel("Circle");
        } else
        if (source == bt_tail) {
            bsim.set_tail(!bsim.has_tail());
            if (bsim.has_tail()) bt_tail.setLabel("No Tail");
            else bt_tail.setLabel("Tail");
        } else
        if (source == bt_clear) {
            bsim.clear(); 
            bsim.repaint();
        } else
        if (source == bt_quit) {
            stop();
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        Scrollbar bar = (Scrollbar)e.getSource();
        if (bar == sb_tickrate) {
            adjust_tickrate(bar.getValue()/(double)(bar.getMaximum()-bar.getVisibleAmount()));
        } else
        if (bar == sb_velocity) {
            adjust_velocity(bar.getValue()/(double)(bar.getMaximum()-bar.getVisibleAmount()));
        } else
        if (bar == sb_size) {
            adjust_size(bar.getValue()/(double)(bar.getMaximum()-bar.getVisibleAmount()));
            if (bsim.is_paused()) bsim.repaint(); // Force repaint to show new size in pause state.
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
        new Bounce(800, 600);
    }

    // Set values/scrolls to the given percent along their MINs/MAXs. 
    public void adjust_tickrate(double percent) {
        percent = Util.restrict_bounds(percent, 0.001, 1);
        int new_tickrate = (int)Math.round(Util.relate_bounds(percent, 0.001, 1, bsim.SIM_TICKRATE_MIN, bsim.SIM_TICKRATE_MAX));

        bsim.sim_set_tickrate(new_tickrate);
        
        // Update scrollbar.
        sb_tickrate.setValue(Util.relate_bounds(new_tickrate, bsim.SIM_TICKRATE_MIN, bsim.SIM_TICKRATE_MAX, sb_tickrate.getMinimum(), sb_tickrate.getMaximum()-sb_tickrate.getVisibleAmount()));
        sb_tickrate_lbl.setText("Tickrate (speed): " + new_tickrate + "t/s");
    }
    public void adjust_velocity(double percent) {
        percent = Util.restrict_bounds(percent, 0.001, 1);
        double magnitude = Util.relate_bounds(percent, 0.001, 1, bsim.BODY_VEL_MIN, bsim.BODY_VEL_MAX);
        
        // Preserve vector heading.
        Vec2 new_vel = bsim.body_get_velocity(); 
        new_vel.x /= Math.abs(new_vel.x); // May cause issue if vel is ever 0
        new_vel.y /= Math.abs(new_vel.y);
        // Adjust velocity magnitude, given the percentage between VEL_MIN and VEL_MAX.
        // (equal components, assuming movement is always perfectly diagonal).
        new_vel.mul(Math.sqrt((magnitude*magnitude/2)));
        bsim.body_set_velocity(new_vel);

        sb_velocity.setValue((int)Math.round(Util.relate_bounds(magnitude, bsim.BODY_VEL_MIN, bsim.BODY_VEL_MAX, sb_velocity.getMinimum(), sb_velocity.getMaximum()-sb_velocity.getVisibleAmount())));
        sb_velocity_lbl.setText("Velocity: " + Math.round(magnitude*100)/100.0 + "px/t");
    }
    public void adjust_size(double percent) { 
        percent = Util.restrict_bounds(percent, 0.001, 1);
        int new_size = (int)Math.round(Util.relate_bounds(percent, 0.001, 1, bsim.BODY_SIZE_MIN, bsim.BODY_SIZE_MAX));
        
        bsim.body_set_size(new_size);
        new_size = bsim.body_get_size(); // Size may have been restricted below SIZE_MAX if object was close to screen edge.
        
        sb_size.setValue(Util.relate_bounds(new_size, bsim.BODY_SIZE_MIN, bsim.BODY_SIZE_MAX, sb_size.getMinimum(), sb_size.getMaximum()-sb_size.getVisibleAmount()));
        sb_size_lbl.setText("Size: " + new_size*2+1 + "px");
    }
}

    // @todo Percent util thing should go from 0 not 0.0001 maybe

class BounceSim extends Canvas implements Runnable {
    private static final long serialVersionUID = 11L;

    public final int SIM_TICKRATE_MIN = 1;
    public final int SIM_TICKRATE_MAX = 256;
    public final double BODY_VEL_MIN = 0.01;    // Magnitude, sqrt(x^2 + y^2)
    public final double BODY_VEL_MAX = 20;
    public final int BODY_SIZE_MIN = 10;
    public final int BODY_SIZE_MAX = 200;

    private int screen_width;
    private int screen_height;
  
    // Sim settings: 
    private int sim_delay;          // Ms delay between ticks, 1000/tickrate.
    private int sim_tickrate; 
    private Thread sim_thread;
    private boolean sim_paused;
    private boolean sim_running;

    // Render flags: 
    private boolean render_clear;   // Clear the canvas on next draw. Resets self.
    private boolean render_circle;  // Render the body as a circle. Otherwise drawn as square.
    private boolean render_tail;    // Keep tails (don't erase last frame's shape).
   
    // State of previous frame object, for tail erasure/shape changes. 
    private boolean notail_circle;
    private int notail_x, notail_y; 
    private int notail_size;    

    // Bouncing body:
    private int size;   // Expands outward from center. 
    private Vec2 pos;   // Relative to center pixel.
    private Vec2 vel;   // Pixels per tick.
    
    public BounceSim(int w, int h) {
        screen_width = w; screen_height = h;
        
        sim_tickrate = SIM_TICKRATE_MIN;
        sim_delay = 1000/sim_tickrate;
        sim_thread = null;
        sim_paused = true;
        sim_running = false;

        size = BODY_SIZE_MIN;
        pos = new Vec2(screen_width/4, screen_height/4);
        vel = Vec2.mul((new Vec2(1,1)), Math.sqrt((BODY_VEL_MIN*BODY_VEL_MIN)/2));

        render_circle = false;  // Start as rect, with tails.
        render_tail = true;
        render_clear = false;
        notail_x = (int)Math.round(pos.x);
        notail_y = (int)Math.round(pos.y);
        notail_size = size;
        notail_circle = render_circle;
    }

    public void start() {
        if (sim_thread == null) {
            sim_running = true;
            sim_thread = new Thread(this);
            sim_thread.start();
        }
    }
    public void stop() {
        if (sim_thread != null) {
            sim_running  = false;
            sim_thread.interrupt();
            sim_thread = null;
        }
    }

    public void run() {
        while (sim_running) {
            while (!sim_paused) {
                Vec2 next_pos = Vec2.add(pos, vel);
                
                // Screen collision detection: 
                if (next_pos.x < 1+size+1) { 
                    next_pos.x = 1+size+1; 
                    vel.x = -vel.x; 
                } else if (next_pos.x > screen_width-size-1-1) { 
                    next_pos.x = screen_width-size-1-1; 
                    vel.x = -vel.x; 
                }
                if (next_pos.y < 1+size+1) { 
                    next_pos.y = 1+size+1; 
                    vel.y = -vel.y; 
                } else if (next_pos.y > screen_height-size-1-1) { 
                    next_pos.y = screen_height-size-1-1; 
                    vel.y = -vel.y; 
                }

                pos = next_pos;
                repaint();
                
                try { Thread.sleep(sim_delay); }
                catch (InterruptedException e) {}
            }
            try { Thread.sleep(1); }    // To update sim_pause on interrupts.
            catch (InterruptedException e) {}   

        }
    }
    
    public int get_width() { return screen_width; }
    public int get_height() { return screen_height; }
    public void resize_screen(int w, int h) { 
        screen_width = w; screen_height = h; 
        pos.x = Util.restrict_bounds(pos.x, size+1, screen_width-size-1);
        pos.y = Util.restrict_bounds(pos.y, size+1, screen_height-size-1);
        setSize(screen_width, screen_height);
    }    
    
    public boolean is_circle() { return render_circle; }  
    public boolean has_tail() { return render_tail; }
    public boolean is_paused() { return sim_paused; }
    public void clear() { render_clear = true; }
    public void set_circle(boolean c) { notail_circle = render_circle; render_circle = c; }
    public void set_tail(boolean t) { render_tail = t; }
    public void set_pause(boolean p) { 
        if (sim_paused != p) {
            sim_paused = p; 
            sim_thread.interrupt();
        }
    }
    
    // Use update to draw graphics instead of paint (remove screen wipes).
    public void update(Graphics g) {
        if (render_clear) {
            super.paint(g); // Call original paint function, which calls original update which clears the screen. ? maybe, not sure if thats how the stack works.
            render_clear = false;  // Clear the clear.
            g.setColor(Color.red);
            g.drawRect(0, 0, screen_width-1, screen_height-1);    // Redraw the red boarder.
        }
        if (!render_tail || sim_paused) {   // Clear previous frame when tails disabled or bouncing paused.
            g.setColor(getBackground());
            if (notail_circle) g.fillOval(notail_x-notail_size-2, notail_y-notail_size-2, notail_size*2+3, notail_size*2+3);
            else g.fillRect(notail_x-notail_size-1, notail_y-notail_size-1, notail_size*2+2, notail_size*2+2);
        }

        // Top left position from center pixel, rounding subpixel to pixel precision.
        int tl_x = (int)Math.round(pos.x-1-size);   
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

        notail_x = (int)Math.round(pos.x);
        notail_y = (int)Math.round(pos.y);
        notail_circle = render_circle;
        notail_size = size;
    }

    // Override paint to draw the red boarder and call update (os will trigger a paint occasionally).
    public void paint(Graphics g) {
        g.setColor(Color.red);
        g.drawRect(0, 0, screen_width-1, screen_height-1);
        update(g);
    }

    public void sim_set_tickrate(int tps) {
        sim_tickrate = Util.restrict_bounds(tps, SIM_TICKRATE_MIN, SIM_TICKRATE_MAX);
        sim_delay = (int)Math.round(1000/sim_tickrate);
    }
    public void body_set_velocity(Vec2 new_vel) {
        // Restrict bounds? final VEL_COMPONENT_MAX instead of magnitude?
        vel.x = new_vel.x; vel.y = new_vel.y;
    }
    public void body_set_size(int px) {
        notail_size = size;
        int next_size = Util.restrict_bounds(px, BODY_SIZE_MIN, BODY_SIZE_MAX);
        // Restrict within screen bounds from current position:
        if ((pos.x+next_size+1) >= screen_width-1) next_size = (int)(screen_width-pos.x-2);
        else if ((pos.x-next_size-1) <= 1) next_size = (int)(pos.x-2);
        if ((pos.y+next_size+1) >= screen_height-1) next_size = (int)(screen_height-pos.y-2);
        else if ((pos.y-next_size-1) <= 1) next_size = (int)(pos.y-2);
        size = next_size;
    }

    public int sim_get_tickrate() { return sim_tickrate; }
    public Vec2 body_get_velocity() { return (new Vec2(vel)); }
    public int body_get_size() { return size; }
    
    //public double body_get_velocity() { return Math.round(Math.sqrt(vel.x*vel.x+vel.y+vel.y)*100)/100.0; }
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
    public static Vec2 mul(Vec2 multiplicand, double multiplier) {
        Vec2 product = new Vec2(multiplicand);
        product.mul(multiplier);
        return product;
    }

}

class Util {
    private Util() {}

    // Templates?
    public static int restrict_bounds(int num, int lower, int upper) {
        if (num < lower) num = lower; else if (num > upper) num = upper; return num;
    }
    public static double restrict_bounds(double num, double lower, double upper) {
        if (num < lower) num = lower; else if (num > upper) num = upper; return num;
    }
    public static int relate_bounds(int num, int low1, int up1, int low2, int up2) {
        return (int)Math.round(relate_bounds((double)num, low1, up1, low2, up2)); // @jank, nuneeded
        //System.out.println(num + "|" + low1 + "," + up1 + "|" + low2 + "," + up2 + "\n" + (low2 + ((up2-low2) * ((num-low1)/(up1-low1)))));
        //return low2 + ((up2-low2) * ((num-low1)/(up1-low1))); ! causes issues, always returns 1 because ints, use the double instead.
    }
    public static double relate_bounds(double num, double low1, double up1, double low2, double up2) {
        //System.out.println(num + "|" + low1 + "," + up1 + "|" + low2 + "," + up2 + "\n" + (low2 + ((up2-low2) * ((num-low1)/(up1-low1)))));
        return low2 + ((up2-low2) * ((num-low1)/(up1-low1)));
    }
    //public static double get_normalized_scroll_value(Scrollbar bar) {
    //    return

    // might be better to have a seperate relate_bounds_round() or smthn
}

