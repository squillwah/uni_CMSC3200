
// [CMSC3200] Technical Computing Using Java
// Program 5: Bouncing Ball
//
//  A ball moves around the screen, bouncing off rectangles and screen borders.
//  Use the controls to change the tickrate, add, switch, remove balls, and adjust their ball sizes and velocities.
//  Click and drag with the mouse to place rectangles. Right click to delete them.
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu

package BouncingBall;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class BouncingBall extends Frame implements WindowListener, ComponentListener, ActionListener, AdjustmentListener, MouseListener, MouseMotionListener {
    private static final long SerialVersionUID = 101L;

    // Frame size is part of the Frame. Use getSize().
   
    // Two panels of the GUI, ball screen and controls section.
    private Panel pnl_screen;
    private Panel pnl_controls;
    // Bouncing ball/rectangle simulation.
    private BounceSim bsim;
    // BufferedCanvas display for the simulation.
    private BufferedCanvas screen;
    // Elements of controls section.
    private Button bt_start, bt_pause, bt_quit, bt_create, bt_destroy, bt_next;
    private Scrollbar sb_tickrate, sb_velocity, sb_size;
    private Label sb_tickrate_lbl, sb_velocity_lbl, sb_size_lbl;
    // Mouse vars.
    private Rectangle dragbox;
    private Point m1;
    private Point m2;

    public BouncingBall(Dimension initial_size, int fps) {
        // Configure the main frame:
        setTitle("Program 5: Bouncing Ball");
        setLayout(new BorderLayout());
        setPreferredSize(initial_size.getSize());
        setMinimumSize(getPreferredSize());
        setBounds(10, 10, getWidth(), getHeight());
        setBackground(Color.lightGray);
        // Create and initialize panels, components:
        try {
            init_pnl_screen(fps);
            init_pnl_controls();
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
        // Attach window/component listeners, make visible.
        this.addComponentListener(this);
        this.addWindowListener(this);
        // Set visible and adjust screen size the new size of its panel.
        setVisible(true);
        screen.setSize(pnl_screen.getSize());
        // Finally create the BounceSim, with a space size that of screen and the renderer set to screen.
        // * Do this as a final step, as the size and displayablility of BufferedCanvas will be not be set properly until the parent frame visible.
        bsim = new BounceSim(screen.getSize(), screen);
        adjust_tickrate(.2475); adjust_velocity(.35); adjust_size(.25); // Set defaults for the first ball.
        start();
    }


    public void init_pnl_screen(int framerate) {
        // Set Panel layout and add to main frame:
        pnl_screen = new Panel(); 
        pnl_screen.setLayout(new BorderLayout());
        add("Center", pnl_screen);
        dragbox = new Rectangle(0, 0, 0, 0);
        // Create BounceSim, add to panel:
        screen = new BufferedCanvas(getMinimumSize(), framerate);
        pnl_screen.add("Center", screen);
        // Attach mouse listeners:
        screen.addMouseListener(this);
        screen.addMouseMotionListener(this);
        validate();
    }
    public void init_pnl_controls() {
        pnl_controls = new Panel();
        pnl_controls.setLayout(new GridBagLayout());
        pnl_controls.setMaximumSize(getMinimumSize());
        add("South", pnl_controls);
        // Create, initialize components:
        m1 = new Point(0,0);
        m2 = new Point(0,0);
        bt_start = new Button("START"); bt_start.setEnabled(true);
        bt_pause = new Button("PAUSE"); bt_pause.setEnabled(false);
        bt_quit = new Button("QUIT");
        bt_create = new Button("CREATE");
        bt_destroy = new Button("DESTROY"); bt_destroy.setEnabled(false);   // Assuming single ball initialization.
        bt_next = new Button("NEXT"); bt_next.setEnabled(false);
        sb_tickrate_lbl = new Label(("Tickrate: ?t/s"), Label.CENTER);    
        sb_velocity_lbl = new Label(("Velocity: ?px/t"), Label.CENTER);       
        sb_size_lbl = new Label(("Ball Size: ?px"), Label.CENTER);            
        sb_tickrate = new Scrollbar(Scrollbar.HORIZONTAL);
        sb_velocity = new Scrollbar(Scrollbar.HORIZONTAL);
        sb_size = new Scrollbar(Scrollbar.HORIZONTAL);
        Scrollbar[] bars = {sb_tickrate, sb_velocity, sb_size};
        for (Scrollbar bar : bars) { 
            bar.setMinimum(1); 
            bar.setValue(1);
            bar.setMaximum(1200);           // 1,000 values, .001 normalized precision.
            bar.setVisibleAmount(200); 
            bar.setUnitIncrement(5); 
            bar.setBlockIncrement(50); 
            bar.setBackground(Color.gray); 
        } // @todo We should pick more exotic colors for all the UI
        // Gridbag schenanigans: 
        GridBagConstraints gbc = new GridBagConstraints();
        //  Scrollbars 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.075;
        gbc.ipady = 2;
        //  Tickrate 
        gbc.insets.top = 10;  
        gbc.insets.left = 10;
        gbc.gridx = 0; gbc.gridy = 0;
        pnl_controls.add(sb_tickrate, gbc); 
        gbc.insets.top = 3; gbc.insets.bottom = 7;
        gbc.ipady = 0;
        gbc.gridy = 1;
        pnl_controls.add(sb_tickrate_lbl, gbc); 
        gbc.ipady = 2;
        gbc.insets.top = 10; gbc.insets.bottom = 0;
        //  Velocity
        gbc.insets.left = 5;
        gbc.insets.right = 10;
        gbc.gridx = 1; gbc.gridy = 0;
        pnl_controls.add(sb_velocity, gbc); 
        gbc.insets.top = 3; gbc.insets.bottom = 7;
        gbc.ipady = 0;
        gbc.gridy = 1;
        pnl_controls.add(sb_velocity_lbl, gbc); 
        gbc.ipady = 2;
        gbc.insets.top = 10; gbc.insets.bottom = 0;
        //  Size 
        gbc.weightx = .10;
        gbc.insets.left = 10;
        gbc.insets.right = 10; 
        gbc.gridx = 5; gbc.gridy = 0;
        pnl_controls.add(sb_size, gbc); 
        gbc.insets.top = 3; gbc.insets.bottom = 7;
        gbc.ipady = 0;
        gbc.gridy = 1;
        pnl_controls.add(sb_size_lbl, gbc); 
        gbc.insets.top = 10; gbc.insets.bottom = 0;
        //  Middle Buttons
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.025;
        gbc.insets.top = 5;
        gbc.insets.bottom = 5;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.gridx = 2; gbc.gridy = 0;
        pnl_controls.add(bt_start, gbc);
        gbc.gridy = 1;
        pnl_controls.add(bt_create, gbc);
        gbc.gridx = 3; gbc.gridy = 0;
        pnl_controls.add(bt_pause, gbc);
        gbc.gridy = 1;
        pnl_controls.add(bt_destroy, gbc);
        gbc.gridx = 4; gbc.gridy = 0;
        pnl_controls.add(bt_quit, gbc);
        gbc.gridy = 1;
        pnl_controls.add(bt_next, gbc);
        // Attach listeners:
        bt_start.addActionListener(this);
        bt_pause.addActionListener(this);
        bt_quit.addActionListener(this);
        bt_create.addActionListener(this);
        bt_destroy.addActionListener(this);
        bt_next.addActionListener(this);
        sb_tickrate.addAdjustmentListener(this);
        sb_velocity.addAdjustmentListener(this);
        sb_size.addAdjustmentListener(this);
        validate();
    }
    public void destroy_pnl_controls() {
        bt_start.removeActionListener(this);
        bt_pause.removeActionListener(this);
        bt_quit.removeActionListener(this);
        bt_create.removeActionListener(this);
        bt_destroy.removeActionListener(this);
        bt_next.removeActionListener(this);
        sb_tickrate.removeAdjustmentListener(this);
        sb_velocity.removeAdjustmentListener(this);
        sb_size.removeAdjustmentListener(this);
    }
    public void destroy_pnl_screen() {
        screen.removeMouseListener(this);
        screen.removeMouseMotionListener(this);
        bsim.stop();
        screen.stop();
    }
    
    // Start the program + close it:
    public void start() { 
        screen.start();
        bsim.start(); 
        bsim.forcedraw();
    }
    public void stop() {
        destroy_pnl_screen();
        destroy_pnl_controls();
        this.removeComponentListener(this);
        this.removeWindowListener(this);
        dispose();
        System.exit(0);
    }

    // Refresh scrollbar values with bsim gets.
    public void sb_refresh_tickrate() {
        int tps = bsim.sim_get_tickrate();
        sb_tickrate.setValue(Util.relate_bounds(tps, bsim.SIM_TICKRATE_MIN, bsim.SIM_TICKRATE_MAX, sb_tickrate.getMinimum(), sb_tickrate.getMaximum()-sb_tickrate.getVisibleAmount()));
        sb_tickrate_lbl.setText("Tickrate (speed): " + tps + "t/s");
    }
    public void sb_refresh_velocity() {
        double mag = Vec2.magnitude(bsim.body_get_velocity());
        sb_velocity.setValue((int)Math.round(Util.relate_bounds(mag, bsim.BODY_VEL_MIN, bsim.BODY_VEL_MAX, sb_velocity.getMinimum(), sb_velocity.getMaximum()-sb_velocity.getVisibleAmount())));
        sb_velocity_lbl.setText("Velocity: " + Math.round(mag*100)/100.0 + "px/t");
    }
    public void sb_refresh_size() {
        int size = bsim.body_get_size(); 
        sb_size.setValue(Util.relate_bounds(size, bsim.BODY_SIZE_MIN, bsim.BODY_SIZE_MAX, sb_size.getMinimum(), sb_size.getMaximum()-sb_size.getVisibleAmount()));
        sb_size_lbl.setText("Size: " + size*2+1 + "px");
    }

    // Set values/scrolls to the given percent along their MINs/MAXs. 
    public void adjust_tickrate(double percent) {
        percent = Util.restrict_bounds(percent, 0.001, 1);
        int new_tickrate = (int)Math.round(Util.relate_bounds(percent, 0.001, 1, bsim.SIM_TICKRATE_MIN, bsim.SIM_TICKRATE_MAX));
        bsim.sim_set_tickrate(new_tickrate);
        sb_refresh_tickrate();
    }
    public void adjust_velocity(double percent) {
        percent = Util.restrict_bounds(percent, 0.001, 1);
        double magnitude = Util.relate_bounds(percent, 0.001, 1, bsim.BODY_VEL_MIN, bsim.BODY_VEL_MAX);
        // Preserve vector heading.
        Vec2 new_vel = bsim.body_get_velocity(); 
        new_vel.x = Math.signum(new_vel.x);
        new_vel.y = Math.signum(new_vel.y);
        // Adjust velocity magnitude, given the percentage between VEL_MIN and VEL_MAX.
        // (equal components, assuming movement is always perfectly diagonal).
        new_vel.mul(Math.sqrt((magnitude*magnitude/2)));
        bsim.body_set_velocity(new_vel);
        sb_refresh_velocity();
    }
    public void adjust_size(double percent) { 
        percent = Util.restrict_bounds(percent, 0.001, 1);
        int new_size = (int)Math.round(Util.relate_bounds(percent, 0.001, 1, bsim.BODY_SIZE_MIN, bsim.BODY_SIZE_MAX));
        bsim.body_set_size(new_size);
        new_size = bsim.body_get_size(); // Size may have been restricted below SIZE_MAX if object was close to screen edge.
        sb_refresh_size();
    }
    
    // Resize the BufferedCanvas (draw space, canvas image) and BounceSim (bounce space, within red border).
    void resize_screen(Dimension new_size) {
        bsim.resize_space(new_size);
        screen.setSize(new_size);
    }

    // Event handler implementations:
    //  WindowListener
    public void windowClosing(WindowEvent e) { stop(); }
    //  ComponenetListener
    public void componentResized(ComponentEvent e) { resize_screen(pnl_screen.getSize()); }
    //  ActionListener
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == bt_start || source == bt_pause) {
            bsim.set_pause(!bsim.is_paused());
            bt_start.setEnabled(bsim.is_paused());
            bt_pause.setEnabled(!bsim.is_paused());
        } else 
        if (source == bt_create && bsim.num_balls() < bsim.MAX_BALLS) {
            bt_create.setEnabled(bsim.num_balls() < bsim.MAX_BALLS-1);
            bsim.add_ball();
            bsim.forcedraw();
            sb_refresh_velocity();  sb_refresh_size();
            bt_destroy.setEnabled(true);
            bt_next.setEnabled(true);
        } else
        if (source == bt_destroy && bsim.num_balls() > 1) {
            bt_destroy.setEnabled(bsim.num_balls()-1 > 1);  
            bsim.remove_ball();
            bsim.forcedraw();
            sb_refresh_velocity();  sb_refresh_size();
            bt_next.setEnabled(bt_destroy.isEnabled());
            bt_create.setEnabled(true);
        } else
        if (source == bt_next && bsim.num_balls() > 1) {
            bsim.next_ball();
            bsim.forcedraw();
            sb_refresh_velocity();  sb_refresh_size();
        } else
        if (source == bt_quit) {
            stop();
        }
    }
    //  AdjustmentListener
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
            bsim.forcedraw();
        }
    }
    //  MouseListener
    public void mousePressed(MouseEvent e) {
        m1 = e.getPoint();
        screen.set_dragbox(dragbox);
    }
    public void mouseReleased(MouseEvent e) {
        if (!dragbox.isEmpty()) bsim.add_rect(dragbox);
        screen.set_dragbox(null);
        dragbox.setBounds(0,0,0,0);
        bsim.forcedraw();
    }
    public void mouseClicked(MouseEvent e) {
        System.out.println("Mouse Clicked" + e.getClickCount() + " clicks");
    }
    public void mouseEntered(MouseEvent e) { screen.set_dragbox(dragbox); }
    public void mouseExited(MouseEvent e) { screen.set_dragbox(new Rectangle(dragbox)); } // Don't update when mouse dragged off.
    //  MouseMotionListener
    public void mouseDragged(MouseEvent e) {
        m2 = e.getPoint();
        dragbox.setLocation(Math.max(1, Math.min(Math.min(m1.x,m2.x), screen.getWidth()-1)), 
                            Math.max(1, Math.min(Math.min(m1.y,m2.y), screen.getHeight()-1)));
        dragbox.setSize(Math.min(Math.abs(m1.x-m2.x), screen.getWidth()-dragbox.x-1), 
                        Math.min(Math.abs(m1.y-m2.y), screen.getHeight()-dragbox.y-1));
        bsim.forcedraw();
    }
    public void mouseMoved(MouseEvent e) {}
    
    // Unimplemented WindowListener, ComponenetLister:
    public void windowClosed(WindowEvent e) {} public void windowOpened(WindowEvent e) {} public void windowActivated(WindowEvent e) {} public void windowDeactivated(WindowEvent e) {} public void windowIconified(WindowEvent e) {} public void windowDeiconified(WindowEvent e) {}
    public void componentHidden(ComponentEvent e) {} public void componentShown(ComponentEvent e) {} public void componentMoved(ComponentEvent e) {}
    
    public static void main(String[] args) {
        new BouncingBall(new Dimension(900, 600), 120);
    }
}

