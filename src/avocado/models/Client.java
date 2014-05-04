package avocado.models;

import avocado.helpers.AvocadoLogger;
import avocado.models.workers.ReceiveWorker;
import avocado.models.workers.SendWorker;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client model
 * @author Mario
 */
public class Client {

    public static final String TRANSFER_MODE = "netascii";
    public static final int DEFAULT_PORT = 69;
    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_TIMEOUT = 15000; //15secs

    private String remoteIp;
    private InetAddress remoteHost;
    private ExecutorService threadPool;
    private int remotePort;

    public Client() throws SocketException {
        this.remotePort = DEFAULT_PORT;
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void sendFile(String localFile, String remoteFile) throws IOException {
        Thread sendThread = new Thread(new SendWorker(remoteHost, localFile, remoteFile));
        threadPool.submit(sendThread);
    }

    public void receiveFile(String remoteFile, String localFile) throws FileNotFoundException, IOException {
        Thread receiveThread = new Thread(new ReceiveWorker(remoteHost, localFile, remoteFile));
        threadPool.submit(receiveThread);
    }
    
    public void close()
    {
        // Shutdown all workers
        threadPool.shutdown();
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
