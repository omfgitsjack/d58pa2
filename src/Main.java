import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
    	VRouter V = new VRouter();
        ArrayList<IP4Packet> packets = (ArrayList<IP4Packet>) V.incomingPackets("InPackets.txt");

        System.out.println(V.checksum(packets.get(0)));
        System.out.println(V.checksum(packets.get(1)));
        System.out.println(V.checksum(packets.get(2)));

        System.out.println(V.dropPacket(packets.get(0).getSrcAddress(), packets.get(0).getDestAddress(), 1, "heres a message"));
        System.out.println(V.forward(packets.get(0), InetAddress.getLocalHost()));

        System.out.println("some testing cases");
        String[] a = new String[]{"10","1000","1000","1"};
        System.out.println(V.decimalConvert(a));

        String b = "255.255.255.128";
        System.out.println(V.binaryConvert(b));

        System.out.println(V.ForwardingTableMap);
        System.out.println(V.InterfacesMap);

        System.out.println(V.lookupInterfaces(InetAddress.getByName("172.18.25.1")));



        System.out.println(V.lookupDest(InetAddress.getByName("172.18.25.1")));

        ArrayList<IP4Packet> fragmentedPackets = (ArrayList<IP4Packet>) VRouter.fragment(packets.get(2), 29);
        System.out.println(fragmentedPackets.size());
        
        System.out.println(V.lookupDest(InetAddress.getByName("150.111.0.5")));
    }
}
