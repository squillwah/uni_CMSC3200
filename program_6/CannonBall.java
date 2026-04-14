
package CannonBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.awt.image.VolatileImage;    
// https://docs.oracle.com/javase/8/docs/api/java/awt/image/VolatileImage.html
// Hardware accelerated images, uses GPU/VRAM for buffer drawing/storage.
// For major performance benefit with multiple transparent buffer layers.
// Requires additional management (see RenderComposer.vi_validate()), as VRAM makes no promises.


import java.util.Vector;


// ----------------
// The Main Class
// Creates the frame + UI elements, game engine and canvas display.
// Manages interaction between UI bits/controls and game state.
// ----------------
public class CannonBall implements ActionListener, AdjustmentListener, ComponentListener, ItemListener, Runnable, WindowListener {
    private static final long serialVersionUID = 1111L;
    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    // Offsets for MenuItem and value arrays.
    private final byte run     = 0, pause = 1, restart = 2, quit  = 3, NUM_CONTROLS = 4; 
    private final byte xsmall  = 0, small = 1, medium  = 2, large = 3, xlarge  = 4, NUM_SIZES    = 5;
    private final byte xslow   = 0, slow  = 1, normal  = 2, fast  = 3, xfast   = 4, NUM_SPEEDS   = 5;
    private final byte mercury = 0, venus = 1, earth   = 2, mars  = 3, jupiter = 4, saturn = 5, uranus = 6, neptune = 7, pluto = 8, NUM_PLANETS = 9;
    private final byte nodebug = 0, db1 = 1, db2 = 2, db3 = 3, NUM_DEBUG_LEVELS = 4;
    private final byte fertileballs = 0, drawhitboxes = 1, bubblegravity = 2, NUM_EXTRAS = 3;
    // Parallel MenuItem value arrays.
    private final double SIZES[] = {.5, 1, 1.5, 2.5, 4};                                                // Radius in meters (expanding from a single center point pixel).
    private final double SPEEDS[] = {1, 2, 3, 5, 8};                                                    // Meters per second (applied equally to both components).
    private final double GRAVITIES[] = {3.7, 8.87, 9.80665, 3.71, 24.79, 10.4, 8.87, 11.15, 0.620};     // Meters per second per second. (Pixel -> Meter ratio is defined in Engine)

    // Frame and panels.
    private Frame window;
    private Panel pnl_display, pnl_controls;
    // The game logic, drawing system, thread.
    private CannonBallEngine engine;
    private MultiBufferedCanvas display;
    private Thread main_thread;
    private boolean main_thread_running;
    // Elements of UI: MenuItems, ScrollBars, Labels.
    private MenuBar menubar;
    private Menu mnu_control, mnu_parameters, mnu_environment, mnu_parameters_mnu_size, mnu_parameters_mnu_speed, mnu_debuginfo, mnu_extras;
    private MenuItem[] mnu_control_itms;                        
    private CheckboxMenuItem[] mnu_parameters_mnu_size_itms;    
    private CheckboxMenuItem[] mnu_parameters_mnu_speed_itms;   
    private CheckboxMenuItem[] mnu_environment_itms;
    private CheckboxMenuItem[] mnu_debuginfo_itms;
    private CheckboxMenuItem[] mnu_extras_itms;
    private Label lbl_cannon_force, lbl_cannon_angle, lbl_score_ball, lbl_score_player, lbl_time;   
    private Scrollbar sb_cannon_force, sb_cannon_angle;                                                 

    public static void main(String[] args) { new CannonBall(); }

