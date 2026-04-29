package spatter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import EDU.emporia.mathbeans.*;
import EDU.emporia.mathtools.*;
import java.util.*;

// Todo:
//  - Fix the tail/tip points of the vel vector, so they don't leave axes.
//  - Zoom out the graph, so the wall is easier to see.
//  - Discover and fix the "error in calculation".

public class SpatterApplication extends JFrame implements WindowListener, ActionListener {
    final double gravity=4;
    final double wallDistance=6;
   
    // Time of the spatter sim/anim:
    double t=0;
    // Tip+tail points of initial blood vel:
    double x1=0, y1=4, x2=1, y2=5;
    
    // Pre drag event positions, used to apply the tail's Y delta to the tip.
    //double oldx1=x1, oldy1=y1, oldx2=x2, oldy2=y2;  
    
    // Mouse drag flags for tip/tail points:
    boolean dragging1=false, dragging2=false;
    
    double spatterWidth=0, spatterLength=0; // Unused.
    
    
    javax.swing.Timer animationTimer;
    boolean move=false;
    private boolean isStandalone = false;
    JPanel jPanel1 = new JPanel();
    
    MathGrapher graph = new MathGrapher();
    MathGrapher dropShapeGraph = new MathGrapher();
    SymbolicParametricCurve bloodPath = new SymbolicParametricCurve();
    SymbolicParametricCurve directionVector = new SymbolicParametricCurve();
    SymbolicParametricCurve wall = new SymbolicParametricCurve();
    
    JLabel jLabel1 = new JLabel();
    JButton trackButton = new JButton();
    JLabel floorOrWallLabel = new JLabel();
    Ellipse spatterEllipse = new Ellipse();
    MathTextField widthMathTextField = new MathTextField();
    MathTextField lengthMathTextField = new MathTextField();
    JLabel widthLabel = new JLabel();
    JLabel lengthLabel = new JLabel();
    MathTextField angleMathTextField = new MathTextField();
    JLabel jLabel2 = new JLabel();
    JButton resetButton = new JButton();

    JLabel lbl_initial_angle = new JLabel();
    JLabel lbl_initial_height = new JLabel();
    JLabel lbl_initial_velocity = new JLabel();

    MathTextField mtf_initial_angle = new MathTextField();
    MathTextField mtf_initial_height = new MathTextField();
    MathTextField mtf_initial_velocity = new MathTextField();

    //Get a parameter value
    // Returns the property String of the given property key in the given property object.
    // Defaults to third argument 'def' if invalid 'key' or empty related property.
    // Used nowhere.
    private String getProperty(Properties property, String key, String def) {
	    String temp;
	    try {
	    	temp = property.getProperty(key);
	    	if(temp.equals("")) temp = def;
	    } catch (NullPointerException e) { temp = def; }
	    return temp;
    }