// Implement the rendering as a seperate class, with a seperate thread.
//  * note: Differs from lesson, but I think it just makes more sense this way.
class BufferedCanvas extends Canvas implements Runnable {
    private int framerate;
    private int frame_delay;

    private Image frontbuff;
    private Image backbuff;
    private Graphics backbuff_g;

    private Rectangle db;
    
    private boolean fl_draw_db;
    private boolean fl_swap;

    private Thread render_thread;
    private boolean render_running;

    public BufferedCanvas(Dimension initial_size, int fps) {
        framerate = fps;
        frame_delay = 1000/framerate;
        setSize(initial_size.getSize());
        setBackground(Color.white);
        frontbuff = null;
        backbuff = null;
        backbuff_g = null;
        render_thread = null;
        render_running = false;
        db = null;
        fl_draw_db = false;
        fl_swap = false;
    }

    public boolean start() {
        if (render_thread == null && isDisplayable()) { // Buffers will fail to create if the canvas is not displayable yet.
            backbuff = createImage(getWidth(), getHeight());
            render_running = true;
            render_thread = new Thread(this);
            render_thread.start();
        }
        return render_running;
    }
    public void stop() {
        if (render_thread != null) {
            render_running  = false;
            render_thread.interrupt();
            render_thread = null;
        }
    }
    public void run() {
        while (render_running) {
            if (fl_swap) {
                if (fl_draw_db) backbuff_g.drawRect((int)db.getX(), (int)db.getY(), (int)db.getWidth(), (int)db.getHeight());
                backbuff_g.dispose();
                frontbuff = backbuff;
                backbuff = createImage(getWidth(), getHeight());
                backbuff_g = null; 
                fl_swap = false;
                repaint();
            }
            try { Thread.sleep(frame_delay); } 
            catch (InterruptedException e) {}
        }
    }

