// Code from Lesson 12
/*
Panel sheet = new Panel();      // Panel for drawing object
Panel control = new Panel();    // Panel for control buttons and scrollbars

setLayout(new BorderLayout());              // Set main class (extending rame) to border layout
sheet.setLayout(new BorderLayout(0,0));     // Set sheet panel (with the drawing canvas) to border layout
sheet.add("Center", Ball);                  // Add the Ball drawing object to the center of sheet panel.                                            
GridBagLayout gbl = new GridBagLayout();    // New GridBagLayout, for use in control panel.

control.setLayout(gbl);

add("Center", sheet);       // Add sheet and control panels to main frame.
add("South", control);
*/

// Lesson 12 test program:
//  Create a panel with a BorderLayout and place it in the center of the application.
//  Create a list and add it to the center of the Panel.
//  Add the MouseListener to the Panel.
//  Create the mouse event handlers and add text to the List when an event is triggered.
//  Then add the MouseMotionListener with it's event handlers which also add to the List.

import java.awt.*;
import java.awt.event.*;

public class Lesson12 extends Frame implements MouseListener, MouseMotionListener {
    private Panel list_panel;
    private List mouse_event_list;

    public Lesson12() {
       init(); 
    }

    public void init() {
        // Window settings.
        setTitle("Lesson 12 MouseListener Test");
        setPreferredSize(new Dimension(600, 400));
        setMinimumSize(getPreferredSize());     //setBounds(10, 10, 400, 600);
        setLayout(new BorderLayout());
       
        // List panel.
        list_panel = new Panel();
        list_panel.setLayout(new BorderLayout(0,0)); // ? What do the arguments do?
        list_panel.addMouseListener(this);
        list_panel.addMouseMotionListener(this);    // Can't get these to trigger on the panel, could just be my WM.
        add("Center", list_panel); 
       
        // List object in list panel. 
        mouse_event_list = new List();
        list_panel.add(mouse_event_list);
        mouse_event_list.addMouseListener(this);
        mouse_event_list.addMouseMotionListener(this);

        validate();         // ?
        setVisible(true);
    }
    public void close() {
        list_panel.removeMouseListener(this);
        list_panel.removeMouseMotionListener(this);
        mouse_event_list.removeMouseListener(this);
        mouse_event_list.removeMouseMotionListener(this);
        dispose();
    }

    public static void main(String[] args) {
        new Lesson12();
    }

    // MouseListener
    public void mousePressed(MouseEvent e) { mouse_event_list.add(e.paramString()); }
    public void mouseReleased(MouseEvent e) { mouse_event_list.add(e.paramString()); }
    public void mouseClicked(MouseEvent e) { System.out.println(e.paramString()); close(); }
    public void mouseEntered(MouseEvent e) { mouse_event_list.add(e.paramString()); }
    public void mouseExited(MouseEvent e) { mouse_event_list.add(e.paramString()); }

    // MouseMotionListener
    public void mouseDragged(MouseEvent e) { mouse_event_list.add(e.paramString()); }
    public void mouseMoved(MouseEvent e) { System.out.println(e.paramString()); }
}
    
