
//package CannonBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;


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



public class uitest implements ActionListener, AdjustmentListener, ComponentListener, ItemListener, Runnable, WindowListener {
    private static final long SerialVersionUID = 124987123L;
     
    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    
    // Offsets for MenuItem arrays.
    private final byte run     = 0, pause = 1, restart = 2, quit  = 3, NUM_CONTROLS = 4; 
    private final byte xsmall  = 0, small = 1, medium  = 2, large = 3, xlarge  = 4, NUM_SIZES    = 5;
    private final byte xslow   = 0, slow  = 1, normal  = 2, fast  = 3, xfast   = 4, NUM_SPEEDS   = 5;
    private final byte mercury = 0, venus = 1, earth   = 2, mars  = 3, jupiter = 4, saturn = 5, uranus = 6, neptune = 7, pluto = 8, NUM_PLANETS = 9;
    private final byte nodebug = 0, db1 = 1, db2 = 2, db3 = 3, NUM_DEBUG_LEVELS = 4;

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
    private Menu mnu_control, mnu_parameters, mnu_environment, mnu_parameters_mnu_size, mnu_parameters_mnu_speed, mnu_debuginfo;
    private MenuItem[] mnu_control_itms;                        
    private CheckboxMenuItem[] mnu_parameters_mnu_size_itms;    
    private CheckboxMenuItem[] mnu_parameters_mnu_speed_itms;   
    private CheckboxMenuItem[] mnu_environment_itms;
    private CheckboxMenuItem[] mnu_debuginfo_itms;
    private Label lbl_cannon_force, lbl_cannon_angle, lbl_score_ball, lbl_score_player, lbl_time;   
    private Scrollbar sb_cannon_force, sb_cannon_angle;                                                 // @todo sometime maybe idk, Menu and Menubar support a getMenu and getItem Count method, we could use that to dynamically traverse the menubar structure to find things instead of defining it all here and in constructor.

