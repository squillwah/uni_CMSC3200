
// [CMSC3200] Technical Computing Using Java
// Program 7: Chat
//
// ............
// . hello    .
// .          .
// .          .
// .----------.
// . :=====###.
// ............
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu

package chat;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class Chat implements ActionListener, Runnable, WindowListener {
    private static final long serialVersionUID = 1111L;
    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    private enum ConnectionState { HOSTING, CONNECTED, DISCONNECTED }
    private enum ConnectionType { HOST, CLIENT }
    private int TIMEOUT = 8000; // 8 second connection timeout.
    
    private Frame window;
    private Panel pnl_chatlog, pnl_controls;

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
    private Thread listen_thread;
    private boolean listen_thread_listening;

    public static void main(String[] args) { new Chat(); }

    public Chat() {
        window = new Frame("Chat");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setLayout(new BorderLayout());
        window.setBackground(Color.lightGray);

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
        txa_eventlog = new TextArea("", 6, 1); 
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
        c_state = ConnectionState.DISCONNECTED;
        sock = null;
        outgoing = null;
        incoming = null;
        listen_thread = null;
        listen_thread_listening = false;

        // Defaults for textfields, button status.
        txf_username.setText("Johnson Doeth");     // The size of this default string is the size the username box will stay. Don't make it too small.
        txf_host.setText(s_host);
        txf_port.setText(Integer.toString(s_port));
        txf_message.setText("");
        
        //   listeners
        window.addWindowListener(this);
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

    private void shutdown() {
        window.removeWindowListener(this);
        txf_message.removeActionListener(this);
        txf_port.removeActionListener(this);
        txf_host.removeActionListener(this);
        bt_sendmessage.removeActionListener(this);
        bt_changehost.removeActionListener(this);
        bt_changeport.removeActionListener(this);
        bt_startserver.removeActionListener(this);
        bt_connect.removeActionListener(this);
        bt_disconnect.removeActionListener(this);
        change_state(ConnectionState.DISCONNECTED);
        window.dispose();
        System.exit(0);
    }

    // Creates the socket (either waits for or attempts, depending on ConnectionType), starts 
    // listening thread if socket creation was successful. Returns success/fail of connection.
    private boolean establish_connection(ConnectionType c) {
        boolean established = false;
        if (c_state == ConnectionState.CONNECTED) logEvent("Connection Failure: already connected");
        else if (listen_thread != null || sock != null || outgoing != null || incoming != null) logEvent("Connection Failure: evidence of ongoing connection (bug)");
        else { 
            // Create socket:
            ServerSocket serversock = null;
            try { 
                switch (c) {
                    case HOST:
                        logEvent("Server started, waiting for client...");
                        serversock = new ServerSocket(s_port);
                        serversock.setSoTimeout(TIMEOUT);
                        try { 
                            sock = serversock.accept(); 
                            logEvent("Connection recieved from: " + sock.getInetAddress());
                        } catch (SocketTimeoutException e) { 
                            throw new IOException("timeout reached; giving up");  // To trigger outer catch, for IO degunking.
                        }
                        serversock.close(); serversock = null;
                        established = true; 
                        break;
                    case CLIENT:
                        logEvent("Attempting connection...");
                        sock = new Socket();
                        sock.setSoTimeout(TIMEOUT);
                        try { 
                            sock.connect(new InetSocketAddress(s_host, s_port)); // Assuming all the checking's been done beforehand.
                            logEvent("Successfully connected to: " + sock.getInetAddress());
                        } catch (SocketTimeoutException e) { 
                            throw new IOException("timeout reached; giving up");
                        }
                        established = true;
                        break;
                    default: logEvent("Bad bad!"); break;
                }
                // Create streams from socket, start listener:
                if (established) {
                    sock.setSoTimeout(100); // Lower timeout for connect/disconnect responsiveness in thread.
                    outgoing = new PrintWriter(sock.getOutputStream(), true);
                    incoming = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    listen_thread = new Thread(this);
                    listen_thread_listening = true;
                    listen_thread.start();
                }
            } catch (IOException e) { 
                logEvent("Connection Failure: " + e.getMessage()); 
                if (sock != null) {
                    try { sock.close(); } catch (IOException ee) {}              // Cleanup of IO stuff.
                    sock = null;
                } 
                if (serversock != null) {
                    try { serversock.close(); } catch (IOException ee) {}
                    serversock = null;
                }
                if (outgoing != null) {
                    outgoing.close();
                    //try { outgoing.close(); } catch (IOException ee) {}
                    outgoing = null;
                }
                if (incoming != null) {
                    try { incoming.close(); } catch (IOException ee) {}
                    incoming = null;
                }
            }
        }
        return established;
    }

    // Stops the listening thread if applicable, destroys socket and in/out streams.
    // Returns success/fail of disconnection.
    private boolean disconnect() { 
        boolean disconnected = false;  
        if (c_state == ConnectionState.DISCONNECTED) logEvent("Disconnect Failure: already disconnected");
        else if (listen_thread == null || sock == null || outgoing == null || incoming == null) logEvent("Disconnect Failure: evidence of partial disconnect (bug)");
        else { 
            listen_thread_listening = false; 
            if (Thread.currentThread() != listen_thread) {  // Disconnect() may be called inside listen_thread too, when peer disconnects.
                listen_thread.interrupt(); 
                try { listen_thread.join(); }           // Wait for listening thread to finish before closing sockets, streams.
                catch (InterruptedException e) {}
            }
            try {
                sock.close();
                outgoing.close();
                incoming.close();
            } catch (IOException e) { logEvent("Ugly Disconnect: " + e.getMessage()); }
            listen_thread = null; sock = null; outgoing = null; incoming = null; // Do away with it all.
            disconnected = true;
        }
        return disconnected;    // Doesn't make as much sense here as in establish_connection(), but it is still useful to have the confirmation.
    }
    
    // The incoming message update thread. The listener.
    // Will trigger its own change_state(DISCONNECT) on a null recieve. 
    public void run() {
        String msg;
        while (listen_thread_listening) {
            try { 
                msg = incoming.readLine();
                if (msg == null) {
                    logEvent("Peer disconnected; closing connection.");
                    change_state(ConnectionState.DISCONNECTED); 
                }
                else txa_chatlog.append(msg + '\n');
            } 
            catch (SocketTimeoutException e) {}
            catch (IOException e) { 
                logEvent("Err: bad incoming message: " + e); 
                change_state(ConnectionState.DISCONNECTED); 
            }
            try { Thread.sleep(1); }
            catch (InterruptedException e) {}
        }
    }
   
    // Call this to make a connection, host a server, or disconnect/shutdown server.
    // (do not call establish_connection() or disconnect() directly).
    private void change_state(ConnectionState s) {
        boolean state_changed = false;
        if (s != c_state) {
            switch (s) {
                case HOSTING:
                    if (listen_thread != null)  logEvent("Err: Can't start server; the listening thread is active (are you connected?).");
                    else if (s_port == -1)      logEvent("Err: Can't start server; invalid port.");
                    else state_changed = establish_connection(ConnectionType.HOST);
                    break;
                case CONNECTED:
                    if (listen_thread != null)  logEvent("Err: Can't open connection; the listening thread is active (are you hosting?).");
                    else if (s_host == null)    logEvent("Err: Can't open connection; invalid host.");
                    else if (s_port == -1)      logEvent("Err: Can't open connection; invalid port.");
                    else state_changed = establish_connection(ConnectionType.CLIENT);
                    break;
                case DISCONNECTED:
                    state_changed = disconnect();
                    break;
                default: logEvent("Uh oh!"); break;
            }
        }
        if (state_changed) {
            c_state = s;
            refresh_button_states();
            logEvent("You are now: " + c_state);
        }
    }

    // Update the enable/disable state of the buttons and textfields based on the client connection state.
    private void refresh_button_states() {
        if (c_state == ConnectionState.HOSTING) bt_disconnect.setLabel("Stop Server");
        else bt_disconnect.setLabel("Disconnect");
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

    // Set the hostname to this string. With empty check.
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
   
    // Set the hostname to number, either in integer or string format.
    // Does digit check on string variant, and bounds check on integer variant. 
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
   
    // Log an event to the event log. 
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
        }
    }
   
    // Send a message over the outgoing stream, append to local chatlog.
    // Inserts username and host status at beginning of message.
    // Only works if outgoing stream is not null.
    private void sendMessage(String msg) {
        if (outgoing == null) logEvent("Err: can't send message; outgoing stream is null.");
        else {
            String username = txf_username.getText();
            if (c_state == ConnectionState.HOSTING) username += " (host)";
            msg = username + ": " + msg;
            outgoing.println(msg);
            txa_chatlog.append(msg + '\n');
        }
    }
    
    //  Listeners
    public void windowClosing(WindowEvent e) { shutdown(); }
    // Unimplemented WindowListener. 
    public void windowActivated(WindowEvent e) {} public void windowDeactivated(WindowEvent e) {} public void windowDeiconified(WindowEvent e) {} public void windowIconified(WindowEvent e) {} public void windowOpened(WindowEvent e) {} public void windowClosed(WindowEvent e) {}
}

