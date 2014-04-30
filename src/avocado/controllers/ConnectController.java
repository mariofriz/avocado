
package avocado.controllers;

import avocado.models.Client;
import avocado.views.ConnectView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mario
 */
public class ConnectController{
    
    private ConnectView view;
    private Client client;
    
    public ConnectController(Client client)
    {
        this.client = client;
        this.view = new ConnectView();
        
        this.initialiseListeners();
        
        this.view.setVisible(true);
    }

    private void initialiseListeners() {
        this.view.getConnectButton().addActionListener(new ConnectListener());
    }
    
    class ConnectListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                client.setRemoteIp(view.getHostTextField().getText());
            } catch (UnknownHostException ex) {
                Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                view.dispose();
            }
        }
        
    }

}
