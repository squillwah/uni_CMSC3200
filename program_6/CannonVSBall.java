
// [CMSC3200] Technical Computing Using Java
// Program 6: Cannon VS Ball
//
//  ball 
//          cannon
//   
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu

package CannonVSBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

// ----------------
// The Main Class
// Creates the frame + UI elements, game engine and canvas display.
// Manages interaction between UI bits/controls and game state.
// ----------------
public class CannonVSBall implements ActionListener, AdjustmentListener, ComponentListener, ItemListener, Runnable, WindowListener {
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

    public static void main(String[] args) { new CannonVSBall(); }

    public CannonVSBall() {
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
        sb_cannon_force.setMinimum(0); sb_cannon_force.setMaximum(800); sb_cannon_force.setVisibleAmount(100);   
        sb_cannon_force.setBlockIncrement(25); sb_cannon_force.setUnitIncrement(10); sb_cannon_force.setValue(250);

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
        engine.set_cannon_force(sb_cannon_force.getValue());
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

            // Keep scrolls, score, and time in sync.
            sb_cannon_angle.setValue((int)(engine.get_cannon_angle()*10));
            lbl_cannon_angle.setText("Angle: " + sb_cannon_angle.getValue()/10.0 + "deg");    // @todo May instead want to have some connect_cannon_angle_scroll or connect_player_score_lbl thing in engine.
            
            sb_cannon_force.setValue((int)(engine.get_cannon_force()));
            lbl_cannon_force.setText("Force: " + sb_cannon_force.getValue() + "px/s");

            lbl_time.setText("Time: " + ((int)(engine.get_elapsed_time()*10))/10.0);
            lbl_score_ball.setText("Bubbles: " + engine.get_score_bubbles());
            lbl_score_player.setText("Player: " + engine.get_score_player());

            engine.tick(delta_t);
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
            engine.set_cannon_angle(sb_cannon_angle.getValue() / 10.0);
        } else if (bar == sb_cannon_force) {
            engine.set_cannon_force(sb_cannon_force.getValue());
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
        // Restrict minimum sizes to the furthest rectangles out in the engine's world space.
        //    pnl_display.setMinimumSize(new Dimension(engine.get_furthestrect_display_size().width, engine.get_furthestrect_display_size().height-pnl_controls.getHeight()));
        
        engine.set_world_size(new Dimension(pnl_display.getWidth()-2, pnl_display.getHeight()-2)); // Shrink axis by 2 (Renderer expands to create the border).
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
    
    // The Renderer
    private CannonBallRenderer r;
    
//    // Minimum sizing, accounting for rectangles. 
//    private Rectangle furthestrects[];  // 2 possible furthest rects from top left.
//    private Dimension furthestrect_display_size;
    
    // Controlling tick():
    private boolean physics_paused; 
    private boolean restart_game;
    
    // Mode settings:
    private boolean m_fertileballs;
    private boolean m_drawhitboxes;
    private boolean m_bubblegravity;

    // Game Data:
    // Because events (which change engine values) can occur concurrently with the Thread (and thus during a tick(), which is no good),
    // each externally mutable engine data value gets a buddy. The first value is updated to match the second at each tick start.
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
    private boolean fire_cannon; 

    // Game Objects:
    private Cannon can; 
    private Vector<Bubble> bubbs;   
    private Vector<Balloid> balls;  // Like from the cannon.
    private Vector<Rectangle> rects;

    // Mouse stuff:
    private Point m1, m2;
    private boolean dragoff; 
    private Rectangle dragbox;
    private Rectangle addrect[]; 
    private MouseEvent mouseclick[];

    public CannonBallEngine() {
        
        addrect = new Rectangle[]{null, null};
        cannon_angle = new double[]{1, Math.PI-Math.random()*Math.PI/2};
        cannon_force = new double[]{1, PIXELS_PER_METER*2};
        bubble_size  = new double[]{1, (int)(PIXELS_PER_METER/2+1)};   // It is likely that the cannonball and bubbles will need to be huge, to adhere to normal gravity and velocities.
        bubble_speed = new double[]{1, PIXELS_PER_METER};
        world_gravity = new double[]{1, 9.8*PIXELS_PER_METER};
        world_size = new Dimension[]{MIN_WORLD_SIZE, MIN_WORLD_SIZE};
        world_perim = new Rectangle(0, 0, world_size[0].width, world_size[0].height);
        
        // A twelve by three meter cannon.
        can = new Cannon((int)(PIXELS_PER_METER*12), (int)(PIXELS_PER_METER*3), new Point((int)PIXELS_PER_METER, (int)PIXELS_PER_METER));
       
        mouseclick = new MouseEvent[]{null, null};
        m1 = null;
        m2 = null; 
        dragoff = false;
        dragbox = null;
        
        physics_paused = true;
        restart_game = false;
        m_fertileballs = false;
        m_drawhitboxes = false;
        m_bubblegravity = false;

        fire_cannon = false;
        
        r = new CannonBallRenderer(MIN_WORLD_SIZE);
        
        init_game_state();
        
//        furthestrects = new Rectangle[]{new Rectangle(0,0,0,0), new Rectangle(0,0,0,0)};
//        calculate_furthestrect_display_size();
    }
   
    // Places the game into the "new game" or start state. 
    private void init_game_state() {
        score_player = 0;
        score_bubbles = 0;
        elapsed_time = 0;
        timer_fertileballs = 0;
        set_cannon_angle(Math.random()*90);
        rects = new Vector<Rectangle>();
        bubbs = new Vector<Bubble>();
        balls = new Vector<Balloid>();
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
    public int get_score_bubbles() { return score_bubbles; }
    public int get_score_player() { return score_player; }
    public void fire_cannon() { fire_cannon = true; }

//    private void calculate_furthestrect_display_size() {
//        Dimension ds = new Dimension(MIN_WORLD_SIZE.width + r.BORDER*2, MIN_WORLD_SIZE.height + r.BORDER*2);
//        for (Rectangle rect : rects) {
//            if (rect.x+rect.width > ds.width) {
//                ds.width = rect.x+rect.width; 
//                furthestrects[0] = rect;            // Hold references to the furthest rects, so we'll know when to recalulate this if we destroy them.
//            }
//            if (rect.y+rect.height > ds.height) {
//                ds.height = rect.y+rect.height;
//                furthestrects[1] = rect;
//            }
//        }
//        furthestrect_display_size = ds;
//    }
//    public Dimension get_furthestrect_display_size() { return furthestrect_display_size; }

    // The tick(). This is where all the game logic happens. Intended to be called once per running loop.
    public void tick(double delta_t) {
        // Update engine values. Not all require redraws.
        cannon_force[0] = cannon_force[1];
        world_gravity[0] = world_gravity[1];
        
        if (addrect[0] != addrect[1]) {
            addrect[0] = addrect[1];
            // Verify that the rectangle doesn't intersect with cannon, bubbles, or balloids, as well as absorbs smaller rects/becomes absorbed by bigger rects.
            int bubble = 0, balloid = 0, rect = 0;
            boolean addit = addrect[0] != null && !addrect[0].isEmpty() && !addrect[0].intersects(can.hitbox());
            while (addit && bubble < bubbs.size()) {
                addit = !addrect[0].intersects(bubbs.elementAt(bubble).hitbox());
                bubble++;
            }
            while (addit && balloid < balls.size()) {
                addit = !addrect[0].intersects(balls.elementAt(balloid).hitbox());
                balloid++;
            }
            while (addit && rect < rects.size()) {
                addit = !rects.elementAt(rect).contains(addrect[0]);
                if (addrect[0].contains(rects.elementAt(rect))) { rects.removeElementAt(rect); rect--; }
                rect++;
            } 
            if (addit) {
                rects.addElement(addrect[0]);
//                calculate_furthestrect_display_size();
                r.redraw(r.l_statics); 
            }
        }
        if (mouseclick[0] != mouseclick[1]) {
            mouseclick[0] = mouseclick[1];
            Point p = mouseclick[0].getPoint(); p = new Point(p.x-r.BORDER, p.y-r.BORDER); // To world space.
            if (mouseclick[0].getButton() == MouseEvent.BUTTON3) {
                for (int i = 0; i < rects.size(); i++) {
                    if (rects.elementAt(i).contains(p)) {
//                        if (rects.elementAt(i) == furthestrects[0] || rects.elementAt(i) == furthestrects[1])
//                            calculate_furthestrect_display_size();
                        rects.removeElementAt(i);
                        i = rects.size();
                        r.redraw(r.l_statics);
                    }
                }
            } else 
            if (mouseclick[0].getButton() == MouseEvent.BUTTON1) {
                if (can.hitbox().contains(p)) {
                    fire_cannon();
                }
            }
        }
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
//            world_size[0] = new Dimension(Math.max(world_size[1].width, furthestrect_display_size.width), Math.max(world_size[1].height, furthestrect_display_size.height));
//            world_size[1].setSize(world_size[0]);
            world_size[0] = world_size[1]; 
            world_perim.setBounds(0, 0, world_size[0].width, world_size[0].height);
            can.refresh_hitbox();
            for (Bubble bubb : bubbs) {
                if (bubb.tl_x() <= world_perim.x) bubb.set_pos_x(bubb.radius+1);
                else if (bubb.br_x() >= world_perim.width) bubb.set_pos_x(world_perim.width-bubb.radius-1);
                if (bubb.tl_y() <= world_perim.y) bubb.set_pos_y(bubb.radius+1);
                else if (bubb.br_y() >= world_perim.height) bubb.set_pos_y(world_perim.height-bubb.radius-1);
            }
            r.set_resolution(world_size[0]);
            r.redraw(~0);   
            // @Todo restrict the frame size here somehow, or find a way to let the Frame know (cannot shrink smaller than rectangles).
        }

        // Physics (moving, colliding, accelerating)
        if (!physics_paused) {
            // Special modes:
            if (m_fertileballs) {
                if (timer_fertileballs > 3) {  // Reproduce every five seconds.
                    bubbs.addElement(new Bubble(bubbs.elementAt(0).pos())); // Assuming bubbs won't be empty (though a NULL wont break things). 
                    timer_fertileballs = 0;
                } timer_fertileballs += delta_t;
            }
        
           
            // Cannon fire 
            if (fire_cannon) {
                balls.addElement(new Balloid());
                r.redraw(r.l_balloids);
                fire_cannon = false;
            }
        
       
            // Bubble collisions. 
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
                // If nocollide is flagged, attempt to clear
                if (bubb.nocollide) {
                    bubb.nocollide = false;
                    for (Rectangle rect : rects) bubb.nocollide |= hitbox.intersects(rect);
                    bubb.nocollide |= hitbox.intersects(can.hitbox());
                } else {
                    // Otherwise, continue with collision checking against rects and cannon.
                    for (Rectangle rect : rects) {
                        if (!(intersection = hitbox.intersection(rect)).isEmpty()) {
                            if (intersection.height > intersection.width) {
                                normal.x = bubb.vel_x()*-2; 
                                //bubb.set_pos_x(bubb.pos_x()+Math.signum(normal.x)*intersection.width);
                            } else {
                                normal.y = bubb.vel_y()*-2;
                                //bubb.set_pos_y(bubb.pos_y()+Math.signum(normal.y)*intersection.height);
                            }
                        }
                    }
                    // And against the cannon, for score keeping.
                    if (hitbox.intersects(can.hitbox())) {
                        score_bubbles++;
                        bubb.nocollide = true;  // Disable collisions and randomly relocate.
                        bubb.set_pos_x(bubb.radius()+1+(Math.random()*world_size[0].width*.6-bubb.radius()-1));
                        bubb.set_pos_y(bubb.radius()+1+(Math.random()*world_size[0].height-bubb.radius()-1)); 
                    }
                }
                bubb.accelerate(normal);
                bubb.accelerate(forces);
                bubb.advance(delta_t);
            }

            // Balloid collisions and movement.
            Vec2 ball_normal = new Vec2(0,0);
            Vec2 ball_forces = new Vec2(0, world_gravity[0] * delta_t);
            Rectangle ball_hitbox, ball_intersection;

            for (int i = 0; i < balls.size(); i++) {
                Balloid ball = balls.elementAt(i);

                ball_normal.x = 0;
                ball_normal.y = 0;
                ball_hitbox = ball.next_hitbox(delta_t);

                // collide with rectangles, bounce, then destroy rectangle
                for (int j = 0; j < rects.size(); j++) {
                    Rectangle rect = rects.elementAt(j);

                    if (!(ball_intersection = ball_hitbox.intersection(rect)).isEmpty()) {
                        if (ball_intersection.height > ball_intersection.width) {
                            ball_normal.x = ball.vel_x() * -2;
                        } else {
                            ball_normal.y = ball.vel_y() * -2;
                        }

                        rects.removeElementAt(j);
                        r.redraw(r.l_statics);
                        break;
                    }
                }

                // collide with bubbles, cannon
                boolean removed = false;
                for (int j = 0; j < bubbs.size() && !removed; j++) {
                    if (!(ball_intersection = ball_hitbox.intersection(bubbs.elementAt(j).hitbox())).isEmpty()) {
                        bubbs.removeElementAt(j);
                        bubbs.addElement(new Bubble(null));
                        score_player++;

                        balls.removeElementAt(i);
                        i--;
                        removed = true;

                        r.redraw(r.l_bubbles);
                    }
                }
                // Only check cannon collision if ball is older than two seconds. Invincibility frames, like Mario.
                if (ball.lifetime > 1 && ball_hitbox.intersects(can.hitbox())) {
                    balls.removeElementAt(i);
                    i--;
                    removed = true;
                    score_bubbles++;
                }


                if (!removed) {
                    ball.accelerate(ball_normal);
                    ball.accelerate(ball_forces);
                    ball.advance(delta_t);
                    ball.lifetime += delta_t;

                    if (ball.offscreen()) {
                        balls.removeElementAt(i);
                        i--;
                    }
                }
            }

            r.redraw(r.l_balloids);
             

            r.redraw(r.l_bubbles);
            elapsed_time += delta_t;
        } else if (restart_game) {
            init_game_state();
            r.redraw(r.l_bubbles);
            r.redraw(r.l_cannon);
            r.redraw(r.l_statics);
            r.redraw(r.l_balloids);
            restart_game = false;
        }
    }
   