    public Graphics get_backbuff() { 
        Graphics bfg = null;
        if (!fl_swap) {  //backbuff_g == null) { 
            backbuff_g = backbuff.getGraphics();
            bfg = backbuff_g;
        }
        return bfg;
    }
    public void swap() { fl_swap = backbuff != null; }
        // Draw overlay.
        // Assuming backbuffer draws and swap calls are being done sequentially (not on seperate threads),
        // it should be safe to use backbuff_g here (as it's only disposed of when the backbuff is retrieved).
        //backbuff_g.drawRect(overlay_rect.get);  
        
        // Draw the dragbox overlay before swap.
        //if (draw_db) backbuff_g.drawRect((int)db.getX(), (int)db.getY(), (int)db.getWidth(), (int)db.getHeight());
        // Dispose old backbuffer graphics, swap buffers, create new backbuffer and graphics.

    // Override update to just call paint, without clearing, only after buffers are swapped. 
    // Removing the default canvas clearing fixes the terrible white canvas flicker.
    //  * note: Differs from lesson, but the program is unusable without it (on my laptop).
    //          Hopefully the continuous overdraws won't cause any memory issues.
    public void update(Graphics g) { 
        //if (draw_db) {
        //    frontbuff_g = frontbuff.getGraphics();
        //    frontbuff_g.drawRect((int)db.getX(), (int)db.getY(), (int)db.getWidth(), (int)db.getHeight());
        //    frontbuff_g.dispose();
        //}
        //if (swapped || draw_db) {

            paint(g); 
        //    swapped = false;
        //}
    }        
    public void paint(Graphics g) { 
        g.drawImage(frontbuff, 0, 0, null); 
        //if (draw_db) g.drawRect((int)db.getX(), (int)db.getY(), (int)db.getWidth(), (int)db.getHeight());
        //System.out.println(draw_db + " " + db);
        //if (draw_db) g.drawRect((int)db.getX(), (int)db.getY(), (int)db.getWidth(), (int)db.getHeight());
    }

