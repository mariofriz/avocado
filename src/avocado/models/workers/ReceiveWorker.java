
package avocado.models.workers;

import avocado.helpers.AvocadoLogger;
import avocado.models.Client;
import avocado.models.Packet;
import avocado.models.PacketFactory;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receive worker model
 * @author Mario
 */
public class ReceiveWorker extends AbstractWorker {

    public ReceiveWorker(InetAddress remoteHost, DatagramSocket socket, String localFile, String remoteFile) {
        super(remoteHost, socket, localFile, remoteFile);
    }

    @Override
    public void run() {
        try {
            // Send RRQ packet
            Packet packet = PacketFactory.createRRQ(remoteFile);
            this.sendPacket(packet);
            
            // Log message
            AvocadoLogger.log("Sent RRQ for " + remoteFile);
            
            // Prepare local file
            String localFilePath;
            if (localFile == null) {
                localFilePath = System.getProperty("user.dir") + File.separator + remoteFile;
            } else {
                localFilePath = localFile;
            }
            File file = new File(localFilePath);
            boolean fileExists = file.exists();
            
            // Prepare to write to local file
            DataOutputStream out;
            out = new DataOutputStream(new FileOutputStream(localFilePath));
            
            int transferPort = Client.DEFAULT_PORT;
            
            // Get ready to receive packets
            AvocadoLogger.info("Receiving " + remoteFile);
            Packet lastPacket;
            while (true) {
                // Get parsed received packet
                lastPacket = Packet.receivePacket(socket);
                if (transferPort == Client.DEFAULT_PORT) {
                    transferPort = lastPacket.getServerPort();
                }
                
                // Get data and write to file
                out.write(lastPacket.getData());
                
                // Return acknowledge packet with block number
                Packet ackPacket = PacketFactory.createACK(lastPacket.getBlockNumber());
                this.sendPacket(ackPacket, transferPort);
                
                // Check if last data packet
                if (lastPacket.getPacketSize() < Packet.MAX_DATA_SIZE) {
                    //System.out.println("Finished transfer!");
                    AvocadoLogger.log(remoteFile + " successfully received");
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ReceiveWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