    public CannonBall() {
        // Engine, Display, Thread:
        engine = new CannonBallEngine();
        display = new MultiBufferedCanvas(engine.renderer());
        main_thread = null;
        main_thread_running = false;
        // Frame:
        window = new Frame();
        window.setTitle("CannonBubbles");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setBackground(Color.black);
        window.setLayout(new BorderLayout());
        // Panels:
        pnl_display = (Panel)window.add("Center", (new Panel()));  
        pnl_display.setBackground(Color.gray);
        pnl_display.setLayout(new BorderLayout());
        pnl_controls = (Panel)window.add("South", (new Panel()));
        pnl_controls.setBackground(new Color(158, 137, 79));
        pnl_controls.setLayout(new GridBagLayout());
        // Menubar, MenuItems: 
        menubar                                 = new MenuBar();
        mnu_control                             = menubar.add(new Menu("Control"));
        mnu_control_itms                        = new MenuItem[4];
        mnu_control_itms[run]                   = mnu_control.add(new MenuItem("Run", new MenuShortcut(KeyEvent.VK_R)));
        mnu_control_itms[pause]                 = mnu_control.add(new MenuItem("Pause", new MenuShortcut(KeyEvent.VK_P)));
        mnu_control_itms[restart]               = mnu_control.add(new MenuItem("Restart", new MenuShortcut(KeyEvent.VK_P, true))); 
        mnu_control.addSeparator();
        mnu_control_itms[quit]                  = mnu_control.add(new MenuItem("Quit", new MenuShortcut(KeyEvent.VK_Q, true)));
        mnu_parameters                          = menubar.add(new Menu("Parameters"));
        mnu_parameters_mnu_size                 = (Menu)mnu_parameters.add(new Menu("Size"));
        mnu_parameters_mnu_size_itms            = new CheckboxMenuItem[NUM_SIZES];
        mnu_parameters_mnu_size_itms[xsmall]    = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("xsmall"));
        mnu_parameters_mnu_size_itms[small]     = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("small"));
        mnu_parameters_mnu_size_itms[medium]    = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("medium"));
        mnu_parameters_mnu_size_itms[large]     = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("large"));
        mnu_parameters_mnu_size_itms[xlarge]    = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("xlarge"));
        mnu_parameters_mnu_speed                = (Menu)mnu_parameters.add(new Menu("Speed"));
        mnu_parameters_mnu_speed_itms           = new CheckboxMenuItem[NUM_SPEEDS];
        mnu_parameters_mnu_speed_itms[xslow]    = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xslow"));
        mnu_parameters_mnu_speed_itms[slow]     = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("slow"));
        mnu_parameters_mnu_speed_itms[normal]   = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("normal"));
        mnu_parameters_mnu_speed_itms[fast]     = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("fast"));
        mnu_parameters_mnu_speed_itms[xfast]    = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xfast"));
        mnu_environment                         = menubar.add(new Menu("Environment"));
        mnu_environment_itms                    = new CheckboxMenuItem[NUM_PLANETS];
        mnu_environment_itms[mercury]           = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Mercury")); 
        mnu_environment_itms[venus]             = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Venus"));
        mnu_environment_itms[earth]             = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Earth"));
        mnu_environment_itms[mars]              = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Mars"));
        mnu_environment_itms[jupiter]           = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Jupiter"));
        mnu_environment_itms[saturn]            = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Saturn"));
        mnu_environment_itms[uranus]            = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Uranus"));
        mnu_environment_itms[neptune]           = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Neptune"));
        mnu_environment_itms[pluto]             = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("PLUTO"));
        mnu_debuginfo                           = menubar.add(new Menu("Info"));
        mnu_debuginfo_itms                      = new CheckboxMenuItem[NUM_DEBUG_LEVELS];
        mnu_debuginfo_itms[nodebug]             = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("none"));
        mnu_debuginfo_itms[db1]                 = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("level 1"));
        mnu_debuginfo_itms[db2]                 = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("level 2"));
        mnu_debuginfo_itms[db3]                 = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("level 3"));
        mnu_extras                              = menubar.add(new Menu("Extras"));
        mnu_extras_itms                         = new CheckboxMenuItem[NUM_EXTRAS];
        mnu_extras_itms[fertileballs]           = (CheckboxMenuItem)mnu_extras.add(new CheckboxMenuItem("fertile balls"));
        mnu_extras_itms[drawhitboxes]           = (CheckboxMenuItem)mnu_extras.add(new CheckboxMenuItem("draw hitboxes"));
        mnu_extras_itms[bubblegravity]          = (CheckboxMenuItem)mnu_extras.add(new CheckboxMenuItem("bubble gravity"));
        // Conpan Scrolls, Labels, add to Panels:
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1;   gbc.ipady = 2; gbc.insets = new Insets(10,10,0,10); sb_cannon_force = new Scrollbar(Scrollbar.HORIZONTAL);      pnl_controls.add(sb_cannon_force, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1;   gbc.ipady = 1; gbc.insets = new Insets(0,10,5,10);  lbl_cannon_force = new Label("Force: ?px/s", Label.CENTER); pnl_controls.add(lbl_cannon_force, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = .5;  gbc.ipady = 1; gbc.insets = new Insets(5,10,0,0);   lbl_score_ball = new Label("Bubble: ", Label.CENTER);       pnl_controls.add(lbl_score_ball, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = .75; gbc.ipady = 1; gbc.insets = new Insets(0,10,5,10);  lbl_time = new Label("Time: ?s", Label.CENTER);             pnl_controls.add(lbl_time, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = .5;  gbc.ipady = 1; gbc.insets = new Insets(5,0,0,10);   lbl_score_player = new Label("Player: ", Label.CENTER);     pnl_controls.add(lbl_score_player, gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1;   gbc.ipady = 2; gbc.insets = new Insets(10,10,0,10); sb_cannon_angle = new Scrollbar(Scrollbar.HORIZONTAL);      pnl_controls.add(sb_cannon_angle, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1;   gbc.ipady = 1; gbc.insets = new Insets(0,10,5,10);  lbl_cannon_angle = new Label("Angle: ?deg", Label.CENTER);  pnl_controls.add(lbl_cannon_angle, gbc);
        sb_cannon_force.setBackground(pnl_controls.getBackground().darker());  
        sb_cannon_angle.setBackground(pnl_controls.getBackground().darker());
        sb_cannon_angle.setMinimum(0); sb_cannon_angle.setMaximum(1000); sb_cannon_angle.setVisibleAmount(100); // 1000-100 == 90.0 degrees
        sb_cannon_angle.setBlockIncrement(45); sb_cannon_angle.setUnitIncrement(9); 


        // Add panels to frame, display to panel:
        window.setMenuBar(menubar);
        window.add("Center", pnl_display);
        window.add("South", pnl_controls);
        pnl_display.add("Center", display);
        // Set radio defaults
        mnu_control_itms[pause].setEnabled(false);
        set_mradio(mnu_parameters_mnu_size_itms, NUM_SIZES, medium);
        set_mradio(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, normal);
        set_mradio(mnu_environment_itms, NUM_PLANETS, earth);
        set_mradio(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, nodebug);
        engine.set_gravity(GRAVITIES[earth]);
        engine.set_bubble_size(SIZES[medium]);
        engine.set_bubble_speed(SPEEDS[normal]);
        display.debug_lvl = 0;
        // Attach Listeners  
        for (MenuItem mi : mnu_control_itms) mi.addActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_debuginfo_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_extras_itms) mi.addItemListener(this);
        sb_cannon_angle.addAdjustmentListener(this);
        sb_cannon_force.addAdjustmentListener(this);
        window.addWindowListener(this);
        window.addComponentListener(this);
        display.addMouseListener(engine);   // Mouse handling done in Engine.
        display.addMouseMotionListener(engine);
        // Start
        window.validate();
        window.setVisible(true);
        start_thread();
    }


    private void exit() {
        display.removeMouseListener(engine);
        display.removeMouseMotionListener(engine);
        sb_cannon_angle.removeAdjustmentListener(this);
        sb_cannon_force.removeAdjustmentListener(this);
        for (MenuItem mi : mnu_control_itms) mi.removeActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_debuginfo_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_extras_itms) mi.removeItemListener(this);
        window.removeWindowListener(this);
        window.removeComponentListener(this);
        
        stop_thread();
        window.dispose();
        System.exit(0);
    }
    
    private void start_thread() {
        if (main_thread == null) {
            main_thread_running = true;
            main_thread = new Thread(this);
            main_thread.start();
        }
    }
    private void stop_thread() {
        if (main_thread != null) {
            main_thread_running = false;
            main_thread.interrupt();
            main_thread = null;
        }
    }

    public void run() {
        int paintlimiter = 0; 
        double delta_t = 0;
        long frame_start_t = System.nanoTime();
        while(main_thread_running) {
            delta_t = (System.nanoTime() - frame_start_t) / 1000000000.0; // Seconds.
            frame_start_t = System.nanoTime();
            //System.out.println(delta_t + " " + frame_start_t);

            // Keep scrolls, score, and time in sync.
            sb_cannon_angle.setValue((int)(engine.get_cannon_angle()*10));
            lbl_cannon_angle.setText("Angle: " + sb_cannon_angle.getValue()/10.0 + "deg");    // @todo May instead want to have some connect_cannon_angle_scroll or connect_player_score_lbl thing in engine.
            
            lbl_time.setText("Time: " + ((int)(engine.get_elapsed_time()*10))/10.0);


            engine.tick(delta_t);
            lbl_score_player.settext("Player: " + engine.get_player_score());
            lbl_score_ball.settext("Ball: " + engine.get_ball_score());
            display.debug_inform_ticktime(delta_t);
           
            if (paintlimiter > 2) {     // Dunno if actually helps anything.
                display.repaint();
                paintlimiter = 0;
            } paintlimiter++;

            try { Thread.sleep(4); }   // sleep(1000), sleep(0, 500000)
            catch (InterruptedException e) {}
        }
    }


    public void adjustmentValueChanged(AdjustmentEvent e) {
        Object bar = e.getSource();
        if (bar == sb_cannon_angle) {
            engine.set_cannon_angle(sb_cannon_angle.getValue()/10.0);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object item = e.getSource();
        switch (find_mitem(mnu_control_itms, NUM_CONTROLS, item)) {
            case -1: break;
            case run:
            case pause:
                engine.set_pause(!engine.is_paused());
                mnu_control_itms[run].setEnabled(engine.is_paused());
                mnu_control_itms[pause].setEnabled(!engine.is_paused());
                break;
            case restart:
                engine.restart();
                mnu_control_itms[run].setEnabled(true);     // Assuming restart always pauses. @todo may want to change this depending on game feel. Or rubric demands.
                mnu_control_itms[pause].setEnabled(false);
                break;
            case quit:
                window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING)); // A place for everything.
                break;
            default: System.out.println("err: bad control menu item offset, can't match: " + item); break;
        }
    }

    // Return index of object in CheckboxMenuItem array. -1 if absent.
    private int find_mitem(MenuItem[] items, int size, Object item) {
        int i;
        for (i = 0; i < size && item != items[i]; i++);
        if (!(i < size)) i = -1;
        return i;
    }

    // Tick radio on and clear all others in array. Using the offset constant as the 'radio' itself.
    private void set_mradio(CheckboxMenuItem[] radios, int size, int radio) {
        int i;
        for (i = 0; i < radio; i++) radios[i].setState(false);
        for (i = radio+1; i < size; i++) radios[i].setState(false);
        radios[radio].setState(true); // Of course assuming radio is in radios.
    }
  
    public void itemStateChanged(ItemEvent e) {
        Object item = e.getSource();
        int radio;
        if ((radio = find_mitem(mnu_parameters_mnu_size_itms, NUM_SIZES, item)) > -1) {
            set_mradio(mnu_parameters_mnu_size_itms, NUM_SIZES, radio);
            engine.set_bubble_size(SIZES[radio]); 
        } else 
        if ((radio = find_mitem(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, item)) > -1) {
            set_mradio(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, radio);
            engine.set_bubble_speed(SPEEDS[radio]);
        } else 
        if ((radio = find_mitem(mnu_environment_itms, NUM_PLANETS, item)) > -1) {
            set_mradio(mnu_environment_itms, NUM_PLANETS, radio);
            engine.set_gravity(GRAVITIES[radio]);
            // @todo if we want different colors or render settings (or any other special stuff) for planets, either use a switch (check previous commits) or more parallel arrays.
        } else
        if ((radio = find_mitem(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, item)) > -1) { 
            set_mradio(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, radio);
            display.debug_lvl = radio;  // It so happens that the debug level and MenuItem offset align. Do not rely on this, will cause issues if that's changed.
        } else 
        if ((radio = find_mitem(mnu_extras_itms, NUM_EXTRAS, item)) > -1) {
            switch (radio) {
                case fertileballs: engine.set_m_fertileballs(mnu_extras_itms[radio].getState()); break;
                case drawhitboxes: engine.set_m_drawhitboxes(mnu_extras_itms[radio].getState()); break;
                case bubblegravity: engine.set_m_bubblegravity(mnu_extras_itms[radio].getState()); break;
            }
        }
    }
    
    public void windowOpened(WindowEvent e) { 
        switch ((int)(Math.random()/.25)) {
            case 0: System.out.println("Would you like to play a game?"); break;
            case 1: System.out.println("Life? Don't talk to me about life."); break;
            case 2: System.out.println("Hello there."); break;
            case 3: System.out.println(":-)"); break;
            default: System.out.println("Not in the mood. Go away."); exit(); break;
        }
    }
    public void windowClosing(WindowEvent e) { 
        switch ((int)(Math.random()/.25)) {
            case 0: System.out.println("So soon?"); break;
            case 1: System.out.println("We'd only just begun..."); break;
            case 2: System.out.println("Come back!"); break;
            case 3: System.out.println("Daisy..  Daisy..."); break;
            default: System.out.println("Bagh! Good riddance I say..."); break;
        }
        exit(); 
    }
    //public boolean was_paused = false;  // @todo Have pause on unfocus/iconification be an option in menubar.
    public void windowActivated(WindowEvent e) {} //engine.set_pause(was_paused); } 
    public void windowDeactivated(WindowEvent e) {} //was_paused = engine.is_paused(); engine.set_pause(true); } 
    public void windowIconified(WindowEvent e) {} 
    public void windowDeiconified(WindowEvent e) {}
    public void componentResized(ComponentEvent e) { 
        // To account for the window border, the engine's Renderer creates buffers +2 pixels larger than world_size on each axis.
        // So, to keep the images in the canvas, shrink the world size -2 pixels on each axis.
        engine.set_world_size(new Dimension(pnl_display.getWidth()-2, pnl_display.getHeight()-2));   
    }
    
    // Unimplemented WindowListener, ComponenetLister:
    public void windowClosed(WindowEvent e) {} 
    public void componentHidden(ComponentEvent e) {} public void componentShown(ComponentEvent e) {} public void componentMoved(ComponentEvent e) {}
}

// ----------------
// The Game. 
// Holds data for objects in the world, does the physics and collisions on tick().
// Also handles mouse interactions with the game.
// ----------------
class CannonBallEngine implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 2222L;
    private final double PIXELS_PER_METER = 10;    // ! This also acts as a "zoom", changing the size of everything.
    private final Dimension MIN_WORLD_SIZE = new Dimension(256, 256);
    
    // double cannon_x = 60, cannon_y = 60;
    // double cannon_angle_deg = 45; // degrees only for method interface, internally stored in radians
    // double cannon_force_val = 200; // pixels per second, for both components. will be converted to mps 
    
    // double ball_x, ball_y;
    // double vx, vy;
    // boolean fired = false;

    // public void set_cannon_angle(double angle) {
    //     cannon_angle_deg = angle;
    // }
    // public void set_cannon_force(double force) {
    //     cannon_force_val = force;}
    // }
    // public void fire() {
    //     if (!fired) {
    //     double angle_rad = Math.toRadians(cannon_angle_deg);
    //     ball_x = cannon_x;
    //     ball_y = world_size[0].height - 50; 
    //     vx = cannon_force_val * Math.cos(angle_rad);
    //     vy = -cannon_force_val * Math.sin(angle_rad); // - because screen y increases downwards
    //     fired = true;
    // }
    // }

    // The Renderer
    private CannonBallRenderer r;
    
    // Controlling tick()
    private boolean physics_paused; 
    private boolean restart_game;
    
    // Mode settings
    private boolean m_fertileballs;
    private boolean m_drawhitboxes;
    private boolean m_bubblegravity;

    // Mousery
    //private Point m1, m2;
    //private MouseEvent mouseclick = {null, null}
    //private Rectangle[] drawrect = {null, null}
    //private Rectangle[] addrect = {null, null}
    //if (mouseclick[0] != mouseclick[1])
    //    mouseclick[0] = mouseclick[1];
    //    switch (mouseclick.getKEY) {
    //        case rightckic:
    //            // do box delete check
    //        case leftclic: 
    //            // do cannon fire check
    //
    //if (addrect[0] != addrect[1])
    //    addrect[0] = addrect[1];
    //    // Do the adding rect shit

    //if (dragbox[0] != dragbox[1])
    //    dragbox[0] = dragbox[1];
    //    r.redraw(r.l_dragrect); // Renderer will handle if it's a null and not draw. Import this will only update draw rect if it changes size or disappears (becomes null).

    //mousePressed(MouseEvent e) { m1 = e.getPoint(); }
    //mouseReleased(MouseEvent e) { 
    //    drawRect[1] = null; 
    //}
    //mouseDragged(MouseEvent e) {
    //    m2 = e.getPoint();
    //    dragbox[1] = new Rectangle();//m1.x, m1.y, 
    //    dragbox[1].setLocation(Math.max(1, Math.min(Math.min(m1.x,m2.x), r.rex_y())), 
    //                           Math.max(1, Math.min(Math.min(m1.y,m2.y), r.res_y())));
    //    dragbox[1].setSize(Math.min(Math.abs(m1.x-m2.x), r.res_x()-dragbox[1].x), 
    //                       Math.min(Math.abs(m1.y-m2.y), r.res_y()-dragbox[1].y));

    //private Rectangle m1m2rect(Point mp1, Point mp2) {
    //    rect = new Rectangle();//m1.x, m1.y, 
    //    dragbox[1].setLocation(Math.max(1, Math.min(Math.min(m1.x,m2.x), r.rex_y())), 
    //                           Math.max(1, Math.min(Math.min(m1.y,m2.y), r.res_y())));
    //    dragbox[1].setSize(Math.min(Math.abs(m1.x-m2.x), r.res_x()-dragbox[1].x), 
    //                       Math.min(Math.abs(m1.y-m2.y), r.res_y()-dragbox[1].y));
    //    return
    //}


    //public void mouseClicked(MouseEvent e) {
    //    mouse_click[1] = e;
    //    e_mouseclick = true; 
    //    //if (e.getButton() == MouseEvent.BUTTON3) {
    //    //    bsim.remove_rect_at_point(e.getPoint());
    //    //    bsim.forcedraw();
    //    //}
    //}
    //public void mousePressed(MouseEvent e) {
    //    mousepoints[1][0] = e.getPoint();
    //    e_mousepressed = true;
    //}
    //public void mouseDragged(MouseEvent e) {
    //    mousepoints[1][1] = e.getPoint();
    //    e_mousedragged = true;
    //}
    //public void mouseReleased(MouseEvent e) { drawRect[1] = null; }
    //public void mouseEntered(MouseEvent e) {} 
    //public void mouseExited(MouseEvent e) {} 
    //public void mouseMoved(MouseEvent e) {}


    //private void add_rect(Rectangle); // Adds to world.
    //private void del_rec_at(Point);   // Removes rect under point


    // Game Data
    // Because events (which change engine values) can occur concurrently with the Thread (and thus during a tick(), which is no good),
    // each externally mutable engine data value gets a buddy. The first value is updated to match the second at each tick start.
    //private MouseEvent[] mouse_click;
    //private Rectangle[] draw;
    private double[] cannon_force;      
    private double[] cannon_angle;      // In radians. Degrees only for method interface.
    private double[] bubble_size;       // Pixels (meter -> pixel conversion is done on set_...)
    private double[] bubble_speed;      // Pixels per second (using the delta_t passed to tick() to approximate seconds)
    private double[] world_gravity;     // Pixels per second per second. (Meter conversion is done in set_...)
    private Dimension[] world_size;     // Still in pixels. *Note: the CannonBallRenderer's canvas is always +2 bigger on each axis (for the border).
    private Rectangle world_perim;
    private int score_player;
    private int score_bubbles;
    private double elapsed_time;        // Time game is not paused. Resets on restart.
    private double timer_fertileballs;
   
    // Game Objects:
    private Cannon can; 
    private Vector<Bubble> bubbs;   
    //private Vector<Balloid> balls;  // Like from the cannon.
    private Vector<Rectangle> rects;

    private Point m1, m2;
    private Rectangle dragbox; // Used by Renderer, as the hollow drag box.
    //private Rectangle addbox;  // Used by Game, to add a rect. Dragbox is moved into addbox and nulled on MouseExited
                               // Use isEmpty to check it.
    private Rectangle addbox[] = new Rectangle[2];

    public void mousePressed(MouseEvent e) { 
        dragbox = new Rectangle();
        m1 = e.getPoint(); 
        System.out.println(m1); 
    }
    public void mouseDragged(MouseEvent e) { 
        m2 = e.getPoint(); 
        // Dragbox may become null if mouse moves off screen.
        if (dragbox != null) {
            dragbox.setLocation(Math.max(1, Math.min(Math.min(m1.x,m2.x), r.res_x())), 
                                Math.max(1, Math.min(Math.min(m1.y,m2.y), r.res_y())));
            dragbox.setSize(Math.min(Math.abs(m1.x-m2.x), r.res_x()-dragbox.x), 
                            Math.min(Math.abs(m1.y-m2.y), r.res_y()-dragbox.y));
        }
        r.redraw(r.l_dragbox);
        System.out.println(m1 + " | " + m2); 
        System.out.println(dragbox);
    }
    
    public void mouseReleased(MouseEvent e) {
        addbox[1] = dragbox;    // Compare the objects to see if changed in tick()
        dragbox = null;
        r.redraw(r.l_dragbox);  // Renderer will know what to do with a null dragbox.
        System.out.println(addbox[1]);
    }
    public void mouseExited(MouseEvent e) { dragbox = null; } // Avoid weirdness.

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public CannonBallEngine() {
        //mouse_click = new MouseEvent[]{null, null};
        //mouse_select = new Rectangle[]{Rectangle(0,0,0,0), Rectangle(0,0,0,0)};
        cannon_angle = new double[]{1, Math.PI-Math.random()*Math.PI/2};
        cannon_force = new double[]{1, PIXELS_PER_METER*2};
        bubble_size  = new double[]{1, (int)(PIXELS_PER_METER/2+1)};   // It is likely that the cannonball and bubbles will need to be huge, to adhere to normal gravity and velocities.
        bubble_speed = new double[]{1, PIXELS_PER_METER};
        world_gravity = new double[]{1, 9.8*PIXELS_PER_METER};
        world_size = new Dimension[]{MIN_WORLD_SIZE, MIN_WORLD_SIZE};
        world_perim = new Rectangle(0, 0, world_size[0].width, world_size[0].height);

        // A twelve by three meter cannon.
        can = new Cannon((int)(PIXELS_PER_METER*12), (int)(PIXELS_PER_METER*3), new Point((int)PIXELS_PER_METER, (int)PIXELS_PER_METER));
        
        physics_paused = true;
        restart_game = false;
        m_fertileballs = false;
        m_drawhitboxes = false;
        m_bubblegravity = false;
        
        r = new CannonBallRenderer(MIN_WORLD_SIZE);

        //mousepoints = new Points[2][2];

        init_game_state();
    }
   
    // Places the game into the "new game" or start state. 
    private void init_game_state() {
        score_player = 0;
        score_bubbles = 0;
        elapsed_time = 0;
        timer_fertileballs = 0;
        set_cannon_angle(Math.random()*90);
        bubbs = new Vector<Bubble>();
        bubbs.addElement(new Bubble(null)); // Or a configurable number for start bubbles.
    }

    // Getters, setters, etc. For frame's UI to latch into.
    public void set_world_size(Dimension dim) { world_size[1] = new Dimension(Math.max(MIN_WORLD_SIZE.width, dim.width), Math.max(MIN_WORLD_SIZE.height, dim.height)); }
    public void set_bubble_size(double m) { bubble_size[1] = m * PIXELS_PER_METER; }
    public void set_bubble_speed(double mps) { bubble_speed[1] = mps * PIXELS_PER_METER; }
    public void set_cannon_angle(double degrees) { cannon_angle[1] = Math.toRadians(180-Math.min(90, Math.max(0, degrees))); }
    public void set_cannon_force(double mps) { cannon_force[1] = mps; }
    public void set_gravity(double mpsps) { world_gravity[1] = mpsps * PIXELS_PER_METER; }
    public void set_m_fertileballs(boolean m) { m_fertileballs = m; }
    public void set_m_drawhitboxes(boolean m) { m_drawhitboxes = m; r.redraw(~0); }
    public void set_m_bubblegravity(boolean m) { m_bubblegravity = m; }
    public void set_pause(boolean p) { physics_paused = p; }
    public void restart() { set_pause(true); restart_game = true; }
    public boolean is_paused() { return physics_paused; }
    public Renderer renderer() { return r; }
    public double get_cannon_angle() { return Math.toDegrees(Math.PI-cannon_angle[0]); }
    public double get_cannon_force() { return cannon_force[0]; }
    public double get_elapsed_time() { return elapsed_time; }

    // The tick(). This is where all the game logic happens. Intended to be called once per running loop.
    public void tick(double delta_t) {

        //System.out.println(m1 + " | " + m2);

        // Update engine values. Not all require redraws.
        cannon_force[0] = cannon_force[1];
        world_gravity[0] = world_gravity[1];
        if (cannon_angle[0] != cannon_angle[1]) { 
            cannon_angle[0] = cannon_angle[1];
            can.aim(cannon_angle[0]);
            r.redraw(r.l_cannon);
        }
        if (bubble_speed[0] != bubble_speed[1]) {
            bubble_speed[0] = bubble_speed[1];
            for (Bubble bubb : bubbs) bubb.refresh_speed(); // Update all bubbs.
        }
        if (bubble_size[0] != bubble_size[1]) {
            bubble_size[0] = bubble_size[1];
            for (Bubble bubb : bubbs) bubb.refresh_size();
            r.redraw(r.l_bubbles);
        }
        if (!world_size[0].equals(world_size[1])) {
            world_size[0] = world_size[1];
            world_perim.setBounds(0, 0, world_size[0].width, world_size[0].height);
            // Refresh cannon hitbox
            can.refresh_hitbox();
            // Loop through bubbles, keep them in bounds.
            for (Bubble bubb : bubbs) {
                if (bubb.tl_x() <= world_perim.x) bubb.set_pos_x(bubb.radius+1);
                else if (bubb.br_x() >= world_perim.width) bubb.set_pos_x(world_perim.width-bubb.radius-1);
                if (bubb.tl_y() <= world_perim.y) bubb.set_pos_y(bubb.radius+1);
                else if (bubb.br_y() >= world_perim.height) bubb.set_pos_y(world_perim.height-bubb.radius-1);
            }
            r.set_resolution(world_size[0]);
            r.redraw(~0);   
        }

        // Handling mouseclicks and draws
        //if (e_mousclicked) {
        //    System.out.println(mouse_clicked.getPoint());
        //} 

        //if (e_mousedragged) {
            
    


        // Physics (moving, colliding, accelerating)
        if (!physics_paused) {
            // Special modes:
            if (m_fertileballs) {
                if (timer_fertileballs > 5) {  // Reproduce every five seconds.
                    bubbs.addElement(new Bubble(bubbs.elementAt(0).pos())); // Assuming bubbs won't be empty (though a NULL wont break things). 
                    timer_fertileballs = 0;
                } timer_fertileballs += delta_t;
            }
            /* if (fired) {
                    vy += world_gravity[0] * delta_t;
                    ball_x += vx * delta_t;
                    ball_y += vy * delta_t;
                    
                    // if off screen
                    if (ball_x > world_size[0].width || ball_y > world_size[0].height) {
                    fired = false; 
                    }
         } */

            // time + score 
            /* double time = 0;
               int player_score = 0;
               int ball_score = 0;
               time += delta_t; */

            // adding getters for UI
            /* public double get_time() {return time;}
                public int get_player_score() {return player_score;}
                public int get_ball_score() {return ball_score;}
             */
        
            Vec2 normal = new Vec2(0,0);
            Vec2 forces = new Vec2(0,0);
            Rectangle hitbox, intersection;
            if (m_bubblegravity) forces.y = world_gravity[0]*delta_t;
            for (Bubble bubb : bubbs) {
                // Collision detection of bubbles against screen edges. 
                normal.x = normal.y = 0;
                hitbox = bubb.next_hitbox(delta_t);                 // There will always be a degree of slight innacuracy to this, because we cannot predict the next ticktime.
                intersection = hitbox.intersection(world_perim);
                if (hitbox.width != intersection.width) {
                    normal.x = bubb.vel_x()*-2; 
                    if (hitbox.x <= world_perim.x) bubb.set_pos_x(bubb.radius+1);
                    else bubb.set_pos_x(world_perim.width-bubb.radius-1);
                } 
                if (hitbox.height != intersection.height)  {
                    normal.y = bubb.vel_y()*-2;
                    if (hitbox.y <= world_perim.y) bubb.set_pos_y(bubb.radius+1);
                    else bubb.set_pos_y(world_perim.height-bubb.radius-1);
                }
                bubb.accelerate(normal);
                bubb.accelerate(forces);
                bubb.advance(delta_t);
            }

            r.redraw(r.l_bubbles);
            elapsed_time += delta_t;
        } else if (restart_game) {
            init_game_state();
            r.redraw(r.l_bubbles);
            restart_game = false;
        }
    }
    
    // ----------------
    // The Game Renderer 
    // Defines how the drawing's done, to which layers and of how many.
    // ----------------
    class CannonBallRenderer extends Renderer {
        // Alias render layers with descriptive names.
        public final int l_background, l_statics, l_bubbles, l_balloids, l_cannon, l_dragbox;   
        // Border size constant, for readability. Could also make adjustable, by moving and exposing this to Engine.
        public final int BORDER = 1; 
        // For easy of use in drawing. Top left and bottom right corners of world space.
        public Point tl, br;
        
        public CannonBallRenderer(Dimension resolution) {
            super(6, new Dimension(resolution.width+2, resolution.height+2));  // +2 for BORDER
            l_background = LAYERS[0];   // Six layers
            l_statics    = LAYERS[1];
            l_bubbles    = LAYERS[2];
            l_balloids   = LAYERS[3];
            l_cannon     = LAYERS[4];   // The array business may be stupid and uncessary.
            l_dragbox    = LAYERS[5];
            tl = new Point(0+BORDER, 0+BORDER);
            br = new Point(res_x()-BORDER, res_y()-BORDER);
        }
    
        public void draw(int layer, Graphics g) {
                // Could very easily support drawing multiple layers onto one graphics here (with &), if that ever becomes desirable.
                if (layer == l_background)      draw_background(g);
                else if (layer == l_statics)    draw_statics(g);    
                else if (layer == l_bubbles)    draw_bubbles(g);    
                else if (layer == l_balloids)   draw_balloids(g);   
                else if (layer == l_cannon)     draw_cannon(g);     
                else if (layer == l_dragbox)    draw_dragbox(g);    
                else System.out.println("err: bad layer code in draw");
        }
    
        // @todo depending on the planet, can make the background different.
        private void draw_background(Graphics g) {
            g.setColor(Color.darkGray);
            g.fillRect(0, 0, res_x(), res_y());         // Fill background. RenderComposers preserve all layer transparency.
            g.setColor(Color.blue);
            g.drawRect(0, 0, res_x()-1, res_y()-1);
        }
    
        // @todo draw rectangles here, any other static objects
        private void draw_statics(Graphics g) {
            g.setColor(Color.blue);
            g.fillRect(res_x()/2, 30, 40, 40);
        }
    
        private void draw_bubbles(Graphics g) {
            g.setColor(new Color(155, 255, 255, 50));
            for (Bubble bubb : bubbs) {
                g.fillOval(BORDER+bubb.tl_x(), BORDER+bubb.tl_y(), (int)(bubb.radius()*2+1), (int)(bubb.radius()*2+1));
                if (m_drawhitboxes) g.drawRect(bubb.hitbox().x, bubb.hitbox().y, bubb.hitbox().width, bubb.hitbox().height);
            }
        }
    
        // @todo bullets here
        private void draw_balloids(Graphics g) {
            g.setColor(Color.green);
            g.drawRect(res_x()/2 + 35, 25, 20, 20);
        }
    
        private void draw_cannon(Graphics g) {
            Point a = can.tip();
            Point b = can.tail();
            Point z = can.sidediff();
            
            // The wheels are assuming cannon is in bottom right.
            g.setColor(new Color(48, 39, 14, 255));
            g.fillOval((int)(br.x-PIXELS_PER_METER*7.5), (int)(br.y-PIXELS_PER_METER*6), (int)(PIXELS_PER_METER*7.5), (int)(PIXELS_PER_METER*6));
            g.setColor(new Color(36, 38, 17));
            g.fillPolygon(new int[]{a.x+z.x, b.x+z.x, b.x-z.x, a.x-z.x}, new int[]{a.y-z.y, b.y-z.y, b.y+z.y, a.y+z.y}, 4);
            g.setColor(new Color(74, 60, 20, 120));
            g.fillOval((int)(br.x-PIXELS_PER_METER*8), (int)(br.y-PIXELS_PER_METER*5.5), (int)(PIXELS_PER_METER*7.5), (int)(PIXELS_PER_METER*6));
            
            if (m_drawhitboxes) { 
                g.setColor(Color.red);
                g.drawPolygon(new int[]{a.x+z.x, b.x+z.x, b.x-z.x, a.x-z.x}, new int[]{a.y-z.y, b.y-z.y, b.y+z.y, a.y+z.y}, 4);
                g.setColor(Color.blue);
                g.drawLine(a.x, a.y, b.x, b.y);
                g.setColor(Color.magenta);
                g.drawRect(can.hitbox().x, can.hitbox().y, can.hitbox().width, can.hitbox().height);
            }
        }
        /* private void draw_projectiles(Graphics g) {
        g.drawRect()
        if (fired) {
            g.set.solor(Color.green);
            g.fillOval((int)ball_x, (int)ball_y, 10,10);
            }
        } */
    
        // @todo draw the dragbox
        private void draw_dragbox(Graphics g) {
            if (dragbox != null) 
                g.drawRect(dragbox.x, dragbox.y, dragbox.width, dragbox.height);
        }

        // Expose resolution to rest of class, so render size can be matched with world size 1:1
        public void set_resolution(Dimension res) {
            super.set_resolution(new Dimension(res.width+2, res.height+2));
            br = new Point(res_x()-BORDER, res_y()-BORDER);
        }
    }

    // ----------------
    // The Cannon
    // Holds the hitbox, angle, length, etc.
    // Doesn't really 'do' anything. Firing is done elsewhere.
    // ----------------
    class Cannon {
        private Point corner_offset;
        private int length;
        private int diameter;
        private double angle;  // Assuming in range of pi/2 -> pi
        private Rectangle hitbox;

        public Cannon(int l, int d, Point offset) {
            diameter = d;
            length = l; 
            angle = Math.PI/2+Math.PI/4+Math.PI/8;
            corner_offset = new Point(d/2+1+offset.x,d/2+1+offset.y); // Draw as close to corner as possible.
            hitbox = new Rectangle();
            refresh_hitbox();
        }

        public void aim(double rad) {
            angle = Math.min(Math.PI, Math.max(Math.PI/2, rad));
            refresh_hitbox();
        }

        public void refresh_hitbox() {
            Point tip = tip(); Point tail = tail(); Point diff = sidediff();
            hitbox.setBounds(tip.x-diff.x, tip.y-diff.y, tail.x-tip.x+diff.x*2, tail.y-tip.y+diff.y*2);
        }

        public double angle() { return angle; }

        public Rectangle hitbox() { return hitbox; }

        // World space pixel coordinates of tip and tail (center line).
        public Point tip() {
            Point tip = tail();
            tip.x += (int)(Math.cos(angle)*length);
            tip.y += -(int)(Math.sin(angle)*length);
            return tip;
        }
        public Point tail() {
            return new Point(world_size[0].width-corner_offset.x, world_size[0].height-corner_offset.y);
        }
        // Coordinate difference of four vertices from center line.
        public Point sidediff() {
            System.out.println(Math.cos(angle-Math.PI/2)*(diameter/2.0));
            return new Point((int)Math.abs(Math.cos(angle-Math.PI/2)*diameter/2), (int)Math.abs(Math.sin(angle-Math.PI/2)*diameter/2));
        }
    }
   
    // ----------------
    // Balls
    // Abstract clas for common utility between Bubbles and Balloids
    // ---------------- 
    abstract class Ball {
        protected Vec2 pos;
        protected Vec2 vel;
        protected double radius;
        protected Rectangle hitbox;

        public Ball() {} // Extending classes need to ensure that they initialize vel, pos, radius, and hitbox.

        protected Rectangle gen_hitbox(Vec2 p) { return new Rectangle((int)(p.x-radius-1), (int)(p.y-radius-1), (int)(radius*2+1), (int)(radius*2+1)); }
        protected void refresh_hitbox() { hitbox.setBounds((int)(pos.x-radius-1), (int)(pos.y-radius-1), (int)(radius*2+1), (int)(radius*2+1)); }

        // Hitboxes, current position and next (at delta_t).
        public Rectangle hitbox() { return hitbox; }
        public Rectangle next_hitbox(double delta_t) { return gen_hitbox(Vec2.add(pos, Vec2.mul(vel, delta_t))); }
      
        // Apply acceleration force, advance position by velocity (per second, given time).
        public void accelerate(Vec2 accel) { vel.add(accel); }
        public void advance(double delta_t) { 
            pos.add(Vec2.mul(vel, delta_t));
            hitbox.setLocation((int)(pos.x-radius-1), (int)(pos.y-radius-1)); 
        } 

        public double radius() { return radius; }
        public Vec2 vel() { return new Vec2(vel); }
        public Vec2 pos() { return new Vec2(pos); }
        public double vel_x() { return vel.x; } public double vel_y() { return vel.y; }
        public double pos_x() { return pos.x; } public double pos_y() { return pos.y; }
       
        // Four corners of the circle. Primary use in drawing. 
        public int tl_x() { return (int)(pos.x-1-radius); }
        public int tl_y() { return (int)(pos.y-1-radius); }
        public int br_x() { return (int)(pos.x+radius); }
        public int br_y() { return (int)(pos.y+radius); }
    }

    // ----------------
    // The Bubble
    // These are the targets that move around the game.
    // Can be placed randomly, or at a position. Heading is always a random diagonal.
    // Size and speed are based on the single balloid_size/speed vars (shared).
    // ----------------
    class Bubble extends Ball {
        public Bubble(Vec2 position) {
            radius = bubble_size[0];
            // If no position given, init with random (avoiding cannon).
            if (position == null) pos = new Vec2(radius+1+(Math.random()*world_size[0].width*.75-radius-1), radius+1+(Math.random()*world_size[0].height-radius-1)); 
            else pos = new Vec2(position);
            // Heading always random perfect diagonal.
            vel = new Vec2(1,1);
            if (Math.random() < .5) vel.x = -vel.x; 
            if (Math.random() < .5) vel.y = -vel.y;
            hitbox = gen_hitbox(pos);
            refresh_speed();
        }
        
        // Update radius/vel to match bubble_size/bubble_speed
        public void refresh_size() {
            radius = bubble_size[0];
            refresh_hitbox();
        }
        public void refresh_speed() { 
            vel = Vec2.mul(new Vec2(Math.signum(vel.x), Math.signum(vel.y)), bubble_speed[0]+Math.random()*bubble_speed[0]+1);  // Some random variation.
        }

        // The bubbles are to be restricted in world, even on window resize/during pause.
        public void set_pos_x(double x) { pos.x = x; refresh_hitbox(); }
        public void set_pos_y(double y) { pos.y = y; refresh_hitbox(); }
    }
}

