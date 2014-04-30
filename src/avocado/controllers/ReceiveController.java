package avocado.controllers;

import avocado.models.Client;
import avocado.views.ReceiveView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * Controller for file reception
 * @author Mario
 */
public class ReceiveController {
    
    private ReceiveView view;
    private Client client;
    
    public ReceiveController(Client client) {
        this.client = client;
        this.view = new ReceiveView();
        
        this.initialiseListeners();
        
        this.view.setVisible(true);
    }
    
    private void initialiseListeners() {
        this.view.getReceiveButton().addActionListener(new ReceiveListener());
        this.view.getFileButton().addActionListener(new FileListener());
    }
    
    class ReceiveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String filename = view.getRemoteFileTextField().getText();
            String localName = view.getLocalFileTextField().getText();
            try {
                client.receiveFile(filename, localName);
            } catch (IOException ex) {
                Logger.getLogger(ReceiveController.class.getName()).log(Level.SEVERE, null, ex);
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
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fc.showSaveDialog(view);
            
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String path = file.getAbsoluteFile().getPath();
                view.getLocalFileTextField().setText(path + File.separator + view.getRemoteFileTextField().getText());
                view.getReceiveButton().setEnabled(true);
            }            
        }        
    }
    
}