    // Set an overlay rect, to be drawn on top the backbuffer every time it is swapped in for drawing.
    // Intended for mouse dragbox overlay type stuff.

    // Set the overlay dragbox reference.
    // It will be drawn ontop of the front buffer, each frame.
    public void set_dragbox(Rectangle dragbox) {
        fl_draw_db = (dragbox != null);
        if (fl_draw_db) db = dragbox;
    }
}

class BounceSim implements Runnable {
    private static final long serialVersionUID = 11L;

    public static final int SIM_TICKRATE_MIN = 1;
    public static final int SIM_TICKRATE_MAX = 256;
    public static final double BODY_VEL_MIN = 0.01;    // Magnitude, sqrt(x^2 + y^2)
    public static final double BODY_VEL_MAX = 20;
    public static final int BODY_SIZE_MIN = 10;
    public static final int BODY_SIZE_MAX = 200;
    public static final int MAX_BALLS = 100;

    // Looking for window dimensions? They're in the parent class, use getWidth/getHeight/getSize/setSize.

    // The renderer:
    BufferedCanvas renderer;
  
    // Sim settings:
    private int sim_delay;          // Ms delay between ticks, 1000/tickrate.
    private int sim_tickrate; 
    private Thread sim_thread;
    private boolean sim_paused;
    private boolean sim_running;
    private Dimension space_size;
    private Rectangle space_perimeter;

