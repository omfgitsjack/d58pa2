import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by omfgitsjack on 2016-03-16.
 */
public class VRouter {

    public static List<IP4Packet> incomingPackets(String fileName) throws IOException {
        return IP4Packet.parseFile(fileName);
    }
}

class IP4Packet {

    // Line 1
    private int version;
    private int IHL;
    private int ToS;
    private int totalLength;
    // Line 2
    private int identification;
    private String flags;
    private int fragmentOffset;
    // Line 3
    private int TTL;
    private int protocol;
    private String checksum;
    // Line 4 & 5
    private InetAddress srcAddress;
    private InetAddress destAddress;

    public IP4Packet(int version, int IHL, int ToS, int totalLength, int identification, String flags, int fragmentOffset, int TTL, int protocol, String checksum, InetAddress srcAddress, InetAddress destAddress) {
        this.version = version;
        this.IHL = IHL;
        this.ToS = ToS;
        this.totalLength = totalLength;
        this.identification = identification;
        this.flags = flags;
        this.fragmentOffset = fragmentOffset;
        this.TTL = TTL;
        this.protocol = protocol;
        this.checksum = checksum;
        this.srcAddress = srcAddress;
        this.destAddress = destAddress;
    }

    public static List<IP4Packet> parseFile(String fileName) throws IOException {
        ArrayList<IP4Packet> list = new ArrayList<IP4Packet>();
        String delimiterRegex = "; ";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            while (true) {
                String firstLine = reader.readLine();
                String[] line1 = firstLine.split(delimiterRegex);
                int version = Integer.parseInt(line1[0]);
                int IHL = Integer.parseInt(line1[1]);
                int ToS = Integer.parseInt(line1[2]);
                int totalLength = Integer.parseInt(line1[3]);

                // Line 2
                String[] line2 = reader.readLine().split(delimiterRegex);
                int identification = Integer.parseInt(line2[0]);
                String flags = line2[1];
                int fragmentOffset = Integer.parseInt(line2[2]);

                // Line 3
                String[] line3 = reader.readLine().split(delimiterRegex);
                int TTL = Integer.parseInt(line3[0]);
                int protocol = Integer.parseInt(line3[1]);
                String checksum = line3[2];

                // Line 4 & 5
                InetAddress srcAddress = InetAddress.getByName(reader.readLine());
                InetAddress destAddress = InetAddress.getByName(reader.readLine());

                list.add(new IP4Packet(version, IHL, ToS, totalLength, identification, flags, fragmentOffset, TTL, protocol, checksum, srcAddress, destAddress));

                String emptyLine = reader.readLine();
                if (emptyLine == null) break;
            }

            return list;
        } catch (Exception e) {
            throw e;
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getIHL() {
        return IHL;
    }

    public void setIHL(int IHL) {
        this.IHL = IHL;
    }

    public int getToS() {
        return ToS;
    }

    public void setToS(int toS) {
        ToS = toS;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public int getIdentification() {
        return identification;
    }

    public void setIdentification(int identification) {
        this.identification = identification;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public int getFragmentOffset() {
        return fragmentOffset;
    }

    public void setFragmentOffset(int fragmentOffset) {
        this.fragmentOffset = fragmentOffset;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public InetAddress getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(InetAddress srcAddress) {
        this.srcAddress = srcAddress;
    }

    public InetAddress getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(InetAddress destAddress) {
        this.destAddress = destAddress;
    }
}
