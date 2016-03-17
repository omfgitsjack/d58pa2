import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        ArrayList<IP4Packet> packets = (ArrayList<IP4Packet>) VRouter.incomingPackets("InPackets.txt");

        System.out.println(VRouter.checksum(packets.get(0)));
        System.out.println(VRouter.checksum(packets.get(1)));
        System.out.println(VRouter.checksum(packets.get(2)));

        System.out.println(VRouter.dropPacket(packets.get(0).getSrcAddress(), packets.get(0).getDestAddress(), 1, "heres a message"));
    }
}