    public uitest(/*Dimension initial_size*/) {
        //window_min_size = initial_size.getSize();     // Window shouldn't get smaller than rects, but also should be infinitely small. We'll need to compare this against the game min size and canvas min size as well on componentResized.
        
        // Engine, Display, Thread:
        engine = new CannonBallEngine();
        display = new MultiBufferedCanvas(engine.renderer());
        main_thread = null;
        main_thread_running = false;
        
        // Frame:
        window = new Frame();
        window.setTitle("CannonBubbles");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setBackground(Color.black);//new Color(10,10,10));//Color.black);
        window.setLayout(new BorderLayout());
        //window.setBounds(10, 10, window.getWidth(), window.getHeight());
       
        // Panels:
        pnl_display  = (Panel)window.add("Center", (new Panel()));  // Hopefully this cast doesn't cause issue.
        pnl_display.setBackground(Color.gray);
        pnl_display.setLayout(new BorderLayout());
        pnl_controls = (Panel)window.add("South", (new Panel()));
        pnl_controls.setBackground(new Color(158, 137, 79));//99, 123, 145));
        pnl_controls.setLayout(new GridBagLayout());
        
        // Menubar, MenuItems: 
        menubar = new MenuBar();    // Mayhaps the menubar would benefit from it's own class? Who cares.
        mnu_control               = menubar.add(new Menu("Control"));
        mnu_control_itms          = new MenuItem[4];
        mnu_control_itms[run]     = mnu_control.add(new MenuItem("Run"));
        mnu_control_itms[pause]   = mnu_control.add(new MenuItem("Pause"));
        mnu_control_itms[restart] = mnu_control.add(new MenuItem("Restart"));
        mnu_control.addSeparator();
        mnu_control_itms[quit]    = mnu_control.add(new MenuItem("Quit"));
        mnu_parameters                       = menubar.add(new Menu("Parameters"));
        mnu_parameters_mnu_size              = (Menu)mnu_parameters.add(new Menu("Size"));  // Possibly buggy cast
        mnu_parameters_mnu_size_itms         = new CheckboxMenuItem[NUM_SIZES];
        mnu_parameters_mnu_size_itms[xsmall] = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("xsmall"));
        mnu_parameters_mnu_size_itms[small]  = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("small"));
        mnu_parameters_mnu_size_itms[medium] = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("medium"));
        mnu_parameters_mnu_size_itms[large]  = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("large"));
        mnu_parameters_mnu_size_itms[xlarge] = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("xlarge"));
        mnu_parameters_mnu_speed              = (Menu)mnu_parameters.add(new Menu("Speed"));
        mnu_parameters_mnu_speed_itms         = new CheckboxMenuItem[NUM_SPEEDS];
        mnu_parameters_mnu_speed_itms[xslow]  = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xslow"));
        mnu_parameters_mnu_speed_itms[slow]   = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("slow"));
        mnu_parameters_mnu_speed_itms[normal] = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("normal"));
        mnu_parameters_mnu_speed_itms[fast]   = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("fast"));
        mnu_parameters_mnu_speed_itms[xfast]  = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xfast"));
        mnu_environment = menubar.add(new Menu("Environment"));
        mnu_environment_itms          = new CheckboxMenuItem[NUM_PLANETS];
        mnu_environment_itms[mercury] = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Mercury")); // @todo Could we think of a way to do all this in the Engine, and attach the Engine as listener? Just pass the menubar or menuitem back to this frame somehow? What about the Scrolls too?
        mnu_environment_itms[venus]   = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Venus"));
        mnu_environment_itms[earth]   = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Earth"));
        mnu_environment_itms[mars]    = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Mars"));
        mnu_environment_itms[jupiter] = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Jupiter"));
        mnu_environment_itms[saturn]  = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Saturn"));
        mnu_environment_itms[uranus]  = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Uranus"));
        mnu_environment_itms[neptune] = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Neptune"));
        mnu_environment_itms[pluto]   = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("PLUTO"));
        mnu_debuginfo = menubar.add(new Menu("Info"));
        mnu_debuginfo_itms          = new CheckboxMenuItem[NUM_DEBUG_LEVELS];
        mnu_debuginfo_itms[nodebug] = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("none"));
        mnu_debuginfo_itms[db1]     = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("level 1"));
        mnu_debuginfo_itms[db2]     = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("level 2"));
        mnu_debuginfo_itms[db3]     = (CheckboxMenuItem)mnu_debuginfo.add(new CheckboxMenuItem("level 3"));
        
        // Conpan Scrolls, Labels:
        GridBagConstraints gbc = new GridBagConstraints(); // @todo configure this gridbag stuff
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.ipady = 2;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1;  gbc.ipady = 2; gbc.insets = new Insets(10,10,0,10); Scrollbar sb_cannon_force = new Scrollbar(Scrollbar.HORIZONTAL);  pnl_controls.add(sb_cannon_force, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1;  gbc.ipady = 1; gbc.insets = new Insets(0,10,5,10); Label lbl_cannon_force = new Label("Force: ?px/s", Label.CENTER); pnl_controls.add(lbl_cannon_force, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = .5; gbc.ipady = 1; gbc.insets = new Insets(5,10,0,0); Label lbl_score_ball   = new Label("Bubble: ", Label.CENTER);     pnl_controls.add(lbl_score_ball, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = .75; gbc.ipady = 1; gbc.insets = new Insets(0,10,5,10); Label lbl_time         = new Label("Time: ?s", Label.CENTER);     pnl_controls.add(lbl_time, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = .5; gbc.ipady = 1; gbc.insets = new Insets(5,0,0,10); Label lbl_score_player = new Label("Player: ", Label.CENTER);        pnl_controls.add(lbl_score_player, gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1;  gbc.ipady = 2; gbc.insets = new Insets(10,10,0,10); Scrollbar sb_cannon_angle = new Scrollbar(Scrollbar.HORIZONTAL);  pnl_controls.add(sb_cannon_angle, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1;  gbc.ipady = 1; gbc.insets = new Insets(0,10,5,10); Label lbl_cannon_angle = new Label("Angle: ?deg", Label.CENTER);  pnl_controls.add(lbl_cannon_angle, gbc);

        sb_cannon_force.setBackground(pnl_controls.getBackground().darker());  // @todo ! We could have a Color pallete class that stores all these specific colors for components, then change that class and refresh when the environment/background is changed.
        sb_cannon_angle.setBackground(pnl_controls.getBackground().darker());
        //lbl_cannon_angle.setBackground(Color.lightGray);
        //lbl_cannon_angle.setForeground(Color.darkGray);
        
        //  Attach UI Listeners  (may want to do all listeners as last step in constructor @todo)
        for (MenuItem mi : mnu_control_itms) mi.addActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_debuginfo_itms) mi.addItemListener(this);
       
        // Setup panels
        //pnl_display.add("Center", (new Canvas()));
        
        
        
        
        

        // Add things to frame
        window.setMenuBar(menubar);
        window.add("Center", pnl_display);
        window.add("South", pnl_controls);


        // Set radio defaults
        //set_radio(mnu_parameters_mnu_size_itms, mnu_parameters_mnu_size_itms[medium]);
        //set_radio(mnu_parameters_mnu_speed_itms, mnu_parameters_mnu_speed_itms[normal]);
        //set_radio(mnu_environment_itms, mnu_environment_itms[earth]);
        mnu_control_itms[pause].setEnabled(false);
        mnu_parameters_mnu_size_itms[medium].setState(true);    // @todo Should we add randomness here? What about an extra menuitem on each, that disables all radios and applies a random size, speed, or gravity?
        mnu_parameters_mnu_speed_itms[normal].setState(true);
        mnu_environment_itms[earth].setState(true);
        mnu_debuginfo_itms[nodebug].setState(true);
        engine.set_gravity(10);
        engine.set_bubble_size(10);
        engine.set_bubble_speed(10);
        // @todo is there a better way to set these defaults using the state of the Engine? Perhaps polling this data from Engine on each loop of the Thread, and setting the UI accordingly?

        // Set game world size and canvas size
//        engine.set_world_size()     // They should probably have their own internal minimum size, both for their initializations and to compare against on set_size()
//        display.setSize()

        //display.setSize(1000,1000);
        pnl_display.add("Center", display);
        //display.setSize(initial_size);
        
        window.validate(); //? 
        window.setVisible(true);

        window.addWindowListener(this);
        window.addComponentListener(this);

        //display.setSize(pnl_display.getSize());
        //display.repaint();
        start_thread();
    }

    public static void main(String[] args) {
        //new uitest(new Dimension(600, 600));
        new uitest();
    }

    private void exit() {
        stop_thread();
        for (MenuItem mi : mnu_control_itms) mi.removeActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_debuginfo_itms) mi.removeItemListener(this);
        window.removeWindowListener(this);
        window.removeComponentListener(this);
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
        while(main_thread_running) {
            engine.tick(0.0);
            display.repaint();
            //try { Thread.sleep(1000); }
            //try { Thread.sleep(0, 500000); } // Nanoseconds, one half of a millescond.
            try { Thread.sleep(8); }   // This is the frame limiter !!! @todo add a MenuBar option to change it.
            catch (InterruptedException e) {}
        }
    }


    // Should everything be started with thread start? Like the frame (setting visible) too?

    // The THREAD
    //class GameThread implements Runnable { 
    //    boolean running;
    //    public GameThread() { running = false; }
    //    public void run() {
    //        while (running) {
    //            display.repaint();
    //            try { Thread.sleep(1); }
    //            catch (InterruptedException e) {}
    //        }
    //    }
    //}


    // ! the critical consideration is whether or not to have all these listeners as part of this main frame class, or some/all implemented as part of the engine. Or something other third thing? I don't think the renderer would have any need.
    // there is an ugly seperation between awt UI objects and the mouse/keyboard inputs to the game. Whatever. AWT UI objects all act externally to the Engine, mouse events and keyboard events get processed in the engine.


    public void adjustmentValueChanged(AdjustmentEvent e) {

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
                exit();
                break;
            default: System.out.println("err: bad control menu item offset, can't match: " + item); break;
        }
    }

    // Return index of object in CheckboxMenuItem array. -1 if absent.
    private int find_mitem(/*Checkbox*/MenuItem[] items, int size, Object item) {
        int i;
        for (i = 0; i < size && item != items[i]; i++);
        if (!(i < size)) i = -1;
        return i;
    }

    // Tick given radio on and clear all others in array. Using the offset constant as the 'radio' itself.
    private void set_mradio(CheckboxMenuItem[] radios, int size, int radio) {//CheckboxMenuItem radio) {
        //for (CheckboxMenuItem r : radios) r.setState(false);
        int i;
        for (i = 0; i < radio; i++) radios[i].setState(false);
        for (i = radio+1; i < size; i++) radios[i].setState(false);
        radios[radio].setState(true); // Of course assuming radio is in radios.
    }
  
    public void itemStateChanged(ItemEvent e) {
        Object item = e.getSource();
        //if (item instanceof CheckboxMenuItem) 
        int radio;
        if ((radio = find_mitem(mnu_parameters_mnu_size_itms, NUM_SIZES, item)) > -1) {
            set_mradio(mnu_parameters_mnu_size_itms, NUM_SIZES, radio);//(CheckboxMenuItem)item);
            switch (radio) {
                case xsmall: engine.set_bubble_size(5); break;
                case small:  engine.set_bubble_size(10); break;
                case medium: engine.set_bubble_size(20); break;         // Could also have set_bubble_size(5, x), to specify the middle size and a range smaller/bigger for randomness. Or that random variation could just be hardset as some constant or constant adjusted for size.
                case large:  engine.set_bubble_size(40); break;
                case xlarge: engine.set_bubble_size(80); break;
                default: System.out.println("err: bad size menu item offset, can't match: " + item); break;
            } 
        } else 
        if ((radio = find_mitem(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, item)) > -1) {
            set_mradio(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, radio);//(CheckboxMenuItem)item);
            switch (radio) {
                case xslow:  engine.set_bubble_speed(5); break;     // Could do away with the offsets, have another parallel array of the settings, and just iterate 0->NUM_whatever. Or since they're parallel, just use the return of radio (cause that'll be the index in the value array). That could automate all the creations and adding of the MenuItems to one NUM_ITEMS number, though setting up names for the items and the related setting values would all still be manual. Would also make the planet gravity values a little unintuitive, because they have no direct relation (speeds, sizes go up). You would need to know that they're planets, and in the order from the sun.
                case slow:   engine.set_bubble_speed(10); break;    // Although the offsets do have their benefits, say if we wanted to do a specific thing for a specific setting, like changing a color or graphic. It's nice to be able to tell explicity which setting, and not treat them all the same.
                case normal: engine.set_bubble_speed(20); break;    
                case fast:   engine.set_bubble_speed(40); break;
                case xfast:  engine.set_bubble_speed(80); break;
                default: System.out.println("err: bad speed menu item offset, can't match: " + item); break;
            } 
        } else 
        if ((radio = find_mitem(mnu_environment_itms, NUM_PLANETS, item)) > -1) {
            set_mradio(mnu_environment_itms, NUM_PLANETS, radio);//(CheckboxMenuItem)item);
            switch (radio) {
                case mercury: engine.set_gravity(10); break;    // How many meters is a pixel?
                case venus:   engine.set_gravity(11); break;
                case earth:   engine.set_gravity(12); break;
                case mars:    engine.set_gravity(13); break;
                case jupiter: engine.set_gravity(14); break;
                case saturn:  engine.set_gravity(15); break;
                case uranus:  engine.set_gravity(16); break;
                case neptune: engine.set_gravity(17); break;
                case pluto:   engine.set_gravity(18); exit(); break;
                default: System.out.println("err: bad env menu item offset, can't match: " + item); break;
            }
        } else
        if ((radio = find_mitem(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, item)) > -1) { 
            set_mradio(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, radio);
            display.debug_lvl = radio;  // It so happens that the debug level and MenuItem offset align. Do not rely on this, may cause issues if changes are made.
            //switch (radio) {
            //    case nodebug: display.debug_lvl = 0
        }
    }

    // @todo would be far better to use an array for these radio settings and just iterate that in itemStateChanged instead of doing this
    // Or if we can get the parent menu of the MenuItem, then iterate that, we could use that to clear.

    // Right now for testing, just have the canvas resize with frame (using this as componenet listener). Keep game world size static to see how the renderer handles that.

    public void windowClosing(WindowEvent e) { exit(); }
    public void componentResized(ComponentEvent e) { 
        // Because border layout, display resizes automatically to fit frame. 
        // So, only need to set game world size to match.
        //  ! This also probably means that a min_canvas_size constant is irrelevant
        //    Only need a min_game_world_size (which changes based on rectangle placement (but should still itself have a min, so can't be zero and break))
        //    Then in the main loop, if a rectangle was placed (or just if min_world_size was updated), set the frame minimumSize to be the max of it's min_window_size and the min_world_size.
        //display.setSize(pnl_display.getSize()); 
        System.out.println(display.getSize());
        System.out.println(window.getSize());
    }
    
    // Unimplemented WindowListener, ComponenetLister:
    public void windowClosed(WindowEvent e) {} public void windowOpened(WindowEvent e) {} public void windowActivated(WindowEvent e) {} public void windowDeactivated(WindowEvent e) {} public void windowIconified(WindowEvent e) {} public void windowDeiconified(WindowEvent e) {}
    public void componentHidden(ComponentEvent e) {} public void componentShown(ComponentEvent e) {} public void componentMoved(ComponentEvent e) {}
}

class CannonBallEngine {
    private final Dimension MIN_WORLD_SIZE = new Dimension(500, 500);
    
    private boolean paused; // If we have paused here, but the main loop is in main, and all the setting of rects and changing of sizes is of course done here, then pausing may cause issues with setting those during pause.
                            // Unless, pause doesn't stop ticks from doing anything entirely, it just skips the velocity/collision parsing stuff. Still allows for changing size, speed vars, and adding/deling rects. And moving the cannon. Ig. It should be that the cannon can fire multiple bullets, with a configurable rate of fire. But that's only during not pause, obv. Should we allow angle adjustment during a pause? Could be a setting. What does rubric say?

    // Update flags corresponding to control events. 
    // For use in thread loop, to update a game value before ticking the engine.
    // (If we updated immediately on action event, we risk changing a value during a tick, which would be no good indeed).
    private boolean running;
    private boolean e_dragging;
    private boolean e_dragstop;
    private boolean e_add_rect;
    private boolean e_del_rect;
    private boolean e_fire_cannon;
    private boolean e_update_cannon_force;
    private boolean e_update_cannon_angle;

    // To avoid concurrency issues (with tick() being called by a thread), each of these gets two values.
    // One for the setters (which can be changed whenever), and one for tick() (which is only update to next_... once per tick).
    private Rectangle dragbox;
    private double next_force;
    private double next_angle;
    private double next_speed;      // This begs the question, should the Engine just take a double value to set as ball speed, or should it know about speed and size presets?
    private double next_size;
    private double next_gravity;    // What about our planet presets too? Should it know the gravity of mars, or do we do that here and just supply the gravity value?

    private Dimension world_size;

    private CannonBallRenderer r;


    testball[] tests = new testball[100];


    public CannonBallEngine() {

        paused = true;
        world_size = MIN_WORLD_SIZE.getSize();
        r = new CannonBallRenderer(world_size); // ? Is this copied over, or is it now the same object? Will changing world size now change Renderer resolution?
        r.redraw(~0); // debug, draw everything

        world_size = new Dimension(1280, 720);  // Testing resolution change.
        r.set_resolution(world_size);
        r.redraw(~0); // debug, draw everything (not needed here cause thread doesn't start until after this init)
        
        for (int i = 0; i < 100; i++)
            tests[i] = new testball();
    }
    public void set_bubble_size(int px) {
        System.out.println("Size: " + px);
    }
    public void set_bubble_speed(double pps) {
        System.out.println("Speed: " + pps);
    }
    public void set_gravity(double ppsps) {
        System.out.println("Gravity: " + ppsps);
    }
    public void set_pause(boolean p) {
        paused = p;
        System.out.println("Pause: " + paused);
    }
    public boolean is_paused() { 
        return paused; 
    }
    public void restart() {
        System.out.println("Restart");
        set_pause(true);
    }
    public Renderer renderer() { return r; }

    class testball {
        int x = (int)(Math.random()*world_size.width);
        int y = (int)(Math.random()*world_size.height);

        public void move() {
            x += 1;
            y += 1;
        }
    }

    public void tick(double delta_t) {
        if (!paused) {
            for (testball test : tests) {
                test.move();
                if (test.x > world_size.width) test.x = (int)(Math.random()*world_size.width);
                if (test.y > world_size.height) test.y = (int)(Math.random()*world_size.height);
            }
            r.redraw(r.l_bubbles);
            //r.redraw(r.l_background | r.l_statics | r.l_balloids);   // redraw all every tick, to test perf
        }
    }
    
    // Renderer for the game.
    // Implements how/what gets drawn to the graphics.
    // Used by the MultiBufferedCanvas during paints.                   // @todo should dragbox be done through CannonBallEngine/Game, or through this renderer?
    class CannonBallRenderer extends Renderer {
        // Give the render layers more descriptive names.
        public final int l_background, l_statics, l_bubbles, l_balloids, l_cannon, l_dragbox;
        public final int BORDER = 1;
        // Dragbox is done in renderer. Probably shouldn't be.
        private Rectangle dragbox;
        public CannonBallRenderer(Dimension resolution) {
            //super(6, new Dimension(resolution.width+BORDER*2, resolution.height+BORDER*2));  // !!! Account for borders, which are not part of world size (to make collision checking etc. in tick more better)
            super(6, new Dimension(resolution.width+1*2, resolution.height+1*2));  // !!! Account for borders, which are not part of world size (to make collision checking etc. in tick more better)
            l_background = LAYERS[0];   // Six layers
            l_statics    = LAYERS[1];
            l_bubbles    = LAYERS[2];
            l_balloids   = LAYERS[3];
            l_cannon     = LAYERS[4];
            l_dragbox    = LAYERS[5];
            dragbox = null;
        }
    
        public void draw(int layer, Graphics g) {
            //switch (layer) {
                // Could very easily support drawing multiple layers onto one graphics here (with &), if that ever becomes desirable.
                if (layer == l_background)      draw_background(g);
                else if (layer == l_statics)    draw_statics(g);    
                else if (layer == l_bubbles)    draw_bubbles(g);    
                else if (layer == l_balloids)   draw_balloids(g);   
                else if (layer == l_cannon)     draw_cannon(g);     
                else if (layer == l_dragbox)    draw_dragbox(g);    
                else System.out.println("err: bad layer code in draw");
                //System.out.println("cbr draw");
            //}
        }
    
        private void draw_background(Graphics g) {
            // @todo depending on the planet, can make the background different.
            //g.drawRect(0, 0, res_x(), res_y());
            g.setColor(Color.darkGray);
            g.fillRect(0, 0, res_x(), res_y()); // Fill background, otherwise last frame stays.
            g.setColor(Color.gray);
            g.drawRect(0, 0, res_x()-1, res_y()-1);     // it is indeed limited to rendered res, even when overlayed (rect gets cutoff)
            //System.out.println("background");
        }
    
        private void draw_statics(Graphics g) {
            g.setColor(Color.blue);
            g.fillRect(res_x()/2, 30, 40, 40);
            // @todo draw rectangles here, any other static objects
        }
    
        private void draw_bubbles(Graphics g) {
            // @todo draw the bubbles (moving targets) here
            for (testball test : tests) {
                g.setColor(Color.magenta);
                g.fillOval(test.x, test.y, 20, 20);
            }
        }
    
        private void draw_balloids(Graphics g) {
            // @todo bullets here
            g.setColor(Color.green);
            g.drawRect(res_x()/2 + 35, 25, 20, 20);     // it is indeed limited to rendered res, even when overlayed (rect gets cutoff)
        }
    
        private void draw_cannon(Graphics g) {
            // @todo draw the cannon
        }
    
        private void draw_dragbox(Graphics g) {
            // @todo draw the dragbox
            //if (dragbox != null) g.drawRect(dragbox);
        }

        // Expose this to rest of class, so render resolution (size) can be changed to match the world size 1:1
        public void set_resolution(Dimension res) {
            super.set_resolution(res);
        }

        //public void set_dragbox(Rectangle box) {    // Little jank. do dragbox in engine
        //    dragbox = box.getRectangle(); 
        //    redraw(l_dragbox);
        //}
    }
}

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


// Abstract class for Game to extend (and define rendering logic with).
abstract class Renderer { 
    private Dimension resolution;   // The resolution thing is an interesting problem. Should the internal rendering be normalized and adjusted to fit in this, resizing everything with window? That probably goes aginst the lesson plan.
    private boolean resolution_changed; // Signal when the resolution has changed, for whoever is supplying the buffers to draw to.

    public final int LAYER_COUNT;
    public final int[] LAYERS;      // Array of bitstrings, each layer being a unique bit. For ease of access to layer i code. Could just be a function that returns 2^i, given that's all the layer codes are.
    
    private int redraw;             // Flag for layers that need redrawing. Sum of layer codes.
    
    public Renderer(int layer_count, Dimension res) { 
        LAYER_COUNT = Math.min(8, Math.max(1, layer_count));    // Arbitrary 1 -> 8 layer limit. Really the max is 32, with the bit codes.
        LAYERS = new int[LAYER_COUNT];
        int layer_code = 1; //0b00000001
        for (int i = 0; i < LAYER_COUNT; i++) {
            LAYERS[i] = layer_code;
            layer_code *= 2;    //0b00000010 -> 0b00000100 -> ...
        }
        redraw = 0; 
        resolution = res.getSize();
        resolution_changed = true;
    }
   
    // Define how a layer should be drawn.
    public abstract void draw(int layer, Graphics g);

    // Flag layer for redraw.
    public void redraw(int layer) { 
        redraw |= layer; 
    }

    public int redraw_status() { return redraw; }
    public void redraw_clear() { redraw = 0; }

    // Check layers flagged for redraw, clear redraw.
    //public int redraws() {    // @todo !? is there a good reason to have this atomic?
    //    int layers = redraw; 
    //    redraw = 0; 
    //    return layers; 
    //}

    // Get resolution (dimensions of layer buffers).
    public int res_x() { return resolution.width; }
    public int res_y() { return resolution.height; }

    protected void set_resolution(Dimension res) {
        resolution = res.getSize();
        resolution_changed = true;
    }

    // Expose when the resolution's been changed. For a RenderComposer to optimize buffer/graphics creation/destruction.
    public boolean reschange_status() { return resolution_changed; }
    public void reschange_clear() { resolution_changed = false; }

    //private MultiBufferedCanvas;    // Instead of passing the canvas in Game for it to set renderer, we could have it call repaint through the renderer. Though this is probably a bad idea.
    //public void set_canvas()  
    //public void repaint()
}



// !!! @todo 
// instead of recreating the graphics each time (if it becomes a lag issue), track when the resolution changes, and only recreate them then.
// instead of creating new buffers each redraw, keep them and their graphics. just wipe the buffer with transparent pixels and call the layer's draw method.
// How best to do? Could make as part of the redraw bitcode, that would give reason for the code array too, cause the codes won't be directly related to powers of 2.

// Create and manage BufferedImage layers for a Renderer. Bake the final image.
//  ! Not designed for concurrency. Ensure that the Renderer and RenderComposer are not being accessed/modified concurrently.
class RenderComposer {
    private static final long SerialVersionUID = 1111L; // Are these needed everywhere? @todo Ask pyz about his compiler settings.
   
    private final Color TRANSPARENT = new Color(0,0,0,0);

    int draws;  // Holds the code of layers to draw in update.
    private Renderer r;
    private Graphics gfx;
    private VolatileImage composedbuff; // @ todo use volatile instead?
    private Graphics2D composedbuff_gfx;    // Graphics 2D is necessary for transparent clearing.
    private VolatileImage[] layerbuffs;
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
        layerbuffs = new VolatileImage[r.LAYER_COUNT];
        layerbuffs_gfx = new Graphics2D[r.LAYER_COUNT];
        generate_buffers();
    }

    // Generate all buffers once, fitting to the resolution of the set Renderer.
    private void generate_buffers() {
        composedbuff = gc.createCompatibleVolatileImage(r.res_x(), r.res_y(), Transparency.TRANSLUCENT);//VolatileImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
        composedbuff_gfx = composedbuff.createGraphics();//getGraphics();
        composedbuff_gfx.setBackground(TRANSPARENT);    // For clearRect
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            //layerbuffs[i] = new VolatileImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
            layerbuffs[i] = gc.createCompatibleVolatileImage(r.res_x(), r.res_y(), Transparency.TRANSLUCENT);//VolatileImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
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
        for (VolatileImage layer : layerbuffs) {
            //gfx.drawImage(layer, 0, 0, null);
            composedbuff_gfx.drawImage(layer, 0, 0, null);
            //System.out.println("drawg" + layer);
        }
        //gfx.dispose();
        //System.out.println("composin");
    }

    public VolatileImage recompose() {
        update();
        return composedbuff;
    }

    // Expose some renderer data, primarily for debug info in MultiBufferedCanvas. Lil sloppy.
    public int info_res_x() { return r.res_x(); }
    public int info_res_y() { return r.res_y(); }
    public int info_layer_count() { return r.LAYER_COUNT; }
    public int info_redraw_status() { return draws; } //r.redraw_status(); } return draws from last update, otherwise will always be zero wth the current single thread configuration.
}

