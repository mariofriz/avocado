package avocado.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Packet class
 *
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
    private int opcode;
    private int blockNumber;
    private int errorCode;
    private byte[] data;
    private int packetSize;
    private int fromPort;
    private InetAddress fromAddr;

    public Packet() {
        this.data = new byte[MAX_PACKET_SIZE];

        this.fromPort = -1;

        this.fromAddr = null;
    }

    public static Packet createRRQ(String filename) {
        Packet pkt = new Packet();

        pkt.writeShort(RRQ);

        pkt.writeString(filename);

        pkt.writeByte((byte) 0);

        return pkt;
    }

    /**
     * Creates a write request packet for a given filename.
     */
    public static Packet createWRQ(String filename) {
        Packet pkt = new Packet();

        pkt.writeShort(WRQ);

        pkt.writeString(filename);

        pkt.writeByte((byte) 0);

        return pkt;
    }

    public static Packet createData(byte[] data, int numBytes) {
        Packet pkt = new Packet();

        pkt.writeShort(DATA);

        pkt.writeBytes(data, numBytes);

        return pkt;
    }

    public static Packet createACK() {
        Packet pkt = new Packet();

        pkt.writeShort(ACK);

        return pkt;
    }

    public static Packet createERR(String errMsg) {
        Packet pkt = new Packet();

        pkt.writeShort(ERROR);

        pkt.writeString(errMsg);

        return pkt;
    }

    public static Packet receivePacket(DatagramSocket net) throws IOException {
        System.out.println("Receiving packet");
        Packet netPkt = new Packet();

        byte[] netBytes = new byte[MAX_PACKET_SIZE];

        DatagramPacket recvPacket = new DatagramPacket(netBytes, netBytes.length);

        // This should set recvPacket's length to the number of bytes received
        net.receive(recvPacket);

        System.out.println("Received packet");

        // Parse packet
        netPkt.parsePacket(recvPacket);

        return netPkt;
    }

    private void parsePacket(DatagramPacket packet) {
        rawData = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());

        // Read opcode
        byte[] byteCode = new byte[2];
        rawData.read(byteCode, 0, 2);
        opcode = new BigInteger(byteCode).intValue();

        // Read next 2 bytes for errorCode or blockNumber
        rawData.read(byteCode, 0, 2);
        if (opcode == ERROR) {
            errorCode = new BigInteger(byteCode).intValue();
        } else {
            blockNumber = new BigInteger(byteCode).intValue();
        }

        // Read data if available
        packetSize = rawData.available();
        rawData.read(data, 0, packetSize);

        // Read port and address
        fromPort = packet.getPort();
        fromAddr = packet.getAddress();
    }

    public int fromPort() {
        return fromPort;
    }

    public InetAddress fromAddr() {
        return fromAddr;
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

    public int packetSize() {
        return packetSize;
    }

    public short getShort(int idx) {
        return (short) ((data[idx] << 8) | data[idx + 1]);
    }

    public String getString(int idx) {
        int numBytes = 0;

        // Pass #1 to count bytes
        for (int i = idx;; ++i) {
            if (data[i] == 0) {
                break;
            }
            ++numBytes;
        }

        byte[] strBytes = new byte[numBytes];

        // Pass #2 to build string
        for (int i = 0; i < numBytes; ++i) {
            strBytes[i] = data[idx + i];
        }

        return new String(strBytes);
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

    public String toString() {
        return Arrays.toString(data);
    }

}
