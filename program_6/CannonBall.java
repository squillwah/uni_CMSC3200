
package CannonBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class CannonBall {
    private static final long SerialVersionUID = 124987123L;
    
    private Frame window;
    private CannonBallEngine game;
    private MultiBufferedCanvas display;

    private MenuBar menubar;
    private Panel pnl_display;
    private Panel pnl_controls;

    public CannonBall(Dimension initial_size) {
        
        window = new Frame("CannonBubbles");
        window.setLayout(new BorderLayout());
        
        menubar = new MenuBar();    window.setMenuBar(menubar);
        pnl_display = new Panel();  window.add("Center", pnl_display);
        pnl_controls = new Panel(); window.add("South", pnl_controls);

        game = new CannonBallEngine(initial_size);                   //game.set_world_size()
        display = new MultiBufferedCanvas(game.renderer());
        
        pnl_display.add("Center", display);
        
        //validate() ? 
        window.setVisible(true);
    }

    // Right now for testing, just have the canvas resize with frame (using this as componenet listener). Keep game world size static to see how the renderer handles that.
}

class CannonBallEngine {
    private Renderer game_renderer = new Renderer(6) {
        // Give the render layers more descriptive names.
        public final int l_background = LAYERS[0];  // This should work, as long as the super constructor executes before extension initialization.
        public final int l_statics    = LAYERS[1];
        public final int l_bubbles    = LAYERS[2];
        public final int l_balloids   = LAYERS[3];
        public final int l_cannon     = LAYERS[4];
        public final int l_dragbox    = LAYERS[5];
    
        public void draw(int layer, Graphics g) {
            //switch (layer) {
                // Could very easily support drawing multiple layers onto one graphics here, if that ever becomes desirable.
                if (layer == l_background:  draw_background(g); break;
                case l_statics:     draw_statics(g);    break;
                case l_bubbles:     draw_bubbles(g);    break;
                case l_balloids:    draw_balloids(g);   break;
                case l_cannon:      draw_cannon(g);     break;
                case l_dragbox:     draw_dragbox(g);    break;
                default: 
                    System.out.println("err: bad layer code in draw");
            //}
        }
    
        private void draw_background(Graphics g) {
            // @todo depending on the planet, can make the background different.
        }
    
        private void draw_statics(Graphics g) {
            // @todo draw rectangles here, any other static objects
        }
    
        private void draw_bubbles(Graphics g) {
            // @todo draw the bubbles (moving targets) here
        }
    
        private void draw_balloids(Graphics g) {
            // @todo bullets here
        }
    
        private void draw_cannon(Graphics g) {
            // @todo draw the cannon
        }
    
        private void draw_dragbox(Graphics g) {
            // @todo draw the dragbox
        }
    };

    public CannonBallEngine(Dimension world_size) {}
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
    public void draw(int layer, Graphics g);
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
}

class MultiBufferedCanvas extends Canvas {
    private static final long SerialVersionUID = 12412410L;

    private Image backbuff;
    private Renderer renderer;
    private BufferedImage[] layerbuffs;
    private Graphics gra;
    
    public MultiBufferedCanvas(Dimension size, Renderer r) {
        setSize(size);
        setBackground(Color.white);
        renderer = r;
        layerbuffs = new BufferedImage[renderer.LAYER_COUNT];
        for (int i = 0; i < renderer.LAYER_COUNT; i++) 
            layerbuffs[i] = new BufferedImage(r.resolution.width, r.resolution.height, TYPE_INT_ARGB);  // Init empty layers
    }

    public void update(Graphics g) {
        // Iterate through layers, redraw if marked for redraw.
        int draws = renderer.redraws();
        for (int i = 0; i < renderer.LAYER_COUNT; i++) {
            if (draws & renderer.LAYERS[i]) {
                layerbuffs[i] = new BufferedImage(r.resolution.width, r.resolution.height, TYPE_INT_ARGB);
                gra = layerbuffs[i].getGraphics();
                renderer.draw(renderer.LAYERS[i], g);
                gra.dispose();
            }
        }
        if (draws) paint(g);  // Only recompose layers if there was a redraw
    }

    public void paint(Graphics g) {
        backbuff = createImage(r.resolution.width, r.resolution.height);    // Blank, to wipe last frame and preserve first layer.
        for (BufferedImage layer : layerbuffs) 
            backbuff.drawImage(layer, 0, 0, null);
        g.drawImage(backbuff);
    }
}