class MultiBufferedCanvas extends Canvas {
    private final Dimension CANVAS_MIN_SIZE = new Dimension(500, 500);
    
    private static final long SerialVersionUID = 12412410L;
    private RenderComposer composer;
    //private BufferedImage backbuff;
    private VolatileImage backbuff;

    // For debug info bar
    public int debug_lvl;   // 1-3 levels (3 being most detailed), any other number is debug disabled. // @todo ! make this a menubar option
    private String debug_msg;
    private int debug_update_timer; 
    private final int debug_update_timer_refresh = 50; // How many milliseconds between debug stat updates?
    //private Graphics debug_gfx;
    //private BufferedImage debug_buff;
    
    private long time_of_last_frame; // Nanoseconds
    private int frametime;
    private int framerate;

    public MultiBufferedCanvas(Renderer r) {
        System.out.println(r);
        setBackground(new Color(10, 10, 10));
        composer = new RenderComposer(r);
        
        debug_lvl = 3;
        debug_msg = "";
        //debug_update = 0;
        //debug_buff = new BufferedImage(CANVAS_MIN_SIZE.width, 25, BufferedImage.TYPE_INT_ARGB);
        //debug_gfx = debug_buff.getGraphics();

        time_of_last_frame = System.nanoTime();
        frametime = 0;
        framerate = 0;
    }
    public MultiBufferedCanvas() {
        // Anonymous class for empty renderer, debugging.
        this(new Renderer(1, new Dimension(256,256)) {//256, 256)) { 
            { redraw(1); } // 'Instance initializer', to flag empty layer for redraw.
            public void draw(int layer, Graphics g) { 
                g.setColor(Color.magenta);
                g.fillRect(0,0,res_x()-1,res_y()-1);
                g.setColor(Color.black);    // Colors support transparency!
                g.fillRect(3,3,res_x()-7,res_y()-7);
                g.setColor(Color.red);
                g.drawString("there's nothing", 7, res_y()/2 );     // ? i wonder why before this was drawing outside the renderer dimensions. Maybe image overdraw still gets drawn if the image is drawn on a larger image?
            } 
        });
    }
   
