// Lesson 13
//  Addresses:
//   - Vector: addElement, elementAt, removeElementAt, size, clear, firstElement, inserElementAt, isEmpty, lastElement
//   - Math: max, min, abs, signum
//   - Image: createImage, getGraphics
//     - Superclass for all graphical classes.
//     - getGraphcis creates graphics context for drawing to an off-screen image.
//   - Graphics: dispose, drawImage
//     - Abstract base class for all graphics contexts. Allows drawing of graphics onto components.
//     - Some graphics methods:
//       - drawOval, fillOval, drawRect, fillRect, drawString
//       - disposedisposes the graphics context (releasing any resources).
//     - 

// Given Code (reimplimenting lesson 12):

import java.awt.*;
import java.awt.event.*;

public class Lesson13 extends Frame implements WindowListener, MouseListener, MouseMotionListener {
    final int WinLeft = 10; // Top left corner of frame, bring in 10 pixels (why?).
    final int WinTop = 10;

    Point FrameSize = new Point(640, 400);  // Starting frame size (why point?).
    Panel sheet = new Panel();
    List list = new List(13);   // List, thirteen lines.

    public Lesson13() {
        setLayout(new BorderLayout());
        setBounds(WinLeft, WinTop, FrameSize.x, FrameSize.y);   // First two args specify location in parent frame. Since this is main frame, does that equate to starting poition on desktop?
        setBackground(Color.lightGray);
        
        sheet.setLayout(new BorderLayout(0,0)); // Args are gaps between components (hgap, vgap). Zero is same as empty constructor (no gaps).
        sheet.setVisible(true);
        sheet.add("Center", list);

        add("Center", sheet);
        
        addWindowListener(this);
        list.addMouseListener(this);
        list.addMouseMotionListener(this);
        
        setVisible(true);
        validate();
    }

    public static void main(String[] args) {
        new Lesson13();
    }

    public void stop() {
        removeWindowListener(this);
        list.removeMouseListener(this);
        list.removeMouseMotionListener(this);
        dispose();
    }

    public void windowClosing(WindowEvent e) { stop(); }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) { list.add(e.paramString()); }
    public void windowActivated(WindowEvent e) { list.add(e.paramString()); }
    public void windowDeactivated(WindowEvent e) { list.add(e.paramString()); }
    public void windowIconified(WindowEvent e) { list.add(e.paramString()); }
    public void windowDeiconified(WindowEvent e) { list.add(e.paramString()); }

    public void mousePressed(MouseEvent e) {
        String button = "";
        if (e.getButton() == MouseEvent.BUTTON1) button = "Left";
        else if (e.getButton() == MouseEvent.BUTTON2) button = "Center";  // Lesson doesn't have these as elses. Why? Is the else condition less efficient than sequential ifs in a micro way? That doesn't sound true.
        else if (e.getButton() == MouseEvent.BUTTON3) button = "Right";
        list.add(button + " mouse button " + e.getButton() + " pressed");
    }
    public void mouseReleased(MouseEvent e) {
        list.add("Mouse button " + e.getButton() + " released");
    }
    public void mouseClicked(MouseEvent e) {
        list.add("Mouse Clicked" + e.getClickCount() + " clicks");
    }
    public void mouseMoved(MouseEvent e) {
        //list.add(e.paramString());
    }
    public void mouseDragged(MouseEvent e) {
        list.add(e.paramString());
    }
    public void mouseEntered(MouseEvent e) {
        list.add(e.paramString());
    }
    public void mouseExited(MouseEvent e) {
        list.add(e.paramString());
    }
}

// Recommended modifications to be made upon program 4:

//  Apply a BorderLayout to the Frame
//    setLayout(new BorderLayout());

//  Create a Panel (perhaps called 'sheet') for the drawing object (perhaps called 'Ball') and apply a BorderLayout to it
//    private Panel sheet = new Panel();
//    sheet.setLayout(new BorderLayout(0,0));

//  Add the drawing object ('Ball') to the center of the sheet
//    sheet.add("Center", Ball);

//  Create a Panel (perhaps called 'control') for the scrollbars/Buttons/Labels. Use a gridBagLayout.
//    private Panel control = new Panel();
//    control.setLayout(gbl)    // will have to create a gbl object

//  Add the sheet Panel to the Center of the Frame and the control Panel to the South of the Frame
//    add("Center", sheet);
//    add("South", control);

//  Don't forget other pieces like setVisible, etc.

// Add the MouseListener and MouseMotionListener and related event handlers
//    implements MouseListener, MouseMotionListener
//    Ball.addMouseMotionListener(this);
//    Ball.addMouseListener(this);
//    Ball.removeMouseMotionListner(this);
//    Ball.removeMouseListener(this);
//    public void mouseDragged(MouseEvent e) {}
//    public void mouseReleased(MouseEvent e) {}
//    public void mouseClicked(MouseEvent e) {}
//    public void mouseEntered(MouseEvent e) {}
//    public void mouseExited(MouseEvent e) {}
//    public void mouseMoved(MouseEvent e) {}

// Since we'll be using double buffering (and no longer want tails), change the update() method to paint() and remove the old paint that called update.
//  In java, drawing is suppoed to be done all with paint(), then redrawing calls update() which clears the screen. We overrode that because we didn't want to clear the screen.
// We will draw one Graphics onto the backbuffer while the previous Graphics gets displayed on the frontbuffer (screen). The frontbuffer is exchanged with the backbuffer when the new graphics are done drawing.

// Code for a backbuffer:
/*
Image buffer;
Graphics g;

public void paint(Graphics cg) {
    buffer = createImage(width, height);
    // Check if graphics g exists, remove if does. 
    if (g != null) g.dispose();
    // Set graphics to new graphics.
    g = buffer.getGraphics();
    // Do all the drawing, fills, etc. on this Graphics (g).
    g.setColor(Color.red);
    g.fillOval((int)ball.getX(),(int)ball.getY(), (int)ball.getWidth(),(int)ball.getHeight());
    g.setColor(Color.black);
    g.drawOval((int)ball.getX(),(int)ball.getY(), (int)ball.getWidth(),(int)ball.getHeight());
    // At the very end of paint, switch the graphics.
    cg.drawImage(buffer, 0, 0, null);
}
*/
