package avocado.models;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;

/**
 *
 * @author Mario
 */
public class Client extends Observable {

    public static final String TRANSFER_MODE = "netascii";

    private DatagramSocket socket;
    private String remoteIp;
    private InetAddress remoteHost;
    private int remotePort;

    public Client() throws SocketException {
        this.socket = new DatagramSocket();
        this.remotePort = 69;
        System.out.println(System.getProperty("user.dir"));
    }

    public boolean sendFile(String localFile, String remoteFile) throws IOException {

        // Send WRQ packet
        Packet packet = Packet.createWRQ(remoteFile);
        this.sendPacket(packet);

        // Log message
        this.logMessage("Sent WRQ for " + remoteFile);

        // Prepare buffer and file reader
        byte[] data;
        DataInputStream in = new DataInputStream(new FileInputStream(localFile));
        
        int totalLength = in.available();
        int sentLength = 0;        
        System.out.println(totalLength);
        
        // Wait for ACK
        Packet ack = Packet.receivePacket(socket);

        // Get ready to send
        while (true) {
            // Read file content
            data = new byte[Packet.MAX_DATA_SIZE];
            int length = in.read(data, 0, Packet.MAX_DATA_SIZE);
            sentLength += length;

            System.out.println(sentLength);
            // Send DATA packet
            packet = Packet.createData(data, length);
            this.sendPacket(packet);

            // Wait for ACK
            ack = Packet.receivePacket(socket);

            // Check if file has been sent completely
            if (sentLength >= totalLength) {
                this.logMessage(remoteFile + " successfully sent");
                break;
            }
        }

        return false;
    }

    public void receiveFile(String remoteFile, String localFile) throws FileNotFoundException, IOException {

        // Send RRQ packet
        Packet packet = Packet.createRRQ(remoteFile);
        this.sendPacket(packet);

        // Log message
        this.logMessage("Sent RRQ for " + remoteFile);

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

        // Get ready to receive packets
        Packet lastPacket;
        while (true) {
            // Get parsed received packet
            lastPacket = Packet.receivePacket(socket);

            // Get data and write to file
            System.out.println(new String(lastPacket.getData(), "UTF-8"));
            out.write(lastPacket.getData());

            // Return acknowledge packet
            packet = Packet.createACK();
            this.sendPacket(packet);

            // Check if last data packet
            if (lastPacket.packetSize() < Packet.MAX_DATA_SIZE) {
                System.out.println("Finished transfer!");
                // Log message
                this.logMessage(remoteFile + "successfully received");
                break;
            }
        }
    }

    private void sendPacket(Packet packet) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.packetSize(), remoteHost, remotePort);
        this.socket.send(sendPacket);
    }

    private void logMessage(String message) {
        this.setChanged();
        this.notifyObservers(message);
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) throws UnknownHostException {
        this.remoteIp = remoteIp;
        this.remoteHost = InetAddress.getByName(remoteIp);
        this.logMessage("New remote host: " + this.remoteIp);
    }

}
