package avocado.controllers;

import avocado.models.Client;
import avocado.models.Log;
import avocado.views.MainView;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Avocado main controller *
 * @author Mario
 */
public class MainController implements Observer {

    private MainView view;
    private Client client;

    public MainController() {
        // Set look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Create main view
        this.view = new MainView();
        try {
            view.setIconImage(ImageIO.read(new File("resources/avocado.gif")));
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Fire up client model
        try {
            this.client = new Client();
        } catch (SocketException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Listen to me
        this.initialiseListeners();

        // Welcome the new user
        this.view.getLogTextArea().setText("Avocado is ready to use!\n");
        try {
            this.client.setRemoteIp("127.0.0.1");
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Abracadabra!
        this.view.setVisible(true);
    }

    private void initialiseListeners() {
        this.view.getExitButton().addActionListener(new ExitListener());
        this.view.getSendButton().addActionListener(new SendListener());
        this.view.getConnectButton().addActionListener(new ConnectListener());
        this.view.getReceiveButton().addActionListener(new ReceiveListener());

        this.client.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        // Get Log object
        Log log = (Log)arg;
        
        System.out.println("update");
        StyledDocument doc = view.getLogTextArea().getStyledDocument();
        
        // Set style
        Style style = view.getLogTextArea().addStyle("Style", null);        
        switch (log.getType()) {
            case NORMAL:
                StyleConstants.setForeground(style, Color.black);
                break;
            case INFO:
                StyleConstants.setForeground(style, Color.blue);
                break;
            case ERROR:
                StyleConstants.setForeground(style, Color.red);
                break;
            case SUCCESS:
                StyleConstants.setForeground(style, Color.green);
                break;                
        }
        
        // Append line ending
        String text = log.getMessage() + "\n";
        
        // Write to text pane
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ConnectListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ConnectController connectController = new ConnectController(client);
        }
    }

    class SendListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            SendController sendController = new SendController(client);
        }

    }

    class ReceiveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ReceiveController receiveController = new ReceiveController(client);
        }

    }

    class ExitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose();
        }

    }

}
