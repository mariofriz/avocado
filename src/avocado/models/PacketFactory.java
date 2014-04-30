package avocado.models;

import static avocado.models.Packet.ACK;
import static avocado.models.Packet.DATA;
import static avocado.models.Packet.ERROR;
import static avocado.models.Packet.RRQ;
import static avocado.models.Packet.WRQ;

/**
 * Factory for Packet
 *
 * @author Mario
 */
public class PacketFactory {

    public static Packet createRRQ(String filename) {
        Packet packet = new Packet();
        packet.writeShort(RRQ);
        packet.writeString(filename);
        packet.writeByte((byte) 0);
        packet.writeString(Client.TRANSFER_MODE);
        packet.writeByte((byte) 0);
        return packet;
    }

    public static Packet createWRQ(String filename) {
        Packet packet = new Packet();
        packet.writeShort(WRQ);
        packet.writeString(filename);
        packet.writeByte((byte) 0);
        packet.writeString(Client.TRANSFER_MODE);
        packet.writeByte((byte) 0);
        return packet;
    }

    public static Packet createData(byte[] data, short blockNumber, int numBytes) {
        Packet packet = new Packet();
        packet.writeShort(DATA);
        packet.writeShort(blockNumber);
        packet.writeBytes(data, numBytes);
        return packet;
    }

    public static Packet createACK() {
        Packet packet = new Packet();
        packet.writeShort(ACK);
        return packet;
    }

    public static Packet createACK(short blockNumber) {
        Packet packet = new Packet();
        packet.writeShort(ACK);
        packet.writeShort(blockNumber);
        return packet;
    }

    public static Packet createERR(String errMsg) {
        Packet packet = new Packet();
        packet.writeShort(ERROR);
        packet.writeString(errMsg);
        return packet;
    }

}
