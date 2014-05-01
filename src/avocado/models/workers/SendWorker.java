package avocado.models.workers;

import avocado.helpers.AvocadoLogger;
import avocado.models.Packet;
import avocado.models.PacketFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mario
 */
public class SendWorker extends AbstractWorker {

    public SendWorker(InetAddress remoteHost, DatagramSocket socket, String localFile, String remoteFile) {
        super(remoteHost, socket, localFile, remoteFile);
    }

    @Override
    public void run() {
        try {
            // Send WRQ packet
            Packet packet = PacketFactory.createWRQ(remoteFile);
            this.sendPacket(packet);
            
            // Log message
            AvocadoLogger.log("Sent WRQ for " + remoteFile);
            
            // Prepare buffer and file reader
            byte[] data;
            DataInputStream in = new DataInputStream(new FileInputStream(localFile));
            
            short blockNumber = 1;
            int totalLength = in.available();
            int sentLength = 0;
            //System.out.println("Total length: " + totalLength);
            
            // Wait for ACK
            Packet ack = Packet.receivePacket(socket);
            //System.out.println("server ack " + ack.getServerAddress().toString() + "@" + ack.getServerPort());
            int transferPort = ack.getServerPort();
            //ack.debug();
            
            // Get ready to send
            AvocadoLogger.info("Sending " + remoteFile);
            while (true) {
                // Read file content
                data = new byte[Packet.MAX_DATA_SIZE];
                int length = in.read(data, 0, Packet.MAX_DATA_SIZE);
                sentLength += length;
                
                //System.out.println("Sent length: " + sentLength);
                
                // Send DATA packet
                Packet dataPacket = PacketFactory.createData(data, blockNumber, length);
                this.sendPacket(dataPacket, transferPort);
                //dataPacket.debug();
                
                // Wait for ACK
                ack = Packet.receivePacket(socket);
                //ack.debug();
                
                // Check if file has been sent completely
                if (sentLength >= totalLength) {
                    AvocadoLogger.success(remoteFile + " successfully sent");
                    break;
                }
                
                blockNumber++;
            }
        } catch (IOException ex) {
            Logger.getLogger(SendWorker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