// ----------------
// The Canvas. 
// Displays the game, given its Renderer.
// ----------------
class MultiBufferedCanvas extends Canvas {
    private static final long serialVersionUID = 3333L;
    private final Dimension CANVAS_MIN_SIZE = new Dimension(500, 500);
    
    private RenderComposer composer;
    private BufferedImage backbuff;
    //private VolatileImage backbuff;

    // For "debug" stat bar overlay.
    public int debug_lvl; // 3 detail levels: 1 = least, 3 = most. Any other # disables.
    private String debug_msg;
    private long debug_data_last_frame_t; // Nanoseconds
    private double debug_data_frametime;
    private double debug_data_ticktime;

    public MultiBufferedCanvas(Renderer r) {
        System.out.println(r);
        setBackground(new Color(10, 10, 10));
        composer = new RenderComposer(r);
        
        debug_lvl = 0;
        debug_msg = "";
        debug_data_last_frame_t = System.nanoTime();
        debug_data_frametime = 0;
        debug_data_ticktime = 0;
    }
    public MultiBufferedCanvas() {
        // Anonymous class for default renderer (if we wanted to create the canvas before the Engine).
        this(new Renderer(1, new Dimension(256,256)) { 
            { redraw(1); } // 'Instance initializer', who knew?.
            public void draw(int layer, Graphics g) { 
                g.setColor(Color.magenta);
                g.fillRect(0,0,res_x()-1,res_y()-1);
                g.setColor(Color.black);
                g.fillRect(3,3,res_x()-7,res_y()-7);
                g.setColor(Color.red);
                g.drawString("there's nothing", 7, res_y()/2 );
            } 
        });
    }
   
