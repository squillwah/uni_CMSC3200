
package CannonBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

// Extra feature ideas:
//
//  Readding trails. Very easy to do with new layer renderer.
//  Different backgrounds for different planets.
//  Use images for things, like the ball and cannon.
//  Have configurable canon or ball shapes, colors, etc.
//  Make scrollwheel adjust canon angle.

public class CannonBall implements runnable {
    private static final long SerialVersionUID = 124987123L;
   
    // Frame and panels
    private Frame window;
    private Panel pnl_display;
    private Panel pnl_controls;

    // The game logic, drawing system.
    private CannonBallEngine engine;
    private MultiBufferedCanvas display;

    // Elements of UI
    private MenuBar menubar;
    private Menu     mnu_control;
    private MenuItem mnu_control_itm_run;
    private MenuItem mnu_control_itm_pause;
    private MenuItem mnu_control_itm_restart;
    private MenuItem mnu_control_itm_quit;
    private Menu     mnu_parameters;
    private Menu     mnu_parameters_mnu_size;
    private MenuItem mnu_parameters_mnu_size_itm_xsmall;
    private MenuItem mnu_parameters_mnu_size_itm_small;
    private MenuItem mnu_parameters_mnu_size_itm_medium;
    private MenuItem mnu_parameters_mnu_size_itm_large;
    private MenuItem mnu_parameters_mnu_size_itm_xlarge;
    private Menu     mnu_parameters_mnu_speed;
    private MenuItem mnu_parameters_mnu_speed_itm_xslow;    // Can change these to parallel arrays later, use funny names and stuff. Ig this is akin to "difficulty". 
    private MenuItem mnu_parameters_mnu_speed_itm_slow;     // A map would work too.
    private MenuItem mnu_parameters_mnu_speed_itm_normal;
    private MenuItem mnu_parameters_mnu_speed_itm_fast;
    private MenuItem mnu_parameters_mnu_speed_itm_xfast;
    private Menu     mnu_environment;
    private MenuItem mnu_environment_itm_mercury;
    private MenuItem mnu_environment_itm_venus;
    private MenuItem mnu_environment_itm_earth;
    private MenuItem mnu_environment_itm_mars;
    private MenuItem mnu_environment_itm_jupiter;
    private MenuItem mnu_environment_itm_saturn;
    private MenuItem mnu_environment_itm_uranus;
    private MenuItem mnu_environment_itm_neptune;
    private MenuItem mnu_environment_itm_pluto;
    private Scrollbar sb_cannon_force;
    private Scrollbar sb_cannon_angle;
    private Label lbl_cannon_force;
    private Label lbl_cannon_angle;
    private Label lbl_time;
    private Label lbl_score_ball;
    private Label lbl_score_player;

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
    private CannonBallEngine.BACKGROUNDS next_background;

    private void init_ui() {
        menubar = new MenuBar();
        mnu_control             = menubar.add(new Menu("Control"));
        mnu_control_itm_run     = mnu_control.add(new MenuItem("Run"));
        mnu_control_itm_pause   = mnu_control.add(new MenuItem("Pause"));
        mnu_control_itm_restart = mnu_control.add(new MenuItem("Restart"));
        mnu_control_itm_quit    = mnu_control.add(new MenuItem("Quit"));
        mnu_parameters                     = menubar.add(new Menu("Parameters"));
        mnu_parameters_mnu_size            = mnu_parameters.add(new Menu("Size"));
        mnu_parameters_mnu_size_itm_xsmall = mnu_parameters_mnu_size.add(new MenuItem("xsmall"));
        mnu_parameters_mnu_size_itm_small  = mnu_parameters_mnu_size.add(new MenuItem("small"));
        mnu_parameters_mnu_size_itm_medium = mnu_parameters_mnu_size.add(new MenuItem("medium"));
        mnu_parameters_mnu_size_itm_large  = mnu_parameters_mnu_size.add(new MenuItem("large"));
        mnu_parameters_mnu_size_itm_xlarge = mnu_parameters_mnu_size.add(new MenuItem("xlarge"));
        mnu_parameters_mnu_speed            = mnu_parameters.add(new Menu("Speed"));
        mnu_parameters_mnu_speed_itm_xslow  = mnu_parameters_mnu_speed.add(new MenuItem("xslow"));
        mnu_parameters_mnu_speed_itm_slow   = mnu_parameters_mnu_speed.add(new MenuItem("slow"));
        mnu_parameters_mnu_speed_itm_normal = mnu_parameters_mnu_speed.add(new MenuItem("normal"));
        mnu_parameters_mnu_speed_itm_fast   = mnu_parameters_mnu_speed.add(new MenuItem("fast"));
        mnu_parameters_mnu_speed_itm_xfast  = mnu_parameters_mnu_speed.add(new MenuItem("xfast"));
        mnu_environment = menubar.add(new Menu("Environment"));
        mnu_environment_itm_mercury = mnu_environment.add(new MenuItem("Mercury"));
        mnu_environment_itm_venus   = mnu_environment.add(new MenuItem("Venus"));
        mnu_environment_itm_earth   = mnu_environment.add(new MenuItem("Earth"));
        mnu_environment_itm_mars    = mnu_environment.add(new MenuItem("Mars"));
        mnu_environment_itm_jupiter = mnu_environment.add(new MenuItem("Jupiter"));
        mnu_environment_itm_saturn  = mnu_environment.add(new MenuItem("Saturn"));
        mnu_environment_itm_uranus  = mnu_environment.add(new MenuItem("Uranus"));
        mnu_environment_itm_neptune = mnu_environment.add(new MenuItem("Neptune"));
        mnu_environment_itm_pluto   = mnu_environment.add(new MenuItem("PLUTO"));
        Scrollbar sb_cannon_force = new Scrollbar(Scrollbar.HORIZONTAL);
        Scrollbar sb_cannon_angle = new Scrollbar(Scrollbar.HORIZONTAL);
        Label lbl_cannon_force = new Label("Force: ?px/s");
        Label lbl_cannon_angle = new Label("Angle: ?deg");
        Label lbl_time         = new Label("Time: ?s");
        Label lbl_score_ball   = new Label("Bubble: ");
        Label lbl_score_player = new Label("You: ");
    }



    public CannonBall(Dimension initial_size) {
        window = new Frame("CannonBubbles");
        window.setLayout(new BorderLayout());
        window.setMinimumSize(initial_size);
        
        menubar = new MenuBar();    window.setMenuBar(menubar);
        pnl_display = new Panel();  window.add("Center", pnl_display);
        pnl_controls = new Panel(); window.add("South", pnl_controls);

        //display = new MultiBufferedCanvas(game.renderer());
        display = new MultiBufferedCanvas();
        game = new CannonBallEngine(initial_size, display);                   //game.set_world_size()
       
        // Plug game into display
        game.set_display(display);


        pnl_display.add("Center", display);
        display.setSize(initial_size);
        
        //validate() ? 
        window.setVisible(true);

        display.repaint();
        try {Thread.sleep(1000);} catch (InterruptedException e) {}
        game.test();
        display.repaint();
    }

    public static void main(String[] args) {
        new CannonBall(new Dimension(600, 600));
    }



    // Right now for testing, just have the canvas resize with frame (using this as componenet listener). Keep game world size static to see how the renderer handles that.
}

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
    
    public MultiBufferedCanvas(/*Dimension size,*/ Renderer r) {
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