    // Changing the renderer could have use. Example: game over screens, a scoreboard, or something. That's if we wanted more than a layer for those.
    public void set_renderer(Renderer r) {
        composer.set_renderer(r);
    }

    public void update(Graphics g) {
        backbuff = composer.recompose(); // !!! @todo Instead, we could draw onto an internal backbuff instead of taking the compose buff. OR! have recompose return void and take a buff to draw onto. That might be the best solution.
                                         // Then we can do whatever optimizations in RenderComposer or MultiBufferedCanvas we like, in regards to keeping buffer objects/graphics objects (and only recreating them on resolution change).
        paint(g);
    }

    public void paint(Graphics g) {
        g.drawImage(backbuff, 0, 0, null);
        //frametime = (int)(System.nanoTime()-time_of_last_frame);
        //time_of_last_frame += frametime;
        //System.out.println(1000000000/frametime);
        switch (debug_lvl) {    // Hopefully no performance issues. @todo: Compare with and without this code block.
            case 3: 
                frametime = (int)(System.nanoTime()-time_of_last_frame);
                time_of_last_frame += frametime;
                debug_msg += "Frametime: " + (frametime+1)/1000000 + "ms | ";
                debug_msg += "Framerate: " + 1000000000/(frametime+1) + "fps | "; // +1 to avoid div by zero. Makes slight inaccuracy but who cares.
            case 2:
                debug_msg += "Layer Count: " + composer.info_layer_count() + " | ";
                debug_msg += "Draw Status: " + Integer.toBinaryString(composer.info_redraw_status()) + " | "; // drawString does not handle newlines
            case 1: 
                debug_msg += "Renderer Resolution: " + composer.info_res_x() + "x" + composer.info_res_y() + " | ";
                debug_msg += "Canvas Resolution: " + getWidth() + "x" + getHeight();
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
                    g.setColor(Color.red); g.drawString("[" + debug_msg + "]", 12, 15 );
                //debug_gfx.setColor(new Color(0,0,0,200)); debug_gfx.fillRect(0,0, getWidth(), 25);    // @todo make sure margins/border (red border) issues don't look weird, just in general. 
                //debug_gfx.setColor(Color.red); debug_gfx.drawString("[" + debug_msg + "]", 12, 15 );
                //} 
                debug_msg = "";
                //debug_gfx.dispose();
        }
        //g.drawImage(backbuff, 0, 0, null);
        //g.drawImage(debug_buff, 0, 0, null);
    }

//    // Probably unnecessary, since we're only setting size with setSize. Or we should do check in that. Or different new method.
//    public void setMinumumSize(Dimension dim) {
//        super.setMinimumSize(new Dimension(Math.max(CANVAS_MIN_SIZE.width, dim.width), Math.max(CANVAS_MIN_SIZE.height, dim.height)));
//    }
}







