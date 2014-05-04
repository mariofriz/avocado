package avocado.models.workers;

import avocado.helpers.AvocadoLogger;
import avocado.models.Client;
import avocado.models.Packet;
import avocado.models.PacketFactory;
import avocado.models.Error;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receive worker model
 *
 * @author Mario
 */
public class ReceiveWorker extends AbstractWorker {

    private DataOutputStream out;
    private File file;

    public ReceiveWorker(InetAddress remoteHost, String localFile, String remoteFile) throws SocketException {
        super(remoteHost, localFile, remoteFile);
    }

    @Override
    public void run() {
        // Prepare local file
        String localFilePath;
        if (localFile == null) {
            localFilePath = System.getProperty("user.dir") + File.separator + remoteFile;
        } else {
            localFilePath = localFile;
        }
        file = new File(localFilePath);

        try {
            // Prepare to write to local file
            out = new DataOutputStream(new FileOutputStream(localFilePath));
        } catch (FileNotFoundException ex) {
            AvocadoLogger.error("Error creating local file " + remoteFile);
            this.clean();
            return;
        }

        int transferPort = Client.DEFAULT_PORT;

        // Prepare RRQ packet
        Packet request, response;
        request = PacketFactory.createRRQ(remoteFile);

        // Send RRQ to server
        try {
            this.sendPacket(request);
            AvocadoLogger.log("Sent RRQ for " + remoteFile);
        } catch (IOException ex) {
            AvocadoLogger.error("Error while sending packet for file " + remoteFile);
            this.clean();
            return;
        }

        // Prepare to receive file
        short blockNumber = 1;
        boolean nextBlock = true;
        boolean finished = false;

        // File receiving loop
        while (!finished) {

            try {
                // Wait for response
                response = Packet.receivePacket(socket);

                if (response.isERR()) {
                    // An error occured :(
                    AvocadoLogger.error("Server Error: " + Error.ERROR_MESSAGES[response.getErrorCode()]);
                    this.clean();
                    break;
                } else if (response.getBlockNumber() == blockNumber) {
                    // If we are here it's all good
                    //Check if the server's port has changed
                    if (transferPort != response.getServerPort()) {
                        transferPort = response.getServerPort();
                        AvocadoLogger.info("Receiving " + remoteFile + "...");
                    }
                    // Get data and write to file
                    out.write(response.getData());
                    if (response.getPacketSize() < Packet.MAX_DATA_SIZE) {
                        // Transfer is finished
                        AvocadoLogger.success(remoteFile + " successfully received");
                        finished = true;
                    }
                    // Transfer not finished go for next block
                    nextBlock = true;
                } else {
                    // Block number do NOT match
                    nextBlock = false;
                }
            } catch (IOException ex) {
                AvocadoLogger.info("Timeout! Resending last packet of " + remoteFile);
                nextBlock = false;
            }

            if (nextBlock) {
                request = PacketFactory.createACK(blockNumber);
                blockNumber++;
            }

            try {
                // Rocket launch!
                this.sendPacket(request, transferPort);
            } catch (IOException ex) {
                AvocadoLogger.error("Error while sending packet for file " + remoteFile);
                this.clean();
                break;
            }

        }
        // Close up
        this.close();
    }

    public void clean() {
        if (file.exists()) {
            this.close();
            file.delete();
        }
    }

    @Override
    public void close() {
        try {
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.close();
    }

}