    public void mousePressed(MouseEvent e) { 
        m1 = e.getPoint(); 
        dragbox = new Rectangle();
    }
    public void mouseDragged(MouseEvent e) { 
        m2 = e.getPoint(); 
        if (!dragoff) {
            dragbox.setLocation(Math.max(r.BORDER, Math.min(Math.min(m1.x,m2.x), r.res_x()-r.BORDER*2)), Math.max(r.BORDER, Math.min(Math.min(m1.y,m2.y), r.res_y()-r.BORDER*2)));
            dragbox.setSize(Math.min(Math.abs(m1.x-m2.x), r.res_x()-r.BORDER*2-dragbox.x), Math.min(Math.abs(m1.y-m2.y), r.res_y()-r.BORDER*2-dragbox.y));
        }
        r.redraw(r.l_dragbox);
    }
    public void mouseReleased(MouseEvent e) {
        if (m2 == null) mouseclick[1] = e;
        addrect[1] = dragbox;
        dragbox = null;
        m1 = null;
        m2 = null;
        r.redraw(r.l_dragbox);
    }
    public void mouseExited(MouseEvent e) { dragoff = true; }
    public void mouseEntered(MouseEvent e) { dragoff = false; }
    public void mouseClicked(MouseEvent e) {} public void mouseMoved(MouseEvent e) {}
   

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
            try {
                // Could very easily support drawing multiple layers onto one graphics here (with &), if that ever becomes desirable.
                if (layer == l_background)      draw_background(g);
                else if (layer == l_statics)    draw_statics(g);    
                else if (layer == l_bubbles)    draw_bubbles(g);    
                else if (layer == l_balloids)   draw_balloids(g);   
                else if (layer == l_cannon)     draw_cannon(g);     
                else if (layer == l_dragbox)    draw_dragbox(g);    
                else System.out.println("err: bad layer code in draw");
            } catch (java.util.ConcurrentModificationException e) { 
                // No gods no masters.
                System.out.println("Everything is fine."); 
            }
        }
    
        // @todo depending on the planet, can make the background different.
        private void draw_background(Graphics g) {
            g.setColor(Color.darkGray);
            g.fillRect(0, 0, res_x(), res_y());         // Fill background. RenderComposers preserve all layer transparency.
            //switch ((int)(world_gravity[0])) {
            //    case 3: g.setColor(Color.red.darker()); break;
            //    case 8: g.setColor(Color.white); break;
            //    case 9: g.setColor(Color.green.darker()); break;
            //    case 24: g.setColor(Color.orange.darker()); break;
            //    case 10: g.setColor(Color.orange.brighter()); break;
            //    case 11: g.setColor(Color.blue); break;
            //    case 0: g.setColor(Color.lightGray); break;
            //}
            g.setColor(Color.blue);
            g.drawRect(0, 0, res_x()-1, res_y()-1);
        }
    
        private void draw_statics(Graphics g) {
            g.setColor(Color.orange.darker());
            for (Rectangle r : rects) {
                g.fillRect(r.x, r.y, r.width, r.height);
                if (m_drawhitboxes) {
                    g.setColor(Color.red); 
                    g.drawRect(r.x, r.y, r.width, r.height);
                    g.setColor(Color.orange.darker());
                }
            }
            //g.setColor(Color.blue);
            //g.fillRect(res_x()/2, 30, 40, 40);
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
            g.setColor(new Color(180, 255, 180, 180));

            for (Balloid ball : balls) {
                g.fillOval(BORDER + ball.tl_x(), BORDER + ball.tl_y(),
                        (int)(ball.radius() * 2 + 1), (int)(ball.radius() * 2 + 1));

                if (m_drawhitboxes) {
                    g.setColor(Color.green);
                    g.drawRect(ball.hitbox().x, ball.hitbox().y, ball.hitbox().width, ball.hitbox().height);
                    g.setColor(new Color(180, 255, 180, 180));
                }
            }
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
    
        private void draw_dragbox(Graphics g) {
            g.setColor(Color.white);
            if (dragbox != null) g.drawRect(dragbox.x, dragbox.y, dragbox.width, dragbox.height);
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
            return new Point((int)Math.abs(Math.cos(angle-Math.PI/2)*diameter/2), (int)Math.abs(Math.sin(angle-Math.PI/2)*diameter/2));
        }
    }
   
    // ----------------
    // Balls
    // Abstract class for common utility between Bubbles and Balloids
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
        public boolean nocollide;  // With walls, it cannot always be guaranteed that a random placement will be safe. Easy solution is to disable collision until it becomes safe.

        public Bubble(Vec2 position) {
            nocollide = true;
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
            // Lookahead collision detection. Don't give new size if it intersects.
            Rectangle new_hitbox = (new Rectangle(hitbox));
            new_hitbox.grow((int)(bubble_size[0]-radius), (int)(bubble_size[0]-radius));
            Rectangle intersection = new_hitbox.intersection(world_perim);
            boolean nointersects = new_hitbox.width == intersection.width && new_hitbox.height == intersection.height;
            for (int i = 0; nointersects && i < rects.size(); i++) nointersects = (intersection = new_hitbox.intersection(rects.elementAt(i))).isEmpty();
            if (nointersects) {
                radius = bubble_size[0];
                refresh_hitbox();
            }
        }
        public void refresh_speed() { 
            vel = Vec2.mul(new Vec2(Math.signum(vel.x), Math.signum(vel.y)), bubble_speed[0]+Math.random()*bubble_speed[0]+1);  // Some random variation.
        }

        // The bubbles are to be restricted in world, even on window resize/during pause.
        public void set_pos_x(double x) { pos.x = x; nocollide = true; refresh_hitbox(); }
        public void set_pos_y(double y) { pos.y = y; nocollide = true; refresh_hitbox(); }
    }

    // ----------------
    // The Cannon Ball
    // This is the projectile the cannon fires
    // ----------------
    class Balloid extends Ball {
        public double lifetime;     // Time of been living. Seconds.

        public Balloid() {
            radius = 12.0;

            Point tip = can.tip();
            pos = new Vec2(tip.x, tip.y);

            double speed = cannon_force[0];
            vel = new Vec2(Math.cos(can.angle()) * speed,
                        -Math.sin(can.angle()) * speed);

            hitbox = gen_hitbox(pos);

            lifetime = 0;
        }

        public boolean offscreen() {
            return br_x() < 0 || tl_x() > world_size[0].width ||
                /*br_y() < 0 ||*/ tl_y() > world_size[0].height;
        }
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

    // For "debug" stat bar overlay.
    public int debug_lvl; // 3 detail levels: 1 = least, 3 = most. Any other # disables.
    private String debug_msg;
    private long debug_data_last_frame_t; // Nanoseconds
    private double debug_data_frametime;
    private double debug_data_ticktime;

    public MultiBufferedCanvas(Renderer r) {
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



// ----------------
// The RenderComposer
// Creates and manage BufferedImage layers for a Renderer. Bake the final image.
// Not designed for concurrency. Ensure that the Renderer and RenderComposer are not being accessed/modified concurrently.
// ----------------
class RenderComposer {
    private final Color TRANSPARENT = new Color(0,0,0,0);

    int draws;  // Holds the code of layers to draw in update.
    private Renderer r;
    private Graphics gfx;
    private BufferedImage composedbuff;     // @ todo use volatile instead?
    private Graphics2D composedbuff_gfx;    // Graphics 2D is necessary for transparent clearing.
    private BufferedImage[] layerbuffs;
    private Graphics2D[] layerbuffs_gfx;

    public RenderComposer(Renderer rudolph) {
        set_renderer(rudolph);
    }
   
    // There could be a use for changing the renderer. 
    public void set_renderer(Renderer rudolph) {
        r = rudolph;
        // Init empty layers.
        layerbuffs = new BufferedImage[r.LAYER_COUNT];
        layerbuffs_gfx = new Graphics2D[r.LAYER_COUNT];
        generate_buffers();
    }

    // Generate all buffers once, fitting to the resolution of the set Renderer.
    private void generate_buffers() {
        composedbuff = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
        composedbuff_gfx = composedbuff.createGraphics();//getGraphics();
        composedbuff_gfx.setBackground(TRANSPARENT);    // For clearRect
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            layerbuffs[i] = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
            layerbuffs_gfx[i] = layerbuffs[i].createGraphics(); //getGraphics();
            layerbuffs_gfx[i].setBackground(TRANSPARENT);
        } 
    }
    // Dispose and nullify all buffers and graphics. 
    public void dispose_buffers() {
        composedbuff_gfx.dispose(); 
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
            generate_buffers(); 
            r.reschange_clear();
        }
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            if ((draws & r.LAYERS[i]) > 0) { // Again, could just be 2^i instead of LAYERS[i].
                layerbuffs_gfx[i].clearRect(0, 0, r.res_x(), r.res_y()); // Hopefully Renderer resolution isn't change during this.
                                                                         // Also, alternatively, Renderer could just wipe at their own discretion.
                r.draw(r.LAYERS[i], layerbuffs_gfx[i]);
            }
        }
        if (draws != 0) compose(); // Only compose layers if there was a redraw. 
    }

    // Bake layerbuffs onto composedbuff.
    private void compose() {
        composedbuff_gfx.clearRect(0, 0, r.res_x(), r.res_y());
        for (BufferedImage layer : layerbuffs) {
            composedbuff_gfx.drawImage(layer, 0, 0, null);
        }
    }

    public BufferedImage recompose() {
        update();
        return composedbuff;
    }

    // Expose some renderer data, primarily for debug info in MultiBufferedCanvas. Lil sloppy.
    public int info_res_x() { return r.res_x(); }
    public int info_res_y() { return r.res_y(); }
    public int info_layer_count() { return r.LAYER_COUNT; }
    public int info_redraw_status() { return draws; } 
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

