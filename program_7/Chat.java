package chat;

// !!!!
// javac -d . Chat.java  <---
// java chat.Chat        <---
// !!!!

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class Chat implements ActionListener, ItemListener, Runnable, WindowListener {
    private static final long serialVersionUID = 1111L;
    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    private enum ConnectionState { HOSTING, CONNECTED, DISCONNECTED }
    private enum ConnectionType { HOST, CLIENT }
    private int TIMEOUT = 10000; // 10 second connection timeout.
    
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
    private Socket sock;
    private PrintWriter outgoing;
    private BufferedReader incoming;
    private Thread listening_thread;

    // PrintWriter and BufferedReader is created for the socket created by the first socket connection (listen).
    // Or their created on the first client socket. For the client, set a connection time out.
    // Connection made, thread loop checks bufferedread for input, 
    // when input recieved, it is appended to text area.
    // When user tpes message, it is sent and appended to text area.
    
    public static void main(String[] args) { new Chat(); }

    public Chat() {
        window = new Frame("Chat");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setLayout(new BorderLayout());
        window.setBackground(Color.lightGray);

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
        txa_chatlog = new TextArea("", 20, 1); 
        txa_chatlog.setEditable(false);
        txa_chatlog.setBackground(Color.white);
        pnl_chatlog.add(txa_chatlog, BorderLayout.CENTER);
        window.add(pnl_chatlog, BorderLayout.CENTER);
        
        // Controls panel:
        pnl_controls = new Panel();
        pnl_controls.setLayout(new GridBagLayout());
        txf_message = new TextField();
        txf_username = new TextField(); 
        txf_host = new TextField(); 
        txf_port = new TextField(); 
        lbl_host = new Label("Host: ", Label.RIGHT);
        lbl_port = new Label("Port: ", Label.RIGHT);
        txa_eventlog = new TextArea("", 5, 1); 
        txa_eventlog.setEditable(false);
        txa_eventlog.setBackground(Color.white);
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
        //set_client_state(ConnectionState.DISCONNECTED); // c_state = DISCONNECTED, buttons states configured.
        c_state = ConnectionState.DISCONNECTED;
        sock = null;
        outgoing = null;
        incoming = null;
        listening_thread = null;

        // Defaults for textfields, button status.
        txf_username.setText("Johnson Doeth");     // The size of this default string is the size the username box will stay. Don't make it too small.
        txf_host.setText(s_host);
        txf_port.setText(Integer.toString(s_port));
        txf_message.setText("");
        
        //   listeners
        window.addWindowListener(this);
        mi_exit.addActionListener(this);
        mi_about.addActionListener(this);
        txf_message.addActionListener(this);
        txf_port.addActionListener(this);
        txf_host.addActionListener(this);
        bt_sendmessage.addActionListener(this);
        bt_changehost.addActionListener(this);
        bt_changeport.addActionListener(this);
        bt_startserver.addActionListener(this);
        bt_connect.addActionListener(this);
        bt_disconnect.addActionListener(this);

        //  show window
        window.validate();
        window.setVisible(true);
        
        refresh_button_states();    // Colors are weird if we do this before setting visible.
    }

    // ! We need to remove our listeners on close, as well as do whatever socket stuff needs doing later when that's implemented.
    private void shutdown() {
        window.removeWindowListener(this);
        mi_exit.removeActionListener(this);
        mi_about.removeActionListener(this);
        txf_message.removeActionListener(this);
        txf_port.removeActionListener(this);
        txf_host.removeActionListener(this);
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
    
    private boolean establish_connection(ConnectionType c) {
        boolean established = false;
        if (c_state != ConnectionState.DISCONNECTED) logEvent("Err: can't establish a new " + c + " connection; you're " + c_state);
        else if (sock != null) logEvent("Err: can't establish a new " + c + " connection; a socket still exists!");
        else {
            try {
                switch (c) {
                    case HOST:
                        ServerSocket servsock = new ServerSocket(s_port);
                        servsock.setSoTimeout(TIMEOUT);
                        sock = servsock.accept();
                        logEvent("Connection recieved from: " + sock.getInetAddress());
                        outgoing = new PrintWriter(sock.getOutputStream());
                        incoming = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        established = true;
                        break;
                    case CLIENT:
                        sock = new Socket();
                        sock.setSoTimeout(TIMEOUT);
                        sock.connect(new InetSocketAddress(s_host, s_port));
                        logEvent("Connection accepted to: " + sock.getInetAddress());
                        outgoing = new PrintWriter(sock.getOutputStream());
                        incoming = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        established = true;
                        break;
                    default: logEvent("Bad!"); break;
                }
            } catch (SocketTimeoutException e) {
                logEvent("Connection timeout reached; giving up.");
            } catch (IllegalArgumentException e) {
                logEvent("Connection failure; bad port, somehow...");
            } catch (IOException e) {
                logEvent("Connection failure; " + e);
            }
        }
        return established;
    }

    private boolean disconnect() {
        if (listening_thread != null) logEvent("Err: can't disconnect; listening thread still alive.");
        else {
            try {
                if (sock == null) logEvent("Err: there is no socket?");
                else {
                    sock.close();
                    logEvent("Connection to " + sock.getInetAddress() + " closed.");
                    sock = null; incoming = null; outgoing = null;
                }
            } catch (IOException e) {
                logEvent("Disconnection failure; " + e );
            }
        }
        return sock == null;
    }

    private void start_listening() {
        if (listening_thread == null) {
            listening_thread = new Thread(this);
            listening_thread.start();
        } 
    }
    private void stop_listening() {
        if (listening_thread != null) {
            listening_thread.interrupt();
            while (listening_thread.isAlive()); // Busy wait for any buffered reads to finish.
            listening_thread = null;
        }
    }

    public void run() {
        if (sock == null) logEvent("Err: Listening failure; socket is null.");
        else if (incoming == null) logEvent("Err: Listening failure; incoming stream is null.");
        else {
            try {
                String line;
                while (!Thread.currentThread().isInterrupt() && (line = incoming.readLine()) != null) {
                    txa_chatlog.append(line + '\n');
                }
                if (!Thread.currentThread().isInterrupted()) logEvent("Peer disconnected.");
            } catch (IOExpetion e) {
                if (!Thread.currentThread().isInterrupted()) logEvent("Connection lost; " + e.getMessage());
            }
            logEvent("Stopping listener.");
        }
    }
    private void change_state(ConnectionState s) {
        // Adjust button and textfield states:
        boolean state_changed = false;
        switch (s) {
            case HOSTING: 
                if (listening_thread != null)   logEvent("Err: Can't start server; the listening thread is active (are you connected?).");
                else if (s_port == -1)          logEvent("Err: Can't start server; invalid port.");
                else { 
                    logEvent("Establishing host connection, waiting for client...");
                    boolean ok = establish_connection(ConnectionType.HOST);
                    if (ok) {
                        c_state = ConnectionState.HOSTING;
                        bt_disconnect.setLabel("Stop server");
                        logEvent("Starting listener thread...");
                        start_listening();
                    }
                    refresh_button_states();
                    logEvent("You are now: " + c_state);
                }.start();
            }break;
            case CONNECTED:
                if (listening_thread != null)   logEvent("Err: Can't open connection; the listening thread is active (are you hosting?).");
                else if (s_host == null)        logEvent("Err: Can't open connection; invalid host.");
                else if (s_port == -1)          logEvent("Err: Can't open connection; invalid port.");
                else { 
                    logEvent("Establishing client connection, awaiting host response...");
                    bt_startserver.setEnabled(false);
                    bt_connect.setEnabled(false);
                    new Thread(() -> {
                        boolean ok = establish(ConnectionType.CLIENT);
                        if (ok) {
                            c_state = ConnectionState.CONNECTED;
                            logEvent("Starting listener thread...");
                            start_listening();                        
                    }
                    refresh_button_states();
                    logEvent("You are now: " + c_state);
                }).start();
            } break;
            case DISCONNECTED: 
                if (c_state == ConnectionState.DISCONNECTED) logEvent("Err: Can't disconnect, you're already disconnected?");
                else {
                    if (listening_thread == null) logEvent("Err: Listening thread died before disconnect; something terrible has happened.");
                    logEvent("Closing connection...");
                    stop_listening();
                    disconnect();
                    // @todo interrupt the thread to stop listenning, close the sockets. 
                    // Will need to send something to peer to signal their disconnect as well. Have log too, like ("peer disconnected").
                    bt_disconnect.setLabel("Disconnect");
                    state_changed = true;
                } break;
            default: logEvent("Uh oh!"); break;
        } 
        if (state_changed) {
            c_state = s;
            refresh_button_states();
            logEvent("You are now: " + c_state);
        }
    }

    private void refresh_button_states() {
        bt_sendmessage.setEnabled(c_state != ConnectionState.DISCONNECTED);
        txf_message.setEnabled(c_state != ConnectionState.DISCONNECTED);
        txf_message.setEditable(c_state != ConnectionState.DISCONNECTED);
        bt_changehost.setEnabled(c_state == ConnectionState.DISCONNECTED);
        txf_host.setEnabled(c_state == ConnectionState.DISCONNECTED);
        txf_host.setEditable(c_state == ConnectionState.DISCONNECTED);
        bt_changeport.setEnabled(c_state == ConnectionState.DISCONNECTED);
        txf_port.setEnabled(c_state == ConnectionState.DISCONNECTED);
        txf_port.setEditable(c_state == ConnectionState.DISCONNECTED);
        bt_startserver.setEnabled(s_port != -1 && c_state == ConnectionState.DISCONNECTED);
        bt_connect.setEnabled(s_port != -1 && s_host != null && c_state == ConnectionState.DISCONNECTED);
        bt_disconnect.setEnabled(c_state != ConnectionState.DISCONNECTED);
    }

    private void set_host(String host) {
        if (host.isEmpty()) {
            logEvent("Err: host can't be nothing!");
            s_host = null;
        } else {
            s_host = host;
            logEvent("Host set to " + s_host);
        }
        refresh_button_states();
    }
    
    private void set_port(String portstring) { 
        int port;
        try { port = Integer.parseInt(portstring); }
        catch (NumberFormatException ex) { port = -1; }
        set_port(port);
    }
    private void set_port(int port) {
        if (port < 1024 || port > 65535) {
            s_port = -1;
            logEvent("Err: bad port; must be integer 1024 -> 65535");
        } else {
            s_port = port;
            logEvent("Port set to " + s_port);
        }
        refresh_button_states();
    }
    
    private void logEvent(String event) { txa_eventlog.append(event + '\n'); }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if ((src == bt_sendmessage || src == txf_message)) {
            String msg = txf_message.getText();
            if (!msg.isEmpty()) {
                sendMessage(msg);
                txf_message.setText("");
            } //else logEvent("Say something!");
        } else 
        if (src == bt_changehost || src == txf_host) {
            set_host(txf_host.getText());
        } else
        if (src == bt_changeport || src == txf_port) {
            set_port(txf_port.getText());
        } else
        if (src == bt_startserver) {
            change_state(ConnectionState.HOSTING);
        } else
        if (src == bt_connect) {
            change_state(ConnectionState.CONNECTED);
        } else
        if (src == bt_disconnect) {
            change_state(ConnectionState.DISCONNECTED);
        } else 
            
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
    
    //  Listeners
    public void itemStateChanged(ItemEvent e) {}    // @ todo: If we don't have any radio or checkbox button's, we don't need this listener.
    
    public void windowClosing(WindowEvent e) { shutdown(); }

    private void sendMessage(String msg) {
        String username = txf_username.getText();
        if (c_state == ConnectionState.HOSTING) username += " (host)";
        msg = username + ": " + msg + '\n';
        txa_chatlog.append(msg);

        if (outgoing != null) {
            outgoing.println(msg);
            outgoing.flush();
        } else {
            logEvent("Err: Cant't send; no outgoing stream.");
            }
        }
    }

    // Unimplemented WindowListener. 
    public void windowActivated(WindowEvent e) {} public void windowDeactivated(WindowEvent e) {} public void windowDeiconified(WindowEvent e) {} public void windowIconified(WindowEvent e) {} public void windowOpened(WindowEvent e) {} public void windowClosed(WindowEvent e) {}
}