//class MultiBufferedCanvas extends Canvas {
//    private static final long SerialVersionUID = 12412410L;
//
//    private Image backbuff;
//    private Renderer renderer;
//    private BufferedImage[] layerbuffs;
//    private Graphics gfx;
//    
//    public MultiBufferedCanvas(/*Dimension size*/, Renderer r) {
//        //setSize(size);    // Sizes of renderer, canvas, and game world are seperate. We'll need to make sure they stay in sync, unless we want them out of it.
//        setBackground(Color.white);
//        set_renderer(r);
//    }
//    public MultiBufferedCanvas() {
//        // A default renderer, when none given.
//        this(new Renderer(1, new Dimension(100, 100)) { 
//            public void draw(int layer, Graphics g) { 
//                g.setColor(Color.red);
//                g.drawString("there's nothing", res_x()/2, res_y()/2 ); 
//            } 
//        });
//        renderer.redraw(renderer.LAYERS[0]);
//    }
//
//    public void set_renderer(Renderer r) {
//        renderer = r;
//        layerbuffs = new BufferedImage[renderer.LAYER_COUNT];
//        for (int i = 0; i < renderer.LAYER_COUNT; i++) 
//            layerbuffs[i] = new BufferedImage(renderer.res_x(), renderer.res_y(), BufferedImage.TYPE_INT_ARGB);  // Init empty layers
//    }
//
//    public void update(Graphics g) {
//        // Iterate through layers, redraw if marked for redraw.
//        int draws = renderer.redraws();
//        for (int i = 0; i < renderer.LAYER_COUNT; i++) {
//            if ((draws & renderer.LAYERS[i]) > 0) {
//                layerbuffs[i] = new BufferedImage(renderer.res_x(), renderer.res_y(), BufferedImage.TYPE_INT_ARGB);
//                gfx = layerbuffs[i].getGraphics();
//                renderer.draw(renderer.LAYERS[i], gfx);
//                System.out.println("draw");
//                gfx.dispose();
//                //g.drawImage(layerbuffs[0], 0, 0, null);
//                //return;
//            }
//        }
//        if (draws > 0) paint(g);  // Only recompose layers if there was a redraw
//    }
//
//    public void paint(Graphics g) {
//        backbuff = createImage(renderer.res_x(), renderer.res_y());    // Blank, to wipe last frame and preserve first layer.
//        gfx = backbuff.getGraphics();
//        //gfx.fillOval(50, 50, 10, 10);
//        for (BufferedImage layer : layerbuffs) {
//            gfx.drawImage(layer, 0, 0, null);
//            System.out.println("drawg" + layer);
//        }
//        gfx.dispose();
//        g.drawImage(backbuff, 0, 0, null);
//        //g.drawImage(layerbuffs[0], 0, 0, null);
//    }
//}