    // Change the renderer. 
    // Could have use, say for game over screens, scoreboards. (as apposed to a layer in Engine)
    public void set_renderer(Renderer r) { composer.set_renderer(r); }

    // On update, recompose the Renderer's layers into one buffer, then swap (paint) it.
    public void update(Graphics g) {
        backbuff = composer.recompose(); 
        paint(g);
    }

    // Draw the backbuffer to the canvas, optionally create and overlay the debug message.
    public void paint(Graphics g) {
        g.drawImage(backbuff, 0, 0, null);
        switch (debug_lvl) {    // Hopefully no performance issues. @todo: Compare with and without this code block.
            case 3: 
                debug_data_frametime = (int)(System.nanoTime()-debug_data_last_frame_t); 
                debug_data_last_frame_t += debug_data_frametime;
                debug_msg += "Ticktime: ~" + (int)(debug_data_ticktime*1000) + "ms | ";
                debug_msg += "Frametime: ~" + (int)(debug_data_frametime/1000000) + "ms | ";
                debug_msg += "Framerate: ~" + (int)(1000000000/debug_data_frametime) + "fps | "; 
            case 2:
                debug_msg += "Layer Count: " + composer.info_layer_count() + " | ";
                debug_msg += "Draw Status: " + Integer.toBinaryString(composer.info_redraw_status()) + " | ";
            case 1: 
                debug_msg += "Renderer Resolution: " + composer.info_res_x() + "x" + composer.info_res_y() + " | ";
                debug_msg += "Canvas Resolution: " + getWidth() + "x" + getHeight();
                g.setColor(Color.red); g.drawString("[" + debug_msg + "]", 12, 15 );
                debug_msg = "";
            }
    }
    
