
//package CannonBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class uitest implements ActionListener, AdjustmentListener, ItemListener {
    private static final long SerialVersionUID = 124987123L;
   
    // Frame and panels
    private Frame window;
    private Panel pnl_display, pnl_controls;

    // The game logic, drawing system.
    private CannonBallEngine engine;
//    private MultiBufferedCanvas display;

    // Elements of UI: MenuItems, ScrollBars, Labels
    private MenuBar menubar;
    private Menu mnu_control, mnu_parameters, mnu_environment, mnu_parameters_mnu_size, mnu_parameters_mnu_speed;
    private MenuItem[] mnu_control_itms;                        private final byte run     = 0, pause = 1, restart = 2, quit  = 3,              NUM_CONTROLS = 4;   // Array access offset constants.
    private CheckboxMenuItem[] mnu_parameters_mnu_size_itms;    private final byte xsmall  = 0, small = 1, medium  = 2, large = 3, xlarge  = 4, NUM_SIZES    = 5;
    private CheckboxMenuItem[] mnu_parameters_mnu_speed_itms;   private final byte xslow   = 0, slow  = 1, normal  = 2, fast  = 3, xfast   = 4, NUM_SPEEDS   = 5;
    private CheckboxMenuItem[] mnu_environment_itms;            private final byte mercury = 0, venus = 1, earth   = 2, mars  = 3, jupiter = 4, saturn = 5, uranus = 6, neptune = 7, pluto = 8, NUM_PLANETS = 9;
    private Label lbl_cannon_force, lbl_cannon_angle, lbl_score_ball, lbl_score_player, lbl_time;   
    private Scrollbar sb_cannon_force, sb_cannon_angle;                                                 // @todo sometime maybe idk, Menu and Menubar support a getMenu and getItem Count method, we could use that to dynamically traverse the menubar structure to find things instead of defining it all here and in constructor.

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
    // Related data with update events.
    private Rectangle dragbox;
    private double next_force;
    private double next_angle;
    private double next_speed;      // This begs the question, should the Engine just take a double value to set as ball speed, or should it know about speed and size presets?
    private double next_size;
    private double next_gravity;    // What about our planet presets too? Should it know the gravity of mars, or do we do that here and just supply the gravity value?

    public uitest(Dimension initial_size) {
        engine = new CannonBallEngine();
        
        // Frame
        window = new Frame();
        window.setTitle("CannonBubbles");
        window.setMinimumSize(initial_size);
        window.setBackground(Color.black);
        window.setLayout(new BorderLayout());
        // Menubar, MenuItems. 
        menubar = new MenuBar();
        mnu_control               = menubar.add(new Menu("Control"));
        mnu_control_itms          = new MenuItem[4];
        mnu_control_itms[run]     = mnu_control.add(new MenuItem("Run"));
        mnu_control_itms[pause]   = mnu_control.add(new MenuItem("Pause"));
        mnu_control_itms[restart] = mnu_control.add(new MenuItem("Restart"));
        mnu_control.addSeparator();
        mnu_control_itms[quit]    = mnu_control.add(new MenuItem("Quit"));
        mnu_parameters                       = menubar.add(new Menu("Parameters"));
        mnu_parameters_mnu_size              = (Menu)mnu_parameters.add(new Menu("Size"));  // Possibly buggy cast
        mnu_parameters_mnu_size_itms         = new CheckboxMenuItem[5];
        mnu_parameters_mnu_size_itms[xsmall] = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("xsmall"));
        mnu_parameters_mnu_size_itms[small]  = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("small"));
        mnu_parameters_mnu_size_itms[medium] = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("medium"));
        mnu_parameters_mnu_size_itms[large]  = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("large"));
        mnu_parameters_mnu_size_itms[xlarge] = (CheckboxMenuItem)mnu_parameters_mnu_size.add(new CheckboxMenuItem("xlarge"));
        mnu_parameters_mnu_speed              = (Menu)mnu_parameters.add(new Menu("Speed"));
        mnu_parameters_mnu_speed_itms         = new CheckboxMenuItem[5];
        mnu_parameters_mnu_speed_itms[xslow]  = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xslow"));
        mnu_parameters_mnu_speed_itms[slow]   = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("slow"));
        mnu_parameters_mnu_speed_itms[normal] = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("normal"));
        mnu_parameters_mnu_speed_itms[fast]   = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("fast"));
        mnu_parameters_mnu_speed_itms[xfast]  = (CheckboxMenuItem)mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xfast"));
        mnu_environment = menubar.add(new Menu("Environment"));
        mnu_environment_itms          = new CheckboxMenuItem[9];
        mnu_environment_itms[mercury] = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Mercury")); // @todo Could we think of a way to do all this in the Engine, and attach the Engine as listener? Just pass the menubar or menuitem back to this frame somehow? What about the Scrolls too?
        mnu_environment_itms[venus]   = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Venus"));
        mnu_environment_itms[earth]   = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Earth"));
        mnu_environment_itms[mars]    = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Mars"));
        mnu_environment_itms[jupiter] = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Jupiter"));
        mnu_environment_itms[saturn]  = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Saturn"));
        mnu_environment_itms[uranus]  = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Uranus"));
        mnu_environment_itms[neptune] = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("Neptune"));
        mnu_environment_itms[pluto]   = (CheckboxMenuItem)mnu_environment.add(new CheckboxMenuItem("PLUTO"));
        // Panels
        pnl_display  = (Panel)window.add("Center", (new Panel()));  // Hopefully this cast doesn't cause issue.
        pnl_controls = (Panel)window.add("South", (new Panel()));
        // Scrolls
        Scrollbar sb_cannon_force = new Scrollbar(Scrollbar.HORIZONTAL);
        Scrollbar sb_cannon_angle = new Scrollbar(Scrollbar.HORIZONTAL);
        // Labels
        Label lbl_cannon_force = new Label("Force: ?px/s");
        Label lbl_cannon_angle = new Label("Angle: ?deg");
        Label lbl_time         = new Label("Time: ?s");
        Label lbl_score_ball   = new Label("Bubble: ");
        Label lbl_score_player = new Label("You: ");
       
        // Attach Listeners 
        for (MenuItem mi : mnu_control_itms) mi.addActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms) mi.addItemListener(this);
       
        // Setup panels
        pnl_display.setBackground(Color.gray);
        pnl_display.add("Center", (new Canvas()));
        pnl_controls.setBackground(Color.lightGray);
        pnl_controls.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); // @todo configure this gridbag stuff
        pnl_controls.add(sb_cannon_force, gbc);
        pnl_controls.add(sb_cannon_angle, gbc);
        pnl_controls.add(lbl_cannon_force, gbc);
        pnl_controls.add(lbl_cannon_angle, gbc);
        pnl_controls.add(lbl_time, gbc);
        pnl_controls.add(lbl_score_ball, gbc);
        pnl_controls.add(lbl_score_player, gbc);

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
        engine.set_gravity(10);
        engine.set_bubble_size(10);
        engine.set_bubble_speed(10);


        //pnl_display.add("Center", display);
        //display.setSize(initial_size);
        
        window.validate(); //? 
        window.setVisible(true);
    }

    public static void main(String[] args) {
        new uitest(new Dimension(600, 600));
    }

    private void exit() {
        for (MenuItem mi : mnu_control_itms) mi.removeActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms) mi.removeItemListener(this);
        window.dispose();
        System.exit(0);
    }



    // ! the critical consideration is whether or not to have all these listeners as part of this main frame class, or some/all implemented as part of the engine. Or something other third thing? I don't think the renderer would have any need.

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
    private int find_mitem(/*Checkbox*/MenuItem[] radios, int size, Object item) {
        int i;
        for (i = 0; i < size && item != radios[i]; i++);
        if (!(i < size)) i = -1;
        return i;
    }

    // Tick given radio on and clear all others in array.
    private void set_mradio(CheckboxMenuItem[] radios, CheckboxMenuItem radio) {
        for (CheckboxMenuItem r : radios) r.setState(false);
        radio.setState(true);
    }
  
    public void itemStateChanged(ItemEvent e) {
        Object item = e.getSource();
        //if (item instanceof CheckboxMenuItem) 
        int radio;
        if ((radio = find_mitem(mnu_parameters_mnu_size_itms, NUM_SIZES, item)) > -1) {
            set_mradio(mnu_parameters_mnu_size_itms, (CheckboxMenuItem)item);
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
            set_mradio(mnu_parameters_mnu_speed_itms, (CheckboxMenuItem)item);
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
            set_mradio(mnu_environment_itms, (CheckboxMenuItem)item);
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
        }
    }

    // @todo would be far better to use an array for these radio settings and just iterate that in itemStateChanged instead of doing this
    // Or if we can get the parent menu of the MenuItem, then iterate that, we could use that to clear.
    //private void clear_radio_parameters_size() { mnu_parameters_mnu_size_itm_xsmall = mnu_parameters_mnu_size_itm_small = mnu_parameters_mnu_size_itm_medium = mnu_parameters_mnu_size_itm_large = mnu_parameters_mnu_size_itm_xlarge = false; }
    //private void clear_radio_parameters_speed() { mnu_parameters_mnu_speed_itm_xslow = mnu_parameters_mnu_speed_itm_slow = mnu_parameters_mnu_speed_itm_normal = mnu_parameters_mnu_speed_itm_fast = mnu_parameters_mnu_speed_itm_xfast = false; }
    //private void clear_radio_environment() { mnu_environment_itm_mercury = mnu_environment_itm_venus = mnu_environment_itm_earth = mnu_environment_itm_mars = mnu_environment_itm_jupiter = mnu_environment_itm_saturn = mnu_environment_itm_uranus = mnu_environment_itm_neptune = mnu_environment_itm_pluto = false; }


    // Right now for testing, just have the canvas resize with frame (using this as componenet listener). Keep game world size static to see how the renderer handles that.
}

