package avocado.models;

import avocado.helpers.AvocadoLogger;
import avocado.models.workers.ReceiveWorker;
import avocado.models.workers.SendWorker;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;

/**
 * Client model
 * @author Mario
 */
public class Client extends Observable {

    public static final String TRANSFER_MODE = "netascii";
    public static final int DEFAULT_PORT = 69;
    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_TIMEOUT = 30000; //30secs

    private DatagramSocket socket;
    private String remoteIp;
    private InetAddress remoteHost;
    private Thread workerThread;
    private int remotePort;

    public Client() throws SocketException {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(DEFAULT_TIMEOUT);
        this.remotePort = DEFAULT_PORT;
    }

    public void sendFile(String localFile, String remoteFile) throws IOException {
        SendWorker worker = new SendWorker(remoteHost, socket, localFile, remoteFile);
        workerThread = new Thread(worker);
        workerThread.start();
    }

    public void receiveFile(String remoteFile, String localFile) throws FileNotFoundException, IOException {
        ReceiveWorker worker = new ReceiveWorker(remoteHost, socket, localFile, remoteFile);
        workerThread = new Thread(worker);
        workerThread.start();
    }
    
    public void close()
    {
        if (workerThread != null) {
            workerThread.interrupt();
        }
        socket.close();
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) throws UnknownHostException {
        this.remoteIp = remoteIp;
        this.remoteHost = InetAddress.getByName(remoteIp);
        AvocadoLogger.info("Remote host is now: " + this.remoteIp);
    }

}