    public static void main(String[] args) {
        try { SpatterApplication s = new SpatterApplication(); } 
        catch (Exception e) { e.printStackTrace(); }
    }

 
    //Component initialization
    public SpatterApplication() throws Exception {
        animationTimer = new javax.swing.Timer(1, this);
        this.setSize(new Dimension(660,440));
        jPanel1.setLayout(null);
        graph.setTraceEnabled(false);
        graph.setF(bloodPath);
        graph.setG(directionVector);
        graph.setGridLines(EDU.emporia.mathbeans.MathGrapher.GRIDOFF);
        graph.setToolTipText("Drag left hand point to adjust height, right hand point to adjust " + "direction and velocity");
        graph.setXMax(6.5);
        graph.setXMin(0.0);
        graph.setYMax(10.0);
        graph.setYMin(0.0);
        graph.setBounds(new Rectangle(140, 5, 364, 390));
        graph.addMouseMotionListener(new SpatterApplication_graph_mouseMotionAdapter(this));
        graph.addMouseListener(new SpatterApplication_graph_mouseAdapter(this));
        dropShapeGraph.setTraceEnabled(false);
        dropShapeGraph.setAxesColor(Color.lightGray);
        dropShapeGraph.setGridColor(Color.lightGray);
        dropShapeGraph.setTitleEnabled(false);
        dropShapeGraph.setXLabel("");
        dropShapeGraph.setXMax(5.0);
        dropShapeGraph.setXMin(-5.0);
        dropShapeGraph.setYLabel("");
        dropShapeGraph.setYMax(5.0);
        dropShapeGraph.setYMin(-5.0);
        dropShapeGraph.setBounds(new Rectangle(507, 7, 131, 124));

        bloodPath.setYFormula("1");
        directionVector.setXFormula("0");

        bloodPath.setTMax(20.0);
        bloodPath.setTMin(0.0);
        directionVector.setTMax(1.0);
        directionVector.setTMin(0.0);
        wall.setXFormula(""+wallDistance);
        wall.setYFormula("t");
        wall.setTMin(0.0);
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 16));
        jLabel1.setText("Blood Spatter");
        jLabel1.setBounds(new Rectangle(7, 17, 133, 38));
        trackButton.setBounds(new Rectangle(23, 81, 101, 39));
        trackButton.setText("Trace path");
        trackButton.addActionListener(new SpatterApplication_trackButton_actionAdapter(this));
        floorOrWallLabel.setHorizontalAlignment(SwingConstants.CENTER);
        floorOrWallLabel.setText("Press Trace path ");
        floorOrWallLabel.setBounds(new Rectangle(517, 135, 114, 28));
        widthMathTextField.setMaxNumberOfCharacters(8);
        widthMathTextField.setEditable(false);
        widthMathTextField.setFont(new java.awt.Font("Dialog", 0, 14));
        widthMathTextField.setHorizontalAlignment(SwingConstants.CENTER);
        widthMathTextField.setMargin(new Insets(1, 1, 1, 1));
        widthMathTextField.setRequestFocusEnabled(true);
        widthMathTextField.setText("");
        widthMathTextField.setBounds(new Rectangle(520, 207, 110, 30));
        lengthMathTextField.setBounds(new Rectangle(521, 271, 110, 30));
        lengthMathTextField.setMaxNumberOfCharacters(8);
        lengthMathTextField.setEditable(false);
        lengthMathTextField.setFont(new java.awt.Font("Dialog", 0, 14));
        lengthMathTextField.setHorizontalAlignment(SwingConstants.CENTER);
        lengthMathTextField.setText("");
        widthLabel.setText("width (in mm):");
        widthLabel.setBounds(new Rectangle(521, 184, 111, 25));
        lengthLabel.setBounds(new Rectangle(520, 248, 112, 25));
        lengthLabel.setText("height (in mm):");
        angleMathTextField.setBounds(new Rectangle(521, 355, 110, 30));
        angleMathTextField.setText("");
        angleMathTextField.setRequestFocusEnabled(true);
        angleMathTextField.setMargin(new Insets(1, 1, 1, 1));
        angleMathTextField.setHorizontalAlignment(SwingConstants.CENTER);
        angleMathTextField.setFont(new java.awt.Font("Dialog", 0, 14));
        angleMathTextField.setEditable(false);
        angleMathTextField.setMaxNumberOfCharacters(10);
        jLabel2.setText("Angle of impact:");
        jLabel2.setBounds(new Rectangle(520, 328, 111, 24));
        resetButton.addActionListener(new SpatterApplication_resetButton_actionAdapter(this));
        resetButton.setText("reset");
        resetButton.addActionListener(new SpatterApplication_resetButton_actionAdapter(this));
        resetButton.setBounds(new Rectangle(23, 142, 101, 39));
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
        this.setResizable(false);
        setVisible(true);			//make it visible
        validate();				//validate the layout
        addWindowListener(this);
        setTitle("Spatter Application");

        jPanel1.add(graph, null);
        jPanel1.add(jLabel1, null);
        jPanel1.add(trackButton, null);
        jPanel1.add(dropShapeGraph, null);
        jPanel1.add(floorOrWallLabel, null);
        jPanel1.add(widthLabel, null);
        jPanel1.add(lengthLabel, null);
        jPanel1.add(lengthMathTextField, null);
        jPanel1.add(widthMathTextField, null);
        jPanel1.add(angleMathTextField, null);
        jPanel1.add(jLabel2, null);
        jPanel1.add(resetButton, null);
        graph.setPointRadius(4);
        graph.updateGraph();
        dropShapeGraph.removeAll();


        // Add new displays

        int sep_y = 10;
        Point pos = new Point(9,190); 
        Dimension size = new Dimension(120, 30); 
        
        lbl_initial_height.setText("Initial Height (m):");
        lbl_initial_height.setBounds(new Rectangle(pos, size));
        lbl_initial_height.setHorizontalAlignment(SwingConstants.CENTER);
        pos.y += size.height;
        mtf_initial_height.setBounds(new Rectangle(pos, size));
        mtf_initial_height.setText("");
        mtf_initial_height.setRequestFocusEnabled(true);
        mtf_initial_height.setMargin(new Insets(1, 1, 1, 1));
        mtf_initial_height.setHorizontalAlignment(SwingConstants.CENTER);
        mtf_initial_height.setFont(new java.awt.Font("Dialog", 0, 14));
        mtf_initial_height.setEditable(false);
        mtf_initial_height.setMaxNumberOfCharacters(10);
        pos.y += size.height + sep_y;
        
        lbl_initial_angle.setText("Initial Angle: ");
        lbl_initial_angle.setBounds(new Rectangle(pos, size));
        lbl_initial_angle.setHorizontalAlignment(SwingConstants.CENTER);
        pos.y += size.height;
        mtf_initial_angle.setBounds(new Rectangle(pos, size));
        mtf_initial_angle.setText("");
        mtf_initial_angle.setRequestFocusEnabled(true);
        mtf_initial_angle.setMargin(new Insets(1, 1, 1, 1));
        mtf_initial_angle.setHorizontalAlignment(SwingConstants.CENTER);
        mtf_initial_angle.setFont(new java.awt.Font("Dialog", 0, 14));
        mtf_initial_angle.setEditable(false);
        mtf_initial_angle.setMaxNumberOfCharacters(10);
        pos.y += size.height + sep_y;
        
        lbl_initial_velocity.setText("Initial Velocity (m):");
        lbl_initial_velocity.setBounds(new Rectangle(pos, size));
        lbl_initial_velocity.setHorizontalAlignment(SwingConstants.CENTER);
        pos.y += size.height;
        mtf_initial_velocity.setBounds(new Rectangle(pos, size));
        mtf_initial_velocity.setText("");
        mtf_initial_velocity.setRequestFocusEnabled(true);
        mtf_initial_velocity.setMargin(new Insets(1, 1, 1, 1));
        mtf_initial_velocity.setHorizontalAlignment(SwingConstants.CENTER);
        mtf_initial_velocity.setFont(new java.awt.Font("Dialog", 0, 14));
        mtf_initial_velocity.setEditable(false);
        mtf_initial_velocity.setMaxNumberOfCharacters(10);
        pos.y += size.height + sep_y;

        jPanel1.add(mtf_initial_angle, null);
        jPanel1.add(lbl_initial_angle, null);
        jPanel1.add(mtf_initial_height, null);
        jPanel1.add(lbl_initial_height, null);
        jPanel1.add(mtf_initial_velocity, null);
        jPanel1.add(lbl_initial_velocity, null);

        set_initial_displays();

        repaint();
    }

    //Get Application information
    public String getApplicationInfo() { return "Application Information"; }
    //Get parameter info
    public String[][] getParameterInfo() { return null; }

    public void stop() { 
        removeWindowListener(this); 
    	dispose();
    	System.exit(0);
    }
    
    public void windowClosing(WindowEvent e) { stop(); }
    public void windowClosed(WindowEvent e) {} public void windowOpened(WindowEvent e) {} 
    public void windowActivated(WindowEvent e) {} public void windowDeactivated(WindowEvent e) {} 
    public void windowIconified(WindowEvent e) {} public void windowDeiconified(WindowEvent e) {}
   
    // Triggered by animation timer. Advances t and runs frame of spatter animation.
    public void actionPerformed(ActionEvent e) {
        Point2D p = bloodPath.getPoint(t);
        graph.plotPoint(p.getX(),p.getY());
        t += 0.02;
        graph.updateGraph();
        System.out.println(" " + Math.toDegrees(angle(t)));

        // Stop animation and display statistics (when hit wall or floor).
        //if(t>wallDistance/(x2-x1)) {
        if (p.getX() > wallDistance) {
            animationTimer.stop();
            floorOrWallLabel.setText("Wall spatter shape");
            lengthLabel.setText("Height (in mm):");
            spatterEllipse.setXRadius((1+Math.random())/2);
            spatterEllipse.setYRadius(spatterEllipse.getXRadius()/Math.cos(angle(t)));
            dropShapeGraph.addGraph(spatterEllipse, Color.RED);
            widthMathTextField.setMathValue(places1(10*spatterEllipse.getXRadius()));
            lengthMathTextField.setMathValue(places1(10*spatterEllipse.getYRadius()));
            angleMathTextField.setMathValue(places1(90-angle(t)*180/Math.PI));
        } else if (p.getY() < graph.getYMin()) {
        //if(t>(((y2-y1)+Math.sqrt((y1-y2)*(y1-y2)+4*gravity*y1)))/(2*gravity)) {
            animationTimer.stop();
            floorOrWallLabel.setText("Floor spatter shape");
            lengthLabel.setText("Length (in mm):");
            spatterEllipse.setXRadius((1+Math.random())/2);
            spatterEllipse.setYRadius(spatterEllipse.getXRadius()/Math.sin(angle(t)));
            dropShapeGraph.addGraph(spatterEllipse, Color.RED);
            widthMathTextField.setMathValue(places1(10*spatterEllipse.getXRadius()));
            lengthMathTextField.setMathValue(places1(10*spatterEllipse.getYRadius()));
            angleMathTextField.setMathValue(places1(angle(t)*180/Math.PI));
            angleMathTextField.setMathValue(places1(Math.toDegrees(angle(t))));
        }
    }

    // Flag closest point for dragging (precedence to tip avoids sticking).
    void graph_mousePressed(MouseEvent e) {
        int xMouse = e.getX(); int yMouse = e.getY();
        dragging2 = ((xMouse-graph.xMathToPixel(x2)) * (xMouse-graph.xMathToPixel(x2)) + 
                     (yMouse-graph.yMathToPixel(y2)) * (yMouse-graph.yMathToPixel(y2))) < 50;
        dragging1 = !dragging2 && 
                    ((xMouse-graph.xMathToPixel(x1)) * (xMouse-graph.xMathToPixel(x1)) + 
                     (yMouse-graph.yMathToPixel(y1)) * (yMouse-graph.yMathToPixel(y1))) < 50; // Increase value to decrease annoyance.
    }

    void graph_mouseReleased(MouseEvent e) { dragging1 = false; dragging2 = false; } // Clear drag flags.



    // Rounding 
    public double places1(double x) { return Math.round(10*x)/10.0; }
    public double places2(double x) { return Math.round(100*x)/100.0; } // Never used.