    public void debug_inform_ticktime(double tt) { debug_data_ticktime = tt; }
}

// ----------------
// The Renderer
// Abstract, defines the concept of render layers/Renderer.
// Inheriting class implements the drawing.
// ----------------
abstract class Renderer { 
    public final int LAYER_COUNT;
    public final int[] LAYERS;          // Holds the bitcode handles for each layer, in order of painting precedence.
                                        // Slightly redundent (as each code is just 2^(layer #), but still a useful abstraction.
    
    private int redraw;                 // The redraw code. (sum of codes for every layer with a redraw request) 
    private Dimension resolution;       // Pixel size of the layer buffers.
    private boolean resolution_changed; // Signal all resolution changes, so Composer knows to regenerate buffers with new size.
    
    public Renderer(int layer_count, Dimension res) { 
        LAYER_COUNT = Math.min(8, Math.max(1, layer_count));    // Arbitrary 1 -> 8 layer limit. Really the max is 32, with the bit codes.
        LAYERS = new int[LAYER_COUNT];

        int layer_code = 1; //0b00000001
        for (int i = 0; i < LAYER_COUNT; i++) {
            LAYERS[i] = layer_code;
            layer_code *= 2; //0b00000010 -> 0b00000100 -> ...
        }
        
        redraw = 0; 
        resolution = res.getSize();
        resolution_changed = true;
    }
   