class CannonBallEngine {
    
    private boolean paused; // If we have paused here, but the main loop is in main, and all the setting of rects and changing of sizes is of course done here, then pausing may cause issues with setting those during pause.
                            // Unless, pause doesn't stop ticks from doing anything entirely, it just skips the velocity/collision parsing stuff. Still allows for changing size, speed vars, and adding/deling rects. And moving the cannon. Ig. It should be that the cannon can fire multiple bullets, with a configurable rate of fire. But that's only during not pause, obv. Should we allow angle adjustment during a pause? Could be a setting. What does rubric say?


    public CannonBallEngine() {
        paused = true;
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
}

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
    }


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
   

    // Renderer for the game.
    // Implements how/what gets drawn to the graphics.
    // Used by the MultiBufferedCanvas during paints.                   // @todo should dragbox be done through CannonBallEngine/Game, or through this renderer?
    class CannonBallRenderer extends Renderer {
        // Give the render layers more descriptive names.
        public final int l_background, l_statics, l_bubbles, l_balloids, l_cannon, l_dragbox;
        // Dragbox is done in renderer. Probably shouldn't be.
        private Rectangle dragbox;
        public CannonBallRenderer(Dimension resolution) {
            super(6, resolution);
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
            //}
        }
    
        private void draw_background(Graphics g) {
            // @todo depending on the planet, can make the background different.
            //g.drawRect(0, 0, res_x(), res_y());
            g.setColor(Color.red);
            g.drawRect(10, 10, res_x()-20, res_y()-20);     // it is indeed limited to rendered res, even when overlayed (rect gets cutoff)
            System.out.println("background");
        }
    
        private void draw_statics(Graphics g) {
            g.setColor(Color.blue);
            g.fillRect(res_x()/2, 2, 40, 40);
            // @todo draw rectangles here, any other static objects
        }
    
        private void draw_bubbles(Graphics g) {
            // @todo draw the bubbles (moving targets) here
        }
    
        private void draw_balloids(Graphics g) {
            // @todo bullets here
            g.setColor(Color.green);
            g.drawRect(res_x()/2 + 35, 0, 20, 20);     // it is indeed limited to rendered res, even when overlayed (rect gets cutoff)
        }
    
        private void draw_cannon(Graphics g) {
            // @todo draw the cannon
        }
    
        private void draw_dragbox(Graphics g) {
            // @todo draw the dragbox
            if (dragbox != null) g.drawRect(dragbox);
        }

        public void set_dragbox(Rectangle box) {    // Little jank. 
            dragbox = box.getRectangle(); 
            redraw(l_dragbox);
        }
    }
}

