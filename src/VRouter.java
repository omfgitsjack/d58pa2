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

    public static String checksum(IP4Packet packet) {
        byte[] packetBytes = new byte[20];

        packetBytes[0] = new Integer((packet.getVersion() << 4) + packet.getIHL()).byteValue(); // Version + IHL
        packetBytes[1] = new Integer(packet.getToS()).byteValue(); // ToS
        packetBytes[2] = new Integer(packet.getTotalLength() >> 8).byteValue(); // Higher-order byte of Total Length
        packetBytes[3] = new Integer(packet.getTotalLength()).byteValue(); // Lower-order byte of Total Length

        packetBytes[4] = new Integer(packet.getIdentification() >> 8).byteValue();
        packetBytes[5] = new Integer(packet.getIdentification()).byteValue();
        packetBytes[6] = new Integer(
                (Integer.parseInt(String.valueOf(packet.getFlags().charAt(1))) << 6) +
                (Integer.parseInt(String.valueOf(packet.getFlags().charAt(2))) << 5) +
                (packet.getFragmentOffset() >> 8) ).byteValue(); // flag bits + 5 frag offset higher bits
        packetBytes[7] = new Integer(packet.getFragmentOffset()).byteValue();

        packetBytes[8] = new Integer(packet.getTTL()).byteValue();
        packetBytes[9] = new Integer(packet.getProtocol()).byteValue();
        packetBytes[10] = new Integer(0).byteValue();
        packetBytes[11] = new Integer(0).byteValue();

        String[] source = packet.getSrcAddress().toString().split("\\.");
        packetBytes[12] = new Integer(source[0].replace("/", "")).byteValue();
        packetBytes[13] = new Integer(source[1]).byteValue();
        packetBytes[14] = new Integer(source[2]).byteValue();
        packetBytes[15] = new Integer(source[3]).byteValue();

        String[] dest = packet.getDestAddress().toString().split("\\.");
        packetBytes[16] = new Integer(dest[0].replace("/", "")).byteValue();
        packetBytes[17] = new Integer(dest[1]).byteValue();
        packetBytes[18] = new Integer(dest[2]).byteValue();
        packetBytes[19] = new Integer(dest[3]).byteValue();

        long checkSumLong = IP4Packet.calculateChecksum(packetBytes);
        char[] checkSumChar = Long.toBinaryString(checkSumLong).toCharArray();
        String checksum = "";

        for (int i = 0; i < checkSumChar.length; i++) {
            checksum += checkSumChar[i];
            if ((i + 1) % 4 == 0 && i != checkSumChar.length-1) {
                checksum += "-";
            }
        }
        return checksum;
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

    public static long calculateChecksum(byte[] buf) {
        int length = buf.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            sum += (buf[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return sum;

    }

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
