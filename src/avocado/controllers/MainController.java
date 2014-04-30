package avocado.controllers;

import avocado.models.Client;
import avocado.views.MainView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Main controller
 *
 * @author Mario
 */
public class MainController implements Observer {

    private MainView view;
    private Client client;

    public MainController() {
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
        this.view = new MainView();

        try {
            this.client = new Client();
        } catch (SocketException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Initialise server to localhost
            this.client.setRemoteIp("127.0.0.1");
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.initialiseListeners();

        this.view.getLogTextArea().append("Avocado is ready to use!\n");

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
        System.out.println("update");
        this.view.getLogTextArea().append(arg + "\n");
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