// Abstract class for Game to extend (and define rendering logic with).
abstract class Renderer { 
    private Dimension resolution;   // The resolution thing is an interesting and sticky problem. Should the internal rendering be normalized and adjusted to fit in this, resizing everything with window? That probably goes aginst the lesson plan.
    public final int LAYER_COUNT;
    public final int[] LAYERS;      // Array of bitstrings, each layer being a unique bit
    private int redraw;             // Flag for layers that need redrawing. Sum of layer codes.
    
    public Renderer(int layer_count, Dimension res) { 
        LAYER_COUNT = Math.min(8, Math.max(1, layer_count));    // 1 -> 8 layer limit
        LAYERS = new int[LAYER_COUNT];
        int layer_code = 1; //0b00000001
        for (int i = 0; i < LAYER_COUNT; i++) {
            LAYERS[i] = layer_code;
            layer_code *= 2;    //0b00000010 -> 0b00000100 -> ...
        }
        redraw = 0; 
        resolution = res.getSize();
    }
    
    // Draw logic for given layer. 
    // Overridden by game, called by MBC during paint.
    public abstract void draw(int layer, Graphics g);

    // Flag given layer for redraw.
    // For example, after balls have been moved or a rectangle was added.
    public void redraw(int layer) { 
        redraw |= layer; 
    }
    // Check layers flagged for redraw, clear redraw.
    public int redraws() { 
        int layers = redraw; 
        redraw = 0; 
        return layers; 
    }