    // Define how a layer should be drawn.
    public abstract void draw(int layer, Graphics g);

    // Flag layer for redraw.
    public void redraw(int layer) { redraw |= layer; }
    public void redraw_clear() { redraw = 0; }
    public int redraw_status() { return redraw; }

    // Read resolution, but leave resolution changes up to child.
    public int res_x() { return resolution.width; }
    public int res_y() { return resolution.height; }
    protected void set_resolution(Dimension res) {
        resolution = res.getSize();
        resolution_changed = true;
    }

    // Expose reschanges. For a RenderComposer to optimize buffer/graphics creation/destruction.
    public boolean reschange_status() { return resolution_changed; }
    public void reschange_clear() { resolution_changed = false; }
}


// RC still very w.i.p, regarding VolatileImage vs BufferedImage. If only we had defines and ifdefs to toggle.

// !!! @todo 
// instead of recreating the graphics each time (if it becomes a lag issue), track when the resolution changes, and only recreate them then.
// instead of creating new buffers each redraw, keep them and their graphics. just wipe the buffer with transparent pixels and call the layer's draw method.
// How best to do? Could make as part of the redraw bitcode, that would give reason for the code array too, cause the codes won't be directly related to powers of 2.

// Create and manage BufferedImage layers for a Renderer. Bake the final image.
//  ! Not designed for concurrency. Ensure that the Renderer and RenderComposer are not being accessed/modified concurrently.
class RenderComposer {
    private final Color TRANSPARENT = new Color(0,0,0,0);

    int draws;  // Holds the code of layers to draw in update.
    private Renderer r;
    private Graphics gfx;
    //private VolatileImage composedbuff; // @ todo use volatile instead?
    private BufferedImage composedbuff; // @ todo use volatile instead?
    private Graphics2D composedbuff_gfx;    // Graphics 2D is necessary for transparent clearing.
    //private VolatileImage[] layerbuffs;
    private BufferedImage[] layerbuffs;
    private Graphics2D[] layerbuffs_gfx;
    private GraphicsConfiguration gc;

    public RenderComposer(Renderer rudolph) {
        gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        set_renderer(rudolph);
    }
   
    // There could be a use for changing the renderer. 
    public void set_renderer(Renderer rudolph) {
        r = rudolph;
        System.out.println(rudolph);

        // Init empty layers.
        //layerbuffs = new VolatileImage[r.LAYER_COUNT];
        layerbuffs = new BufferedImage[r.LAYER_COUNT];
        layerbuffs_gfx = new Graphics2D[r.LAYER_COUNT];
        generate_buffers();
    }

