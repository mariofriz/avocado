package avocado.models.workers;

import avocado.helpers.AvocadoLogger;
import avocado.models.Client;
import avocado.models.Packet;
import avocado.models.PacketFactory;
import avocado.models.Error;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mario
 */
public class SendWorker extends AbstractWorker {

    private DataInputStream in;

    public SendWorker(InetAddress remoteHost, String localFile, String remoteFile) throws SocketException {
        super(remoteHost, localFile, remoteFile);
    }

    @Override
    public void run() {
        // Preparing for request
        boolean timeout = false;
        int transferPort = Client.DEFAULT_PORT;
        Packet request, response = null;
        int retries = 0;

        // Send WRQ packet while timeout on request
        do {
            try {
                if (retries > 2) {
                    AvocadoLogger.error("3 timeouts : connection lost");
                    this.close();
                    return;
                }

                timeout = false;
                // Off we go
                request = PacketFactory.createWRQ(remoteFile);
                this.sendPacket(request);
                AvocadoLogger.log("Sent WRQ for " + remoteFile);

                // Wait for response from server
                response = Packet.receivePacket(socket);
            } catch (IOException ex) {
                // Something went wrong -> resend WRQ packet
                timeout = true;
                retries++;
                AvocadoLogger.log("Timeout for WRQ on " + remoteFile);
            }
        } while (timeout);

        // Reset tries counter
        retries = 0;

        // Check for error
        if (response.isERR()) {
            AvocadoLogger.error("Server Error: " + Error.ERROR_MESSAGES[response.getErrorCode()]);
            System.out.println(remoteFile + ": CREM = " + response.getErrorCode());
            this.close();
            return;
        }

        //Check if the server's port has changed
        if (transferPort != response.getServerPort()) {
            transferPort = response.getServerPort();
        }

        // Prepare buffer and file reader
        byte[] data;
        try {
            in = new DataInputStream(new FileInputStream(localFile));
        } catch (FileNotFoundException ex) {
            AvocadoLogger.error("Error while opening local file " + remoteFile);
            this.close();
            return;
        }

        // Get ready to read file
        int totalLength;
        try {
            totalLength = in.available();
        } catch (IOException ex) {
            AvocadoLogger.error("Error while checking local file " + remoteFile);
            this.close();
            return;
        }

        // Get ready to send file
        AvocadoLogger.info("Sending " + remoteFile + "...");
        short blockNumber = 1;
        int sentLength = 0;
        Packet dataPacket = null, ackPacket;
        boolean nextBlock = true;

        //System.out.println("Total length: " + totalLength);
        // File sending loop
        while (true) {

            if (nextBlock) {
                retries = 0;
                // Read file content
                data = new byte[Packet.MAX_DATA_SIZE];
                int length;
                try {
                    length = in.read(data, 0, Packet.MAX_DATA_SIZE);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                    AvocadoLogger.error("Error while reading local file " + remoteFile);
                    break;
                }
                sentLength += length;

                // Prepare DATA packet to send
                dataPacket = PacketFactory.createData(data, blockNumber, length);
            } else {
                retries++;
            }
            
            if (retries > 2) {
                AvocadoLogger.error("3 timeouts : connection lost");
                this.close();
                return;
            }

            try {
                // Rocket launch!
                this.sendPacket(dataPacket, transferPort);

                // Try to get ACK packet from server
                ackPacket = Packet.receivePacket(socket);

                // Examine response
                if (ackPacket.isERR()) {
                    // An error occured :(
                    AvocadoLogger.error("Server Error: " + Error.ERROR_MESSAGES[response.getErrorCode()]);
                    System.out.println(remoteFile + ": CREM = " + response.getErrorCode());
                    break;
                } else if (ackPacket.getBlockNumber() == blockNumber) {
                    // If we are here it's all good
                    if (sentLength >= totalLength) {
                        // Transfer is finished
                        AvocadoLogger.success(remoteFile + " successfully sent");
                        System.out.println(remoteFile + ": CREM = 0");
                        break;
                    }
                    // Transfer not finished go for next block
                    nextBlock = true;
                    blockNumber++;
                } else {
                    // Block number do NOT match
                    nextBlock = false;
                }
            } catch (IOException ex) {
                // Something went wrong -> resend last data packet
                nextBlock = false;
                AvocadoLogger.info("Timeout! Resending last packet of " + remoteFile);
            }
        }
        // Close up
        this.close();
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(SendWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.close();
    }

}