/*
// Manages the single game thread, handles all input events affecting how the game plays.       bad idea probably just same as frame class. we need access to the gui objects to handle the events right.
class CannonBallGame implements Runnable, AdjustmentListener, ActionEventListener {
    // pub or priv? 
    private void run() {
        double delta_t;
        long frame_start_t = System.currentTimeMillis();
        while (running) {
            delta_t = (System.currentTimeMillis() - frame_start_t) / 1000;
            frame_start_t = System.currentTimeMillis();
            System.out.println(delta_t);    // debug
         
            // Events 
            if (e_dragging) engine.set_dragbox(rect);
            else if (e_dragstop) {
                engine.set_dragbox(null);
                e_dragstop = false;
            }
            if (e_add_rect) engine.add_rectangle(rect);             // The listeners trigger these events with the corresponding data set (before setting event true).
            if (e_del_rect) engine.del_rectangle(rect);
            if (e_fire_cannon) engine.fire_canon();
            if (e_change_cannon_angle) engine.set_canon_angle(angle);
            if (e_change_cannon_force) engine.set_canon_force(force);

            engine.tick(delta_t);
            display.repaint();
            
            try { Thread.sleep(1); }
            catch (InterruptedException e) {}
        }
    }*/

/*
// Does the logic of moving the balls, physics and collisions and stuff. Also defines how to render all that with a Renderer.
class CannonBallEngine {
    private Dimension world_size;
    private Dimension world_size_min;

    //private CannonBallPhysics cb_physics;
    private CannonBallRenderer cb_render;
    private MultiBufferedCanvas cb_canvas;

    // Events
    private boolean e_fire_cannon;

    private boolean cannon_fire;
    private Vec2 cannon_force;
    private Vec2 cannon_force_next;
    private double cannon_angle;
    private double cannon_angle_next;

    private Vector<Rectangle> rects = new Vector<Rectangle>();      // Only one copy, as rectangles are static.

    private Vector<Balloid> balloids = new Vector<Balloid>();       // Start of tick
    private Vector<Balloid> balloids_next = new Vector<Balloid>();  // End of tick, result of calculations/collisions. Swaps with ^
    
    private Vector<Bubble> bubbles = new Vector<Bubble>();
    private Vector<Bubble> bubbles_next = new Vector<Bubble>();

    // CannonBall
    //
    run() {

    public void tick(double delta_t) {
        cannon_force = cannon_force_next;   // Seperate next variables, as adjustment events can occurr asynchronously with thread.
        cannon_angle = cannon_angle_next;   // unnecessary, cause it will be set by external event handling
        
        if (e_fire_cannon) {
            // Create new balloid with proper velocity and position
            // nah this should be done in fire_cannon() (called externally from external event)
        }
        
        // this whole thing just does physics, all the stuff should be set in motion from external event handling in main thread
        



        
        
    void mouseButtonReleased() {
        addrect(dragbox);
        dragbox = null;
        cb_render.redraw(cb_render.l_dragbox);
    }

    // Engine should not have access to Canvas, nor any thread of it's own
    // The SINGLE thread should be in CannonBall, where it call's the tick() of CanonBallEngine, then the repaint of MultiBufferedCanvas
  
    display.set_renderer(engine.renderer());

    Set all forces and size parameters from GUI listeners
    engine.set_balloid_force(next_force);
    engine.set_cannon_angle(next_angle);    // Or have the listener in engine, and put this in tick as cannon_angle = next_angle
    engine.tick(delta_t);  
    display.repaint();

    Thread.sleep(1) // Or a frame limit
    
    private void run() {
        cb_render.redraw(~0); // Draw everything
        boolean running = false;
        
        double delta_t = 0;
        long frame_start_t = System.currentTimeMillis();
        while (running) {
            delta_t = (System.currentTimeMillis() - frame_start_t) / 1000;
            frame_start_t = System.currentTimeMillis();
            
            //cb_physics.tick(delta_t);
            cb_render.redraw(cb_render.l_bubbles | cb_render.l_balloids);
            
            System.out.println(delta_t);
        }
    }

    public CannonBallEngine(Dimension world_size, MultiBufferedCanvas display) {
        cb_render = new CannonBallRenderer(new Dimension(400, 400));
        cb_canvas = display;
        cbr = new CannonBallRenderer(new Dimension(400, 400));
    }

    public void test() {
        cbr.redraw(cbr.l_statics | cbr.l_balloids);
        cbr.redraw(cbr.l_background);
    }

    public Renderer renderer() { return cbr; }
   

}*/

