package avocado.controllers;

import avocado.models.Client;
import avocado.views.SendView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author Mario
 */
public class SendController {
    
    private SendView view;
    private Client client;
    
    public SendController(Client client) {
        this.client = client;
        this.view = new SendView();
        
        this.initialiseListeners();
        
        this.view.setVisible(true);
    }
    
    private void initialiseListeners() {
        this.view.getSendButton().addActionListener(new SendListener());
        this.view.getLocalFileButton().addActionListener(new FileListener());
    }
    
    class SendListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String local = view.getLocalFileTextField().getText();
            String remote = view.getRemoteFileTextField().getText();
            try {
                client.sendFile(local, remote);
            } catch (IOException ex) {
                Logger.getLogger(SendController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                view.dispose();
            }
        }        
    }
    
    class FileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose local folder");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnValue = fc.showSaveDialog(view);
            
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                view.getLocalFileTextField().setText(file.getAbsolutePath());
                view.getRemoteFileTextField().setText(file.getName());
                view.getSendButton().setEnabled(true);
            }            
        }        
    }
    
}