    // Objects:
    private Vector<Ball> balls;
    private Ball selected_ball; // Reference to the selected ball in the vector (for changing velocity/size)
    private Vector<Rectangle> rects;
    private Rectangle new_rect; // Temporary buffer of new rect.

    // Main thread flags. 
    private boolean fl_add_ball;
    private boolean fl_next_ball;
    private boolean fl_remove_ball;
    private boolean fl_force_draw;
    private boolean fl_force_tick;
    private boolean fl_skip_tick;
    private boolean fl_add_rect;

    public BounceSim(Dimension initial_space_size, BufferedCanvas bc) {
        sim_tickrate = SIM_TICKRATE_MIN;
        sim_delay = 1000/sim_tickrate;
        sim_thread = null;
        sim_paused = true;
        sim_running = false;
        space_size = initial_space_size.getSize();  // @todo points instead? no points means no grade points?
        space_perimeter = new Rectangle(new Point(0, 0), space_size);
        space_perimeter.grow(-1,-1);
        balls = new Vector<Ball>();
        balls.addElement(new Ball());
        selected_ball = balls.elementAt(0);
        selected_ball.color = Color.red;
        rects = new Vector<Rectangle>();
        fl_add_ball = false;
        fl_next_ball = false;
        fl_remove_ball = false;
        fl_force_draw = false;
        fl_force_tick = false;
        fl_skip_tick = false;
        fl_add_rect = false;
        renderer = bc;
    }