    // Generate all buffers once, fitting to the resolution of the set Renderer.
    private void generate_buffers() {
        //composedbuff = gc.createCompatibleVolatileImage(r.res_x(), r.res_y(), Transparency.TRANSLUCENT);//VolatileImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
        composedbuff = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
        composedbuff_gfx = composedbuff.createGraphics();//getGraphics();
        composedbuff_gfx.setBackground(TRANSPARENT);    // For clearRect
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            layerbuffs[i] = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
            //layerbuffs[i] = gc.createCompatibleVolatileImage(r.res_x(), r.res_y(), Transparency.TRANSLUCENT);//VolatileImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
            layerbuffs_gfx[i] = layerbuffs[i].createGraphics(); //getGraphics();
            layerbuffs_gfx[i].setBackground(TRANSPARENT);
        } 
    }
    // Dispose and nullify all buffers and graphics. 
    public void dispose_buffers() {
        composedbuff_gfx.dispose(); // @bug potential if we ever try to dispose without generating first. Easy fix is a condition, but I don't see how that bug could happen rn. The RenderComposer always needs a Renderer set, from construction till destruction. 
        composedbuff_gfx = null;
        composedbuff = null;
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            layerbuffs_gfx[i].dispose();
            layerbuffs_gfx[i] = null;
            layerbuffs[i] = null;
        }
    }

    // Check redraw requests of Renderer, update layers accordingly.
    private void update() {
        draws = r.redraw_status(); r.redraw_clear();
        // Regenerate buffers to correct size on Renderer resolution changes.
        if (r.reschange_status()) {
            dispose_buffers();
            generate_buffers(); // @bug Hopefully no concurrency issues arise (Renderer changing resolution rapidly from an AWT event), r.res_x/y() in the generate method giving different answers per buffer. I think with the way we've threading set up, and the way the Renderer's resolution is only changed in Engine during tick (i think), this should be a problem.
            r.reschange_clear();
        }
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            if ((draws & r.LAYERS[i]) > 0) { // Again, could just be 2^i instead of LAYERS[i].
                //layerbuffs[i] = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
                //gfx = layerbuffs[i].getGraphics();
                //r.draw(r.LAYERS[i], gfx);A
                //layerbuffs_gfx[i].setColor(TRANSPARENT);
                layerbuffs_gfx[i].clearRect(0, 0, r.res_x(), r.res_y()); // @bug ! Again, hopefully Renderer resolution isn't change during this.
                                                                    // Also, we could just allow the Renderer to wipe at their own discretion.
                r.draw(r.LAYERS[i], layerbuffs_gfx[i]);
                //System.out.println("draw");
                //gfx.dispose();
            }
        }
        if (draws != 0) compose(); // Only compose layers if there was a redraw. (!= 0 instead of >0, because all bits set == -1)
    }

    // Bake layerbuffs onto composedbuff.
    private void compose() {
        //composedbuff = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB); // Is transparent, leaving it up to the layers or image recipient to draw the background. 
        //gfx = composedbuff.getGraphics();
        //gfx.fillOval(50, 50, 10, 10);
        //composedbuff_gfx.setColor(Color.white);
        composedbuff_gfx.clearRect(0, 0, r.res_x(), r.res_y());
        //for (VolatileImage layer : layerbuffs) {
        for (BufferedImage layer : layerbuffs) {
            //gfx.drawImage(layer, 0, 0, null);
            composedbuff_gfx.drawImage(layer, 0, 0, null);
            //System.out.println("drawg" + layer);
        }
        //gfx.dispose();
        //System.out.println("composin");
    }

    //public VolatileImage recompose() {
    public BufferedImage recompose() {
        update();
        return composedbuff;
    }

    // Expose some renderer data, primarily for debug info in MultiBufferedCanvas. Lil sloppy.
    public int info_res_x() { return r.res_x(); }
    public int info_res_y() { return r.res_y(); }
    public int info_layer_count() { return r.LAYER_COUNT; }
    public int info_redraw_status() { return draws; } //r.redraw_status(); } return draws from last update, otherwise will always be zero wth the current single thread configuration.
}

// ----------------
// Vec2
// Two dimensional vector, for velocity and position data.
// Doubles (instead of ints) to support pixel movement slower than the tickrate.
// ----------------
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

class OldCannon {
    int base_x, base_y;        // center of base
    int base_radius;           

    double angle_deg;          // angle in degrees
    int barrel_length;         
    int barrel_width;          

    public OldCannon(int x, int y) {
        base_x = x;
        base_y = y;

        base_radius = 20;
        angle_deg = 45;
        barrel_length = 60;
        barrel_width = 12;
    }

    public void set_angle(double deg) {
        angle_deg = Math.max(0, Math.min(90, deg));   // clamp angle
    }

    public void draw(Graphics g) {

        // draw base
        g.setColor(Color.darkGray);
        g.fillOval(base_x - base_radius, base_y - base_radius,
                   base_radius * 2, base_radius * 2);

        // convert angle
        double rad = Math.toRadians(angle_deg);     //  tried to do without radians, thatn was a mess

        // direction vector
        double dx = -Math.cos(rad);
        double dy = -Math.sin(rad);

        // perpendicular vector
        double px = -dy;
        double py = dx;

        int half_w = barrel_width / 2;

        // rectangle corners
        int x1 = (int)(base_x + px * half_w);
        int y1 = (int)(base_y + py * half_w);

        int x2 = (int)(base_x - px * half_w);
        int y2 = (int)(base_y - py * half_w);

        int x3 = (int)(x2 + dx * barrel_length);
        int y3 = (int)(y2 + dy * barrel_length);

        int x4 = (int)(x1 + dx * barrel_length);
        int y4 = (int)(y1 + dy * barrel_length);

        // draw barrel
        g.setColor(Color.gray);
        g.fillPolygon(
            new int[]{x1, x2, x3, x4},
            new int[]{y1, y2, y3, y4},
            4
        );
    }
}



// COMMENT/todo GRAVE
//  ignore
//  will delete later
// -------------------

// If globals become necessary, we can do something like this:

//public class CannonVsBall {
//    public static final Dimension window_min_size = new Dimension(640, 480);
//    public static final Dimension canvas_min_size = new Dimension(500, 500);  // Should be smaller than window_min_size, to account for menubar, margins & control panel.
//    public static final Dimension gworld_min_size = new Dimension(500, 500);  // Should be no bigger than canvas_min_size, so the game world doesn't overdraw on small canvases.
//
//    public static void main(String[] args) {
//        new uitest();
//    }
//}


// Different fonts for menubar? Styling?
// Derive framerate from frametime in canvas, for debug display?
// Have another debug info layer built into CannonBallRenderer
//  - Could override the Renderer redraw_status and redraw_clear to set the debug layer bit depending on a flag.

//todo:
// Connect MouseListener to Engine
// Implement Engine tick() (rudimentery at first, just test out mouse drawrect)
// Implement Thread in main class, to tick and render, and any other polls of Engine info to keep GUI accurate.
//
// ! Preserve the creating and destroying of graphics and buffers for only on resolution change. Important for performance.
//
// Compare all the frame settings in constructor with last program, to make sure we're not missing anything.

