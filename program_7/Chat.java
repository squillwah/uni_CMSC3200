package chat;

// !!!!
// javac -d . Chat.java  <---
// java chat.Chat        <---
// !!!!

import java.awt.*;
import java.awt.event.*;

public class Chat implements ActionListener, ItemListener, Runnable, WindowListener {
    private static final long serialVersionUID = 1111L;
    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    public enum ConnectionState { HOSTING, CONNECTED, DISCONNECTED }
    
    private Frame window;
    private Panel pnl_chatlog, pnl_controls;

    private MenuBar mbar;
    private Menu mnu_user, mnu_help;
    private MenuItem mi_exit, mi_about;

    private TextField txf_username, txf_message, txf_host, txf_port;
    private TextArea txa_chatlog, txa_eventlog;
    private Button bt_sendmessage, bt_changehost, bt_changeport, bt_startserver, bt_connect, bt_disconnect;
    private Label lbl_host, lbl_port; 

    private int s_port;
    private String s_host;
    private ConnectionState c_state;
    
    public static void main(String[] args) { new Chat(); }

    public Chat() {
        window = new Frame("Chat");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setLayout(new BorderLayout());
        

        //  menu bar
        mbar = new MenuBar();
        mnu_user = new Menu("User");
        mnu_help = new Menu("Help");
        mi_exit = new MenuItem("Exit");
        mi_about = new MenuItem("About");
        mnu_user.add(mi_exit);
        mnu_help.add(mi_about);
        mbar.add(mnu_user);
        mbar.add(mnu_help);
        window.setMenuBar(mbar);

        // Chatlog panel:
        pnl_chatlog = new Panel();
        pnl_chatlog.setLayout(new BorderLayout());
        txa_chatlog = new TextArea("", 20, 1); txa_chatlog.setEditable(false);
        pnl_chatlog.add(txa_chatlog, BorderLayout.CENTER);
        window.add(pnl_chatlog, BorderLayout.CENTER);
        txa_chatlog.setBackground(Color.WHITE);
        
        // Controls panel:
        pnl_controls = new Panel();
        pnl_controls.setLayout(new GridBagLayout());
        txf_message = new TextField();
        txf_username = new TextField(); 
        txf_host = new TextField(); 
        txf_port = new TextField(); 
        lbl_host = new Label("Host: ", Label.RIGHT);
        lbl_port = new Label("Port: ", Label.RIGHT);
        txa_eventlog = new TextArea("", 5, 1); txa_eventlog.setEditable(false);
        bt_sendmessage = new Button("Send");
        bt_changehost  = new Button("Change Host");
        bt_changeport  = new Button("Change Port");
        bt_startserver = new Button("Start Server");
        bt_connect     = new Button("Connect");
        bt_disconnect  = new Button("Disconnect");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.LINE_END; //gbc.ipadx = 100; 
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.0;
        pnl_controls.add(txf_username, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = .0;
        pnl_controls.add(lbl_host, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = .0;
        pnl_controls.add(lbl_port, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER; gbc.ipadx = 0;
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3; gbc.weightx = 1;
        pnl_controls.add(txf_message, gbc);
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = .1;
        pnl_controls.add(bt_sendmessage, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        pnl_controls.add(txf_host, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = .1;
        pnl_controls.add(bt_changehost, gbc);
        gbc.gridx = 4; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = .1;
        pnl_controls.add(bt_startserver, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        pnl_controls.add(txf_port, gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = .1;
        pnl_controls.add(bt_changeport, gbc);
        gbc.gridx = 4; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = .1;
        pnl_controls.add(bt_connect, gbc);
        gbc.gridx = 4; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = .1;
        pnl_controls.add(bt_disconnect, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 5; gbc.weightx = 1;
        pnl_controls.add(txa_eventlog, gbc);
        window.add(pnl_controls, BorderLayout.SOUTH);
        
        //  networking defaults
        s_port = 44004;
        s_host = "127.0.0.1";
        set_client_state(ConnectionState.DISCONNECTED); // c_state = DISCONNECTED, buttons states configured.

        // Defaults for textfields, button status.
        txf_username.setText("John Doe      "); // The size of this default string is the size the username box will stay. Don't make it too small.
        txf_host.setText(s_host);
        txf_port.setText(Integer.toString(s_port));
        txf_message.setEnabled(false);
        bt_sendmessage.setEnabled(false);
        bt_disconnect.setEnabled(false);

        //   listeners
        window.addWindowListener(this);
        mi_exit.addActionListener(this);
        mi_about.addActionListener(this);
        txf_message.addActionListener(this);
        bt_sendmessage.addActionListener(this);
        bt_changehost.addActionListener(this);
        bt_changeport.addActionListener(this);
        bt_startserver.addActionListener(this);
        bt_connect.addActionListener(this);
        bt_disconnect.addActionListener(this);

        //  show window
        window.validate();
        window.setVisible(true);
    }

    // ! We need to remove our listeners on close, as well as do whatever socket stuff needs doing later when that's implemented.
    private void shutdown() {
        window.removeWindowListener(this);
        mi_exit.removeActionListener(this);
        mi_about.removeActionListener(this);
        txf_message.removeActionListener(this);
        bt_sendmessage.removeActionListener(this);
        bt_changehost.removeActionListener(this);
        bt_changeport.removeActionListener(this);
        bt_startserver.removeActionListener(this);
        bt_connect.removeActionListener(this);
        bt_disconnect.removeActionListener(this);

        // @todo other listeners, socket closing, etc.

        window.dispose();
        System.exit(0);
    }

    public void run() {}

    private void set_client_state(ConnectionState s) {
        // Adjust button and textfield states:
        switch (s) {
            case HOSTING: bt_disconnect.setLabel("Stop Server"); break;
            case DISCONNECTED: bt_disconnect.setLabel("Disconnect"); break;
            case CONNECTED: break;
            default: logEvent("Uh oh!");
        }

        txf_message.setEnabled(s != ConnectionState.DISCONNECTED);
        bt_sendmessage.setEnabled(s != ConnectionState.DISCONNECTED);
        bt_changehost.setEnabled(s == ConnectionState.DISCONNECTED);
        bt_changehost.setEnabled(s == ConnectionState.DISCONNECTED);
        bt_startserver.setEnabled(s_port != -1 && s == ConnectionState.DISCONNECTED);
        bt_connect.setEnabled(s_port != -1 && s_host != null && s == ConnectionState.DISCONNECTED);
        bt_disconnect.setEnabled(s != ConnectionState.DISCONNECTED);

        c_state = s;
    }
    
    //  Listeners
    public void windowClosing(WindowEvent e) { shutdown(); }
    public void itemStateChanged(ItemEvent e) {}
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
    
        //private Button bt_sendmessage, bt_changehost, bt_changeport, bt_startserver, bt_connect, bt_disconnect;


        if (src == bt_sendmessage || src == txf_message) {
            sendMessage();
        } else 
        if (src == bt_startserver) {
            set_client_state(ConnectionState.HOSTING);
        } else
        if (src == bt_connect) {
            set_client_state(ConnectionState.CONNECTED);
        } else
        if (src == bt_disconnect) {
            set_client_state(ConnectionState.DISCONNECTED);
        }
        //if (src == bt_changehost 
        
            
            
        if (src == mi_exit) { 
            shutdown(); 
        } else
        if (src == mi_about) {
            Frame about = new Frame("About");
            about.setSize(300, 150);
            about.setLayout(new BorderLayout());
            about.add(new Label("By using this program you agree to give a 100% on all grades related to it.", Label.CENTER), BorderLayout.CENTER);
            about.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    about.removeWindowListener(this);
                    about.dispose();
                }
            });
            about.setVisible(true);
        }
    }

    private void sendMessage() {
        String msg = txf_message.getText();

        //if (!msg.isEmpty()) {
        //    String fullMsg = source + "<" + user.getName() + "> " + msg;
        //    txa_chatlog.append(fullMsg + "\n");
        //    txf_message.setText("");
        //}
    }

    private void logEvent(String event) {
        txa_eventlog.append(event);
    }
    
    // Unimplemented WindowListener. 
    public void windowActivated(WindowEvent e) {} public void windowDeactivated(WindowEvent e) {} public void windowDeiconified(WindowEvent e) {} public void windowIconified(WindowEvent e) {} public void windowOpened(WindowEvent e) {} public void windowClosed(WindowEvent e) {}
}