    // Set the BounceCanvas
    public void set_renderer(BufferedCanvas bc) { renderer = bc; }
   
    // Starting/stopping the running thread. 
    public boolean start() {
        if (sim_thread == null && renderer != null) {
            if (renderer.start()) {
                sim_running = true;
                sim_thread = new Thread(this);
                sim_thread.start();
            }
        } 
        return sim_running;
    }
    public void stop() {
        if (sim_thread != null) {
            sim_running = false;
            sim_thread.interrupt();
            sim_thread = null;
        }
        renderer.stop();
    }

    public void run() {
        while (sim_running) {
            if (fl_add_rect) {
                boolean addit = true;
                for (int rectdex = 0; rectdex < rects.size(); rectdex++) {
                    if (new_rect.contains(rects.elementAt(rectdex))) {
                        rects.removeElementAt(rectdex);
                        rectdex--;
                    } else if (rects.elementAt(rectdex).contains(new_rect)) {
                        addit = false;
                        rectdex = rects.size();
                    } 
                }
                if (addit) rects.addElement(new_rect);
            }


            // Control the adding, removing, and switching of balls.
            if (fl_add_ball && balls.size() < MAX_BALLS) {
                Ball new_ball = new Ball();
                balls.addElement(new_ball);
                fl_next_ball = true;
                fl_add_ball = false;
            }
            if (fl_remove_ball && balls.size() > 1) {   // Balllessness is unsupported. Never allow zero balls.
                Ball old_selected = selected_ball;
                if (balls.elementAt(0) == old_selected) selected_ball = balls.lastElement();
                else selected_ball = balls.elementAt(balls.indexOf(old_selected)-1);
                balls.removeElement(old_selected);
                selected_ball.color = Color.red;
                fl_remove_ball = false;
            }
            if (fl_next_ball && balls.size() > 1) {
                selected_ball.color = Color.lightGray;
                if (balls.lastElement() == selected_ball) selected_ball = balls.firstElement();
                else selected_ball = balls.elementAt(balls.indexOf(selected_ball)+1);
                selected_ball.color = Color.red;
                fl_next_ball = false;
            }
           
            // Stepping of the simulation.  
            if (!sim_paused || fl_force_tick) {
                if (!fl_skip_tick) {
                    fl_force_tick = false;
                    for (Ball ball : balls) {
                        Vec2 next_pos = Vec2.add(ball.pos, ball.vel);
                        // Screen collision detection: 
                        if (next_pos.x < 1+ball.size+1) { 
                            next_pos.x = 1+ball.size+1; 
                            ball.vel.x = -ball.vel.x; 
                        } else if (next_pos.x > renderer.getWidth()-ball.size-1-1) { 
                            next_pos.x = renderer.getWidth()-ball.size-1-1; 
                            ball.vel.x = -ball.vel.x; 
                        }
                        if (next_pos.y < 1+ball.size+1) { 
                            next_pos.y = 1+ball.size+1; 
                            ball.vel.y = -ball.vel.y; 
                        } else if (next_pos.y > renderer.getHeight()-ball.size-1-1) { 
                            next_pos.y = renderer.getHeight()-ball.size-1-1; 
                            ball.vel.y = -ball.vel.y; 
                        }
                        // Should we bother with ball on ball collisions?
                        // If we do, we'll need to do proper spherical collisions to get the right normals.
                        // So we can't use the rect.collides. That might have a bonus of better corner collisions with the balls on rects. I think.
                        ball.pos = next_pos;
                    }
                } else fl_skip_tick = false;
            }

            if (!sim_paused || fl_force_draw) {
                fl_force_draw = false;
                draw();
                try { Thread.sleep(sim_delay-1); }                          //  ! jank
                catch (InterruptedException e) {                            // Interrupting the thread to draw will cause the next tick to run prematurely (to draw again at the end of it).
                    fl_skip_tick = fl_force_draw && (sim_tickrate < 48);    // This has the effect of speeding up the simulation, though it's only really noticable at lower tickrates.
                    fl_skip_tick = fl_skip_tick && !fl_force_tick;          // To combat this, skip the next tick when fl_force_draw was set and the tickrate is below some arbitrary "low" value.
                                                                            //  * note that this also causes the inverse problem of slowing down the simulation, but who cares.
                                                                            //  * also if a force_tick is desired, actually don't skip.
                }                                                       
            } else {
                try { Thread.sleep(1); } // To update sim_pause on interrupts, if paused.
                catch (InterruptedException e) {}
            }
        }
    }