/*
    // Moment velocity (components) and angle. 
    // Altered to include initial velocity at time 0 and fix angle weirdness.
    public double x(double t) { return ((x2-x1) + (x2-x1)*t); }
    public double y(double t) { return ((y2-y1) + ((y2-y1) - (.5*gravity*t))*t); }
    public double angle(double t) { return Math.atan(Math.pow(y(t),2) / Math.pow(x(t),2)); }
        
        //return -Math.atan((y(t)-y(t-0.04))/(x(t)-x(t-0.04))); }
                                  //return -Math.atan((y(t)-y(t-0.02))/(x(t)-x(t-0.02)));   ? Is this the error in calculation?

    // Something definitely wrong with the angle.
    // Initial velocity is (1,1), so atan(1) should be giving 45 degrees, not 49.24...






*/










    public double x(double t) { return (x2*t); }
    public double y(double t) { return (y1 + ((y2-y1) - (gravity*t))*t); }
    public double angle(double t) { return Math.atan(Math.pow(y(t),2) / Math.pow(x(t),2)); }
        



    public void repaint() {
        graph.removeAll();
        graph.removeAllPoints();
        graph.addPoint(x1,y1,Color.magenta);
        graph.addPoint(x2,y2,Color.magenta);
      
        // Update constants of vector + path equations to match x1, y1, x2, and y2.
        try {
            directionVector.setXFormula(x2+"*t");
            directionVector.setYFormula(y1+"+("+(y2-y1)+"*t)");
        } catch(Graphable_error e) {}
        try {
            //bloodPath.setXFormula(x2+"*t");
            //bloodPath.setYFormula(y1+"+("+(y2-y1)+"*t - .5*"+gravity+"*t*t)");
               bloodPath.setXFormula("("+x2+"-"+x1+")*t");
               bloodPath.setYFormula(y1+"+("+y2+"-"+y1+")*t-"+gravity+"*t*t");
        } catch(Graphable_error e) {}
          
        graph.addGraph(directionVector, Color.MAGENTA);
        graph.addGraph(bloodPath, Color.RED);
        graph.addGraph(wall, Color.BLUE);
        graph.updateGraph();
        
            
        // if(x1==x2)
        //   { // Why ?
        //   }
        //   else
        //   {
        //   }
    }
    
    void set_initial_displays() {
        mtf_initial_angle.setText("" + places2(Math.signum(y(0)) * Math.toDegrees(angle(0))));
        mtf_initial_height.setText("" + places2(y1));
        mtf_initial_velocity.setText("" + places1(Math.sqrt(Math.pow(x2,2) + Math.pow((y2-y1), 2))));
    }


    void graph_mouseDragged(MouseEvent e) {
        double drag_diff_x, drag_diff_y;
        if (dragging1) {
            drag_diff_y = Math.max((graph.getYMin() - Math.min(y1,y2)), 
                          Math.min((graph.getYMax() - Math.max(y1,y2)), 
                          (graph.yPixelToMath(e.getY()) - y1)));    // Relative to tail.
            
            y1 += drag_diff_y;
            y2 += drag_diff_y;  // Drag both along y.
            set_initial_displays();
            repaint();
        } else 
        if (dragging2) {
            drag_diff_x = Math.max((graph.getXMin() - x2), 
                          Math.min((graph.getXMax() - x2 - .1),
                          (graph.xPixelToMath(e.getX()) - x2)));
            drag_diff_y = Math.max((graph.getYMin() - Math.min(y1,y2)), 
                          Math.min((graph.getYMax() - Math.max(y1,y2)), 
                          (graph.yPixelToMath(e.getY()) - y2)));    // Relative to tip.
            
            x2 += drag_diff_x;
            y2 += drag_diff_y;
            set_initial_displays();
            repaint();
        }
        
    }

    void trackButton_actionPerformed(ActionEvent e) {
        dropShapeGraph.removeGraph(spatterEllipse);
        t=0;
        animationTimer.start();
    }

    void resetButton_actionPerformed(ActionEvent e) {
        animationTimer = null;
        animationTimer = new javax.swing.Timer(1, this);
    	t=0;
    	x1=0;
    	y1=4;
    	//oldx1=x1;
    	//oldy1=y1;
    	x2=1;
    	y2=5;
    	//oldx2=x2;
    	//oldy2=y2;
    	spatterWidth=0;
    	spatterLength=0;

        try {
            bloodPath.setYFormula("1");
      	    directionVector.setXFormula("0");
            wall.setXFormula(""+wallDistance);
            wall.setYFormula("t");
        } catch (Graphable_error r) {}  // When does this throw?

        floorOrWallLabel.setText("Press Trace path ");
        lengthLabel.setText("height (in mm):");
        widthMathTextField.setText("");
        lengthMathTextField.setText("");
        angleMathTextField.setText("");

        graph.setPointRadius(4);
        graph.updateGraph();
        dropShapeGraph.removeGraph(spatterEllipse);
        set_initial_displays();
        repaint();
    }
}

