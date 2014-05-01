package avocado.controllers;

import avocado.models.Client;
import avocado.helpers.AvocadoLogger;
import avocado.views.MainView;
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Create main view
        this.view = new MainView();
        try {
            view.setIconImage(ImageIO.read(new File("resources/small-icon.png")));
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
        
        // Abracadabra!
        this.view.setVisible(true);
        
        // Initialise logger
        AvocadoLogger.setInstance(new AvocadoLogger(this.view.getLogTextArea()));

        // Welcome the new user and set default IP
        AvocadoLogger.log("Avocado is ready to use!");
        try {
            this.client.setRemoteIp(Client.DEFAULT_IP);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initialiseListeners() {
        this.view.getExitButton().addActionListener(new ExitListener());
        this.view.getSendButton().addActionListener(new SendListener());
        this.view.getConnectButton().addActionListener(new ConnectListener());
        this.view.getReceiveButton().addActionListener(new ReceiveListener());

        //this.client.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
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
            client.close();
            view.dispose();
        }

    }

}