    // Force a tick of the simulation, for instant updates of size/velocity/balls.
    public void forcetick() { 
        if (sim_running) {
            fl_force_tick = true;
            sim_thread.interrupt(); 
        }
    }
    
    // Adding, removing, switching balls. Only once per tick.
    public int num_balls() { return balls.size(); }
    public void add_ball() { fl_add_ball = true; }
    public void remove_ball() { fl_remove_ball = true; }
    public void next_ball() { fl_next_ball = true; }

    public void add_rect(Rectangle rect) { new_rect = new Rectangle(rect); fl_add_rect = true; }
   
    // Accessing space size. 
    public Dimension get_space_size() { return space_size.getSize(); }
    public void resize_space(Dimension new_size) { 
        space_size = new_size.getSize();
        try {
            for (Ball ball : balls) {
                ball.pos.x = Util.restrict_bounds(ball.pos.x, ball.size+1, space_size.getWidth()-ball.size-1);
                ball.pos.y = Util.restrict_bounds(ball.pos.y, ball.size+1, space_size.getHeight()-ball.size-1);
                forcedraw();
            }
        } catch (java.util.ConcurrentModificationException e) {}    // Can very rarely occur if you resize the window madly while adding/removing balls.
                                                                    // Unavoidable, unless we do this checking in the thread. Then we face the issue of 
                                                                    // low tickrates letting things get out of bounds before the next tick.
                                                                    // Accesses to balls should really only be done in run(), but so it goes.
    }
   
    // Pausing/playing. 
    public boolean is_paused() { return sim_paused; }
    public void set_pause(boolean p) { 
        if (sim_paused != p) {
            sim_paused = p; 
            sim_thread.interrupt();
        }
    }
    