class SpatterApplication_graph_mouseAdapter extends java.awt.event.MouseAdapter {
    SpatterApplication adaptee;
    SpatterApplication_graph_mouseAdapter(SpatterApplication adaptee) { this.adaptee = adaptee; }
    public void mousePressed(MouseEvent e) { adaptee.graph_mousePressed(e); }
    public void mouseReleased(MouseEvent e) { adaptee.graph_mouseReleased(e); }
}

class SpatterApplication_graph_mouseMotionAdapter extends java.awt.event.MouseMotionAdapter {
    SpatterApplication adaptee;
    SpatterApplication_graph_mouseMotionAdapter(SpatterApplication adaptee) { this.adaptee = adaptee; }
    public void mouseDragged(MouseEvent e) { adaptee.graph_mouseDragged(e); }
}

class SpatterApplication_trackButton_actionAdapter implements java.awt.event.ActionListener {
    SpatterApplication adaptee;
    SpatterApplication_trackButton_actionAdapter(SpatterApplication adaptee) { this.adaptee = adaptee; }
    public void actionPerformed(ActionEvent e) { adaptee.trackButton_actionPerformed(e); }
}

class SpatterApplication_resetButton_actionAdapter implements java.awt.event.ActionListener {
    SpatterApplication adaptee;
    SpatterApplication_resetButton_actionAdapter(SpatterApplication adaptee) { this.adaptee = adaptee; }
    public void actionPerformed(ActionEvent e) { adaptee.resetButton_actionPerformed(e); }
}