    // Get resolution (dimensions of layer buffers).
    public int res_x() { return resolution.width; }
    public int res_y() { return resolution.height; }

    //private MultiBufferedCanvas;    // Instead of passing the canvas in Game for it to set renderer, we could have it call repaint through the renderer. Though this is probably a bad idea.
    //public void set_canvas() 
    //public void repaint()
}

class MultiBufferedCanvas extends Canvas {
    private static final long SerialVersionUID = 12412410L;

    private Image backbuff;
    private Renderer renderer;
    private BufferedImage[] layerbuffs;
    private Graphics gfx;
    
    public MultiBufferedCanvas(/*Dimension size, Renderer r) {
        //setSize(size);    // Sizes of renderer, canvas, and game world are seperate. We'll need to make sure they stay in sync, unless we want them out of it.
        setBackground(Color.white);
        set_renderer(r);
    }
    public MultiBufferedCanvas() {
        // A default renderer, when none given.
        this(new Renderer(1, new Dimension(100, 100)) { 
            public void draw(int layer, Graphics g) { 
                g.setColor(Color.red);
                g.drawString("there's nothing", res_x()/2, res_y()/2 ); 
            } 
        });
        renderer.redraw(renderer.LAYERS[0]);
    }

    public void set_renderer(Renderer r) {
        renderer = r;
        layerbuffs = new BufferedImage[renderer.LAYER_COUNT];
        for (int i = 0; i < renderer.LAYER_COUNT; i++) 
            layerbuffs[i] = new BufferedImage(renderer.res_x(), renderer.res_y(), BufferedImage.TYPE_INT_ARGB);  // Init empty layers
    }

    public void update(Graphics g) {
        // Iterate through layers, redraw if marked for redraw.
        int draws = renderer.redraws();
        for (int i = 0; i < renderer.LAYER_COUNT; i++) {
            if ((draws & renderer.LAYERS[i]) > 0) {
                layerbuffs[i] = new BufferedImage(renderer.res_x(), renderer.res_y(), BufferedImage.TYPE_INT_ARGB);
                gfx = layerbuffs[i].getGraphics();
                renderer.draw(renderer.LAYERS[i], gfx);
                System.out.println("draw");
                gfx.dispose();
                //g.drawImage(layerbuffs[0], 0, 0, null);
                //return;
            }
        }
        if (draws > 0) paint(g);  // Only recompose layers if there was a redraw
    }

    public void paint(Graphics g) {
        backbuff = createImage(renderer.res_x(), renderer.res_y());    // Blank, to wipe last frame and preserve first layer.
        gfx = backbuff.getGraphics();
        //gfx.fillOval(50, 50, 10, 10);
        for (BufferedImage layer : layerbuffs) {
            gfx.drawImage(layer, 0, 0, null);
            System.out.println("drawg" + layer);
        }
        gfx.dispose();
        g.drawImage(backbuff, 0, 0, null);
        //g.drawImage(layerbuffs[0], 0, 0, null);
    }
}

*/
