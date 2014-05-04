
package avocado.models.workers;

import avocado.models.Client;
import static avocado.models.Client.DEFAULT_TIMEOUT;
import avocado.models.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;

/**
 * Abstract worker model
 * @author Mario
 */
public abstract class AbstractWorker extends Observable implements Runnable {
    
    protected InetAddress remoteHost;
    protected DatagramSocket socket;
    protected String localFile;
    protected String remoteFile;

    public AbstractWorker(InetAddress remoteHost, String localFile, String remoteFile) throws SocketException {
        this.remoteHost = remoteHost;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(DEFAULT_TIMEOUT);
        this.localFile = localFile;
        this.remoteFile = remoteFile;
    }
    
    protected void sendPacket(Packet packet) throws IOException {
        this.sendPacket(packet, Client.DEFAULT_PORT);
    }

    protected void sendPacket(Packet packet, int port) throws IOException {
        //System.out.println("Sending packet");
        DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getPacketSize(), remoteHost, port);
        this.socket.send(sendPacket);
    }
    
    @Override
    public abstract void run();
    
    public void close() {
        socket.close();
    }

}