// @todo Add more gravity/environment options, create a constant to define the relation between pixels and meters.

    // Window shouldn't get smaller than rects, but also should be infinitely small. 
    //  We'll need to compare this against the game min size and canvas min size as well on componentResized.
    
    // @todo sometime maybe idk, Menu and Menubar support a getMenu and getItem Count method, 
    // we could use that to dynamically traverse the menubar structure to find things instead of 
    // defining it all here and in constructor.
        
    // @todo Could we think of a way to do all this (creating and attaching MenuItems) in the Engine, and attach 
    // the Engine as listener? Just pass the menubar or menuitem back to this frame somehow? What about the Scrolls too?
    
    // @todo ! We could have a Color pallete class that stores all these specific colors for components, then change that class and refresh when the environment/background is changed.
        
    // @todo Should we add randomness here (MenuItem defaults)? What about an extra menuitem on each, that disables all radios and applies a random size, speed, or gravity?
        
    // @todo is there a better way to set these defaults using the state of the Engine? Perhaps polling this data from Engine on each loop of the Thread, and setting the UI accordingly?
    //
    // @ todo poll cannon angle and force values on each run() iteration, to keep UI scrolls in sync.
    
    // This is the frame limiter !!! @todo add a MenuBar option to change it.
    
    // Should everything be started with thread start? Like the frame (setting visible) too?
    
    //if (item instanceof CheckboxMenuItem) 
        
        // Because border layout, display resizes automatically to fit frame. 
        // So, only need to set game world size to match.
        //  ! This also probably means that a min_canvas_size constant is irrelevant
        //    Only need a min_game_world_size (which changes based on rectangle placement (but should still itself have a min, so can't be zero and break))
        //    Then in the main loop, if a rectangle was placed (or just if min_world_size was updated), set the frame minimumSize to be the max of it's min_window_size and the min_world_size.

    // @todo possible randomness for set size/speed Could also have set_bubble_size(5, x), to specify the middle size and a range smaller/bigger for randomness. Or that random variation could just be hardset as some constant or constant adjusted for size.

            //    // Could do away with the offsets, have another parallel array of the settings, and just iterate 0->NUM_whatever. Or since they're parallel, just use the return of radio (cause that'll be the index in the value array). That could automate all the creations and adding of the MenuItems to one NUM_ITEMS number, though setting up names for the items and the related setting values would all still be manual. Would also make the planet gravity values a little unintuitive, because they have no direct relation (speeds, sizes go up). You would need to know that they're planets, and in the order from the sun.
            //    // Although the offsets do have their benefits, say if we wanted to do a specific thing for a specific setting, like changing a color or graphic. It's nice to be able to tell explicity which setting, and not treat them all the same.
    
    // ! the critical consideration is whether or not to have all these listeners as part of this main frame class, 
    // or some/all implemented as part of the engine. Or something other third thing? I don't think the renderer would have any need.
    // There is an ugly seperation between awt UI objects and the mouse/keyboard inputs to the game. 
    // Whatever. AWT UI objects all act externally to the Engine, mouse events and keyboard events get processed in the engine.
                            
    
                            // If we have paused here, but the main loop is in main, and all the setting of rects and changing of sizes is of course done here, then pausing may cause issues with setting those during pause.
                            // Unless, pause doesn't stop ticks from doing anything entirely, it just skips the velocity/collision parsing stuff. Still allows for changing size, speed vars, and adding/deling rects. And moving the cannon. Ig. It should be that the cannon can fire multiple bullets, with a configurable rate of fire. But that's only during not pause, obv. Should we allow angle adjustment during a pause? Could be a setting. What does rubric say?
    
    //private boolean running;
    //private boolean e_dragging;
    //private boolean e_dragstop;
    //private boolean e_add_rect;
    //private boolean e_del_rect;
    //private boolean e_fire_cannon;
    //private boolean e_update_cannon_force;
    //private boolean e_update_cannon_angle;
    //private Rectangle dragbox;
    //private double next_force;
    //private double next_angle;
    //private double next_speed;      // This begs the question, should the Engine just take a double value to set as ball speed, or should it know about speed and size presets?
    //private double next_size;
    //private double next_gravity;    // What about our planet presets too? Should it know the gravity of mars, or do we do that here and just supply the gravity value?
    //private Dimension world_size;
    //private Dimension next_world_size;
    //private boolean e_world_size_changed = false;
    
    
    // // All as doubles, do any rounding up (for a pixel) in CannonBallRenderer
    // Only accounting for one cannon in the game, so it really doesn't need the overhead of an object right now. Unlike balloids, rects, or bubbles, of which there will be many.

    //private Vector<Rectangle> rects; 
    //private Vector<Circle>                  // @todo create classes for balloids and bubbles, which use the java shapes internally for size, position, and collision detection.

    // Used by the MultiBufferedCanvas during paints.                   // @todo should dragbox be done through CannonBallEngine/Game, or through this renderer?

// RN
//  Adding back the Engine functionality.
//  
//  X Creating the RenderComposer, to sit between Renderer and MultiBufferedCanvas
//  X Doing the component resizing, getting that to work nicely with the canvas.
//    - For setting proper min sizes based on rectangles, we'll probably have to check the engine on each frame for a rectangle added flag (or something), and then set min sizes.
//    - Honestly though, it's really only the frame that needs its min size to match the rects right? Still probably, at least the engine should know of it's min size (rectangle bounds).
//
//  Getting control panel looking good and attached to Engine.
//  Adding debug overlay to make sure rendering pipeline workin right.
//
//  Linking Mouse and Keyboard into Engine, for dragbox, cannonfires, and game buttons
//  Readding balls, so we can have something to move around in tick() for testing.

        
        // Dragbox is done in renderer. Probably shouldn't be.
        //private Rectangle dragbox;
    
    // The resolution thing is an interesting problem. Should the internal rendering be normalized and adjusted to fit in this, resizing everything with window? That probably goes aginst the lesson plan.
    
    // Check layers flagged for redraw, clear redraw.
    //public int redraws() {    // @todo !? is there a good reason to have this atomic?
    //    int layers = redraw; 
    //    redraw = 0; 
    //    return layers; 
    //}


    //private MultiBufferedCanvas;    // Instead of passing the canvas in Game for it to set renderer, we could have it call repaint through the renderer. Though this is probably a bad idea.
    //public void set_canvas()  
    //public void repaint()


//    private int debug_update_timer; 
//    private final int debug_update_timer_refresh = 50; // How many milliseconds between debug stat updates?
    //private Graphics debug_gfx;
    //private BufferedImage debug_buff;
        
        //debug_update = 0;
        //debug_buff = new BufferedImage(CANVAS_MIN_SIZE.width, 25, BufferedImage.TYPE_INT_ARGB);
        //debug_gfx = debug_buff.getGraphics();

// update(g)
// !!! @todo Instead, we could draw onto an internal backbuff instead of taking the compose buff. OR! have recompose return void and take a buff to draw onto. That might be the best solution.
// Then we can do whatever optimizations in RenderComposer or MultiBufferedCanvas we like, in regards to keeping buffer objects/graphics objects (and only recreating them on resolution change).


                // paint switch() {
                //debug_update++;
                //if (debug_update > 10) {    // Only update every ten frames @todo move this debug message into its own buffer layer, probably just implement the backbuff here seperately in Canvas, then render this on top always whenever debug on. Then we can better do the frame frequency limit, and not have the flicker.
                //    debug_update = 0;
               
                // Doing this copy stuff is soooo laggy. Faster to draw straight to g. Just gotta deal with the flickering. Maybe it's creating the Graphics so many times over that lags?
                // Copy backbuff to a new buffer, to avoid drawing directly into the backbuff reference (which reaches back into RenderComposer).
                //  Could make RenderComposer.recompose() return a copy instead, but that's overhead for only this special case.
                //debug_buff = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                
                //backbuff = new BufferedImage(backbuff.getColorModel(), backbuff.copyData(null), backbuff.getColorModel().isAlphaPremultiplied(), null); // !!!! Doing this over drawImage might be a lot more performant. @todo replace RenderComposer drawImages with this?
                //debug_gfx = backbuff.getGraphics();
                //debug_gfx.drawImage(backbuff, 0, 0, null);
                //g.setColor(new Color(0,0,0,200)); g.fillRect(0,0, getWidth(), 25);    // @todo make sure margins/border (red border) issues don't look weird, just in general. 
                //debug_gfx.setColor(new Color(0,0,0,200)); debug_gfx.fillRect(0,0, getWidth(), 25);    // @todo make sure margins/border (red border) issues don't look weird, just in general. 
                //debug_gfx.setColor(Color.red); debug_gfx.drawString("[" + debug_msg + "]", 12, 15 );
                //} 
                //debug_gfx.dispose();
        //g.drawImage(backbuff, 0, 0, null);
        //g.drawImage(debug_buff, 0, 0, null);

//    // Probably unnecessary, since we're only setting size with setSize. Or we should do check in that. Or different new method.
//    public void setMinumumSize(Dimension dim) {
//        super.setMinimumSize(new Dimension(Math.max(CANVAS_MIN_SIZE.width, dim.width), Math.max(CANVAS_MIN_SIZE.height, dim.height)));
//    }


    //private static final long serialVersionUID = 1111L; // Are these needed everywhere? @todo Ask pyz about his compiler settings.


    // This is the start of the cannon line.
//    // For cannon size, we can set the width to the ball radius, and use the approximation that a cannon is 3 times as long as it is wide.
//    //private final Dimension CANNON_SIZE = new Dimension(10.0*PIXELS_PER_METER, 50.0);
//    class CollidableThing {
//        public Vec2 pos;
//        private Rectangle hitbox;
//
//    // Cannon Projectiles
//    class Balloid {
//        public Vec2 pos
//
            
            //r.redraw(r.l_cannon);     // @todo We may want to make sets, or special codes, for states where specific layers will always need to be redrawn. Who cares.
    

    //@todo
    // We have a consideration to make, if we want to define MIN/MAXs for sizes, speeds, etc. or just let the calling methods handle all that.
    // Right now, for simplicity, I'm going to have faith in the callers.
   

// Make sure to flag everything to draw again, as RenderComposer will leave the new buffers blank. 
// @todo We could make this automatic in CBRenderer, or if VolatileBuffers means generating new buffs all 
// the time (which we should check), this might not be an issue cause we'll be generating new buffs all the time anyways.



            //r.redraw(r.l_background | r.l_statics | r.l_balloids);   // redraw all every tick, to test perf