    // Internal, only call once within simulation thread.
    private void draw() {
        Graphics bfg = renderer.get_backbuff();
        if (bfg != null) {
            bfg.setColor(Color.red);
            bfg.drawRect(0, 0, (int)space_size.getWidth()-1, (int)space_size.getHeight()-1);

            for (Rectangle rect : rects) {
                bfg.setColor(Color.black);
                bfg.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
            
            int tl_x, tl_y;
            for (Ball ball : balls) {
                if (ball == selected_ball) continue;
                tl_x = (int)Math.round(ball.pos.x-1-ball.size);   
                tl_y = (int)Math.round(ball.pos.y-1-ball.size);
                bfg.setColor(ball.color);   bfg.fillOval(tl_x, tl_y, ball.size*2+1, ball.size*2+1); 
                bfg.setColor(Color.black);  bfg.drawOval(tl_x, tl_y, ball.size*2+1, ball.size*2+1);
            }
            tl_x = (int)Math.round(selected_ball.pos.x-1-selected_ball.size);   // Always draw selected on top. @todo if we add ball on ball collisions, this becomes unnecessary.
            tl_y = (int)Math.round(selected_ball.pos.y-1-selected_ball.size);
            bfg.setColor(selected_ball.color);  bfg.fillOval(tl_x, tl_y, selected_ball.size*2+1, selected_ball.size*2+1); 
            bfg.setColor(Color.black);          bfg.drawOval(tl_x, tl_y, selected_ball.size*2+1, selected_ball.size*2+1);
        }
        renderer.swap();
    }
    // External, for forcing a draw while the thread is paused/blocked.
    public void forcedraw() { 
        if (sim_running) {
            fl_force_draw = true;
            sim_thread.interrupt(); 
        }
    }

    // Exposed settings for simulation tickrate, ball velocity, and ball size.
    public void sim_set_tickrate(int tps) {
        sim_tickrate = Util.restrict_bounds(tps, SIM_TICKRATE_MIN, SIM_TICKRATE_MAX);
        sim_delay = (int)Math.round(1000/sim_tickrate);
    }
    public void body_set_velocity(Vec2 new_vel) {       // It really would be better not to expose velocity directly (and modify only through applied forces), but I don't really care that much.
        selected_ball.vel.x = new_vel.x; selected_ball.vel.y = new_vel.y;
    }
    public void body_set_size(int px) {
        Ball ball = selected_ball;
        int next_size = Util.restrict_bounds(px, BODY_SIZE_MIN, BODY_SIZE_MAX);
        // Restrict within screen bounds from current position:
        //Util.restrict_bounds( should use restrict bounds here.
        if ((ball.pos.x+next_size+1) >= space_size.getWidth()-1) next_size = (int)(space_size.getWidth()-ball.pos.x-2);
        else if ((ball.pos.x-next_size-1) <= 1) next_size = (int)(ball.pos.x-2);
        if ((ball.pos.y+next_size+1) >= space_size.getHeight()-1) next_size = (int)(space_size.getHeight()-ball.pos.y-2);
        else if ((ball.pos.y-next_size-1) <= 1) next_size = (int)(ball.pos.y-2);
        ball.size = next_size;
    }
    public int sim_get_tickrate() { return sim_tickrate; }
    public Vec2 body_get_velocity() { return (new Vec2(selected_ball.vel)); }
    public int body_get_size() { return selected_ball.size; }

    // The balls that bounce.
    class Ball { 
        public int size; 
        public Vec2 pos;    // * Keeping Vec2's instead of changing to points, decimal (sub-pixel) precision is desired for smoothest movement.
        public Vec2 vel; 
        Color color; 
        // Randomized constructor.
        public Ball() {
            size = (int)Util.relate_bounds(Math.random(), 0.0, 1.0, BODY_SIZE_MIN, BODY_SIZE_MAX); 
            
            // Will need to make sure this doesn't collide with anything. @todo 
            double x = Util.relate_bounds(Math.random(), 0.0, 1.0, size+1, space_size.getWidth()-size-1);   
            double y = Util.relate_bounds(Math.random(), 0.0, 1.0, size+1, space_size.getHeight()-size-1);  // There's probably some cute math function for normalization we should be using instead of relate_bounds.
            pos = new Vec2(x, y);

            // Solutions for finding the free space:
            //  - Pick a random rectangle, move off to the side a litte bit, and check the max space until it hits another rectangle. Pick that middle position, with a size slightly smaller than the distance in between.
            //  - Some kind of data structure for spatial information? Would a BSP tree be applicable here? 
            
            double v = Util.relate_bounds(Math.random(), 0.0, 1.0, BODY_VEL_MIN, BODY_VEL_MAX);
            vel = Vec2.mul(new Vec2(1,1), Math.sqrt(v*v/2));
            
            color = Color.lightGray;
        }
    }
} 

// Two dimensional vector, for velocity and position data.
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
    public static double magnitude(Vec2 v) {
        return Math.sqrt(v.x*v.x+v.y*v.y);
    }
}

// @todo there are probably better methods from Math that we can use instead of these.
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
        return (int)Math.round(relate_bounds((double)num, low1, up1, low2, up2));   // Requires floating point calculation, will return 1 otherwise.
    }
    public static double relate_bounds(double num, double low1, double up1, double low2, double up2) {
        return low2 + ((up2-low2) * ((num-low1)/(up1-low1)));
    }
}

