package avocado.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Packet model
 * @author Mario
 */
public class Packet {

    public static final short RRQ = 1;
    public static final short WRQ = 2;
    public static final short DATA = 3;
    public static final short ACK = 4;
    public static final short ERROR = 5;

    public static final int MAX_PACKET_SIZE = 1024;
    public static final int MAX_DATA_SIZE = 512;

    private ByteArrayInputStream rawData;
    private short opcode;
    private short blockNumber;
    private short errorCode;
    private byte[] data;
    private int packetSize;
    private int serverPort;
    private InetAddress serverAddress;

    public Packet() {
        this.data = new byte[MAX_PACKET_SIZE];
        this.serverPort = -1;
        this.serverAddress = null;
    }

    public static Packet receivePacket(DatagramSocket socket) throws IOException {
        //System.out.println("Receiving packet");
        Packet packet = new Packet();

        byte[] buffer = new byte[MAX_PACKET_SIZE];

        DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(dataPacket);

        //System.out.println("Received packet");
        // Parse packet
        packet.parsePacket(dataPacket);

        return packet;
    }

    private void parsePacket(ByteArrayInputStream raw, int port, InetAddress address) {
        rawData = raw;

        // Read opcode
        byte[] byteCode = new byte[2];
        rawData.read(byteCode, 0, 2);
        opcode = new BigInteger(byteCode).shortValue();

        // Read next 2 bytes for errorCode or blockNumber
        rawData.read(byteCode, 0, 2);
        if (opcode == ERROR) {
            errorCode = new BigInteger(byteCode).shortValue();
        } else {
            blockNumber = new BigInteger(byteCode).shortValue();
        }

        // Read data if available
        packetSize = rawData.available();
        data = new byte[packetSize];
        rawData.read(data, 0, packetSize);

        // Read port and address
        serverAddress = address;
        serverPort = port;
    }

    private void parsePacket(DatagramPacket packet) {
        ByteArrayInputStream raw = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        this.parsePacket(raw, packet.getPort(), packet.getAddress());
    }

    public void debug() {
        if (this.serverPort == -1) {
            this.parsePacket(new ByteArrayInputStream(data, 0, packetSize), serverPort, serverAddress);
        }
        try {
            System.out.println("opcode: " + opcode + "; number: " + blockNumber + "; dataSize: " + packetSize + " data: " + new String(data, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public short getOpcode() {
        return opcode;
    }

    public short getBlockNumber() {
        return blockNumber;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public boolean writeByte(byte b) {
        if (packetSize < MAX_PACKET_SIZE) {
            this.data[packetSize] = b;
            ++packetSize;
            return true;
        }

        return false;
    }

    public boolean writeShort(short s) {
        if (packetSize < MAX_PACKET_SIZE - 1) {
            byte b1 = (byte) ((s & 0xFF00) >> 8);
            byte b2 = (byte) (s & 0x00FF);

            writeByte(b1);
            writeByte(b2);

            return true;
        }

        return false;
    }

    public boolean writeBytes(byte[] data, int numBytes) {
        if (packetSize + numBytes < MAX_PACKET_SIZE) {
            for (int i = 0; i < numBytes; ++i) {
                writeByte(data[i]);
            }

            return true;
        }

        return false;
    }

    public boolean writeString(String s) {
        byte[] strBytes = s.getBytes();

        if (packetSize + strBytes.length < MAX_PACKET_SIZE) {
            for (int i = 0; i < strBytes.length; ++i) {
                writeByte(strBytes[i]);
            }

            return true;
        }

        return false;
    }

    public byte[] getData() {
        return data;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public boolean isRRQ() {
        return opcode == RRQ;
    }

    public boolean isWRQ() {
        return opcode == WRQ;
    }

    public boolean isDATA() {
        return opcode == DATA;
    }

    public boolean isACK() {
        return opcode == ACK;
    }

    public boolean isERR() {
        return opcode == ERROR;
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

}
