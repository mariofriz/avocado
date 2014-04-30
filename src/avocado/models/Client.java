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

        short blockNumber = 1;
        int totalLength = in.available();
        int sentLength = 0;
        System.out.println("Total length: " + totalLength);

        // Wait for ACK
        Packet ack = Packet.receivePacket(socket);
        System.out.println("server ack " + ack.getServerAddress().toString() + "@" + ack.getServerPort());
        int transferPort = ack.getServerPort();
        ack.debug();

        // Get ready to send
        while (true) {
            // Read file content
            data = new byte[Packet.MAX_DATA_SIZE];
            int length = in.read(data, 0, Packet.MAX_DATA_SIZE);
            sentLength += length;

            System.out.println("Sent length: " + sentLength);
            
            // Send DATA packet
            Packet dataPacket = Packet.createData(data, blockNumber, length);            
            this.sendPacket(dataPacket, transferPort);
            dataPacket.debug();

            // Wait for ACK
            ack = Packet.receivePacket(socket);
            ack.debug();

            // Check if file has been sent completely
            if (sentLength >= totalLength) {
                this.logMessage(remoteFile + " successfully sent", LogType.SUCCESS);
                break;
            }
            
            blockNumber++;
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
            out.write(lastPacket.getData());

            // Return acknowledge packet with block number
            Packet ackPacket = Packet.createACK(lastPacket.getBlockNumber());
            this.sendPacket(ackPacket);

            // Check if last data packet
            if (lastPacket.getPacketSize() < Packet.MAX_DATA_SIZE) {
                System.out.println("Finished transfer!");
                this.logMessage(remoteFile + " successfully received", LogType.SUCCESS);
                break;
            }
        }
    }
    
    private void sendPacket(Packet packet) throws IOException {
        this.sendPacket(packet, 69);
    }

    private void sendPacket(Packet packet, int port) throws IOException {
        System.out.println("Sending packet");
        DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getPacketSize(), remoteHost, port);
        this.socket.send(sendPacket);
    }

    private void logMessage(String message) {
        this.setChanged();
        this.notifyObservers(new Log(message, LogType.NORMAL));
    }

    private void logMessage(String message, LogType type) {
        this.setChanged();
        this.notifyObservers(new Log(message, type));
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) throws UnknownHostException {
        this.remoteIp = remoteIp;
        this.remoteHost = InetAddress.getByName(remoteIp);
        this.logMessage("Remote host is now: " + this.remoteIp, LogType.INFO);
    }

}
