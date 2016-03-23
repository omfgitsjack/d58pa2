import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by omfgitsjack on 2016-03-16.
 */
public class VRouter {

	public static HashMap<String,String> ForwardingTableMap = new HashMap();
	public static HashMap<String,String> InterfacesMap = new HashMap();

	//Converts string decimal ip to binary, example "255.255.255.0" = "11111111.11111111.11111111.00000000"
	public static String binaryConvert(String ip){
		String[] prefix = ip.split("\\.");
		String binaryPrefix = "";
		for (int i = 0; i < prefix.length; i++){
			String binary = Integer.toBinaryString(Integer.parseInt(prefix[i]));
			while (binary.length() < 8){
				binary = "0" + binary;
			}
    		binaryPrefix +=  binary + "."; 
		}
		return binaryPrefix.substring(0, binaryPrefix.length()-1);
	}
	
	//Converts string[] binary ip to string, example ["11111111","11111111","11111111","00000000"] = "255.255.255.0"
	public static String decimalConvert(String[] binaryIp) throws NullPointerException{
		String ip = "";
		for (String s: binaryIp){
    		ip += Integer.parseInt(s,2) +"."; 
		}
		return ip.substring(0,ip.length()-1);
	}
	
	//Setup both interface and forwardingtable hashmap
	public static void Setup() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("Interfaces.txt"));
	    String line = br.readLine();
	    String[] split = new String[3];
	    while (line != null){
	    	line = line.substring(1, line.length()-1);
	    	split = line.split(";");
	    	String[] prefix = binaryConvert(split[1]).split("\\.");
	    	int prefixNum = 0;
	    	for (int i = 0; i < 4; i++){
	    		if (i == 3){
	    			while (prefix[3].endsWith("0")){
	    				prefix[3] = prefix[3].substring(0, prefix[3].length()-1);
	    			}
	    		}
	    		prefixNum += prefix[i].length(); 
	    	}
	    	System.out.println(prefixNum);

	    	InterfacesMap.put(split[0]+"/"+Integer.toString(prefixNum), split[2]);
	    	line = br.readLine();
	    }
	    br.close();
	    
	    
	    br = new BufferedReader(new FileReader("ForwardingTable.txt"));
	    line = br.readLine();
	    while (line != null){
	    	line = line.substring(1, line.length()-1);
	    	split = line.split(";");
	    	String[] prefix = binaryConvert(split[1]).split("\\.");
	    	String[] keyPrefix = binaryConvert(split[3]).split("\\.");
	    	int prefixNum = 0;
	    	int keyPrefixNum = 0;
	    	for (int i = 0; i < 4; i++){
	    		if (i == 3){
	    			while (prefix[3].endsWith("0")){
	    				prefix[3] = prefix[3].substring(0, prefix[3].length()-1);
	    			}
	    			while (keyPrefix[3].endsWith("0")){
	    				keyPrefix[3] = keyPrefix[3].substring(0, keyPrefix[3].length()-1);
	    				
	    			}
	    		}
	    		System.out.println("qqq"+keyPrefix[i]);
	    		prefixNum += prefix[i].length(); 
	    		keyPrefixNum += keyPrefix[i].length();
	    	}
	    	ForwardingTableMap.put(split[0]+"/"+Integer.toString(prefixNum), split[2]+"/" +keyPrefixNum);
	    	line = br.readLine();
	    }
	    br.close();
	}
	
	//Checks rather ipAddress is inside the interfaceMap or not
	//Since keys in InterfaceMap has mask we would get rid of the number after "\"
	//Example 172.11.0.2\23 => 172.11.0.2 
	@SuppressWarnings("unchecked")
	public static boolean lookupInterfaces(InetAddress ipAddress){
		String ip = ipAddress.toString().substring(1);
		Set<String> keys = InterfacesMap.keySet();
		for (String s: keys){
			if (s.substring(0, s.lastIndexOf("/")).equals(ip)){
				return true;
			}
		}
		return false;
	};
	
	@SuppressWarnings("unchecked")
	public static InetAddress lookupDest(InetAddress ipAddress) throws UnknownHostException{
		String ip = ipAddress.toString().substring(1);
		String binary = binaryConvert(ip);
    	Set<String> allKey = ForwardingTableMap.keySet();
    	int mask = 0;
    	int right = 0;
    	int wrong = 0;
    	int maxRight = 0;
    	int maxWrong = 0;
    	String ipReturn = "";
    	for (String s: allKey){
    		String keys = binaryConvert(s.substring(0,s.lastIndexOf("/")));
    		mask = Integer.parseInt(s.substring(s.lastIndexOf("/")+1));
    		String keyIp = keys;
    		keys = keys.substring(0, keys.length()- mask);
   
    		for (int l = 1; l <= keys.length(); l++){
    			
    			if (keys.substring(0,l).equals(binary.substring(0,l))){
    				right += 1;
    			}
    			else{
    				wrong += 1;
    			}
    		}
    		if (right > maxRight){
    			maxRight = right;
    			maxWrong = wrong;
    			ipReturn = keyIp;
    		}
    		else if (right == maxRight){
    			if (wrong < maxWrong){
    				maxRight = right;
    				maxWrong = wrong;
    				ipReturn = keyIp;
    			}
    		}
    	}
    	if (maxRight == 0){
    		return null;
    	}
    	ipReturn = decimalConvert(ipReturn.split("\\.")) + "/" + Integer.toString(mask);
    	ipReturn = ForwardingTableMap.get(ipReturn);
    	ipReturn = ipReturn.substring(0, ipReturn.lastIndexOf("/"));
    	return(InetAddress.getByName(ipReturn));
		
	};
	
	//Main function
	public VRouter() throws IOException{
	    Setup();
	}
	
    public static List<IP4Packet> incomingPackets(String fileName) throws IOException {
        return IP4Packet.parseFile(fileName);
    }

    static List<IP4Packet> fragment(IP4Packet ip4packet, int MTU) {
        ArrayList<IP4Packet> l = new ArrayList<>();
        if (ip4packet.getTotalLength() <= MTU) { // No need to fragment
            ip4packet.setMFFlag(0);
            ip4packet.setFragmentOffset(0);
            l.add(ip4packet);

            return l;
        }

        if (ip4packet.getDFFlag() == 1) { // Don't fragment.
            dropPacket(ip4packet.getSrcAddress(), ip4packet.getDestAddress(), ip4packet.getIdentification(), "Fragmentation needed and DF set.");
            return new ArrayList<>();
        }

        // calculate total fragments needed
        int payloadSize = ip4packet.getTotalLength() - 20;
        int effectiveMTU = MTU - 20; // How much of actual payload can we transfer
        int totalFragmentsNeeded = (int) Math.ceil( payloadSize / (double) effectiveMTU );

        // Build fragments
        for (int i = 0; i < totalFragmentsNeeded; i++) {
            IP4Packet packet = ip4packet.clone();
            packet.setFragmentOffset(i);

            if (i == totalFragmentsNeeded -1) { // Is last fragment
                int lastFragmentSize = (payloadSize % effectiveMTU);
                if (lastFragmentSize == 0) { // Last fragment, use all the payload space.
                    packet.setTotalLength(MTU);
                } else {
                    packet.setTotalLength(lastFragmentSize + 20);
                }

                packet.setMFFlag(0);
            } else {
                packet.setTotalLength(MTU);
                packet.setMFFlag(1);
            }

            packet.setChecksum(VRouter.checksum(packet));
            l.add(packet);
        }

        return l;
    }

    static boolean dropPacket(InetAddress sourceAddress, InetAddress destAddress, int ID, String message) {
        FileOutputStream oStream = null;
        String outputPath = "messages.txt";

        try {
            File f = new File(outputPath);
            boolean shouldAppend = f.exists() && !f.isDirectory();

            oStream = new FileOutputStream("messages.txt", shouldAppend);
            DataOutputStream dataOut = new DataOutputStream(oStream);

            String outputMessage = "Packet " + ID + " from " + sourceAddress.getHostAddress() + " to " + destAddress.getHostAddress() +": " + message;
            if (shouldAppend) dataOut.writeChars("\n");
            dataOut.writeChars(outputMessage);

            dataOut.close();
            oStream.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean forward(IP4Packet ip4packet, InetAddress networkInterface) {
        ip4packet.setChecksum(checksum(ip4packet));
        String delimeterRegex = "; ";
        String outputPath = "OutPackets.txt";
        try {
            File f = new File(outputPath);
            boolean shouldAppend = f.exists() && !f.isDirectory();
            FileOutputStream oStream = new FileOutputStream(outputPath, shouldAppend);
            DataOutputStream dataOut = new DataOutputStream(oStream);

            String l1 = Integer.toString(ip4packet.getVersion()) + delimeterRegex +
                    Integer.toString(ip4packet.getIHL()) + delimeterRegex +
                    Integer.toString(ip4packet.getToS()) + delimeterRegex +
                    Integer.toString(ip4packet.getTotalLength()) + "\n";
            String l2 = Integer.toString(ip4packet.getIdentification()) + delimeterRegex +
                    ip4packet.getFlags() + delimeterRegex +
                    Integer.toString(ip4packet.getFragmentOffset()) + "\n";
            String l3 = Integer.toString(ip4packet.getTTL()) + delimeterRegex +
                    Integer.toString(ip4packet.getProtocol()) + delimeterRegex +
                    ip4packet.getChecksum() + "\n";
            String l4 = ip4packet.getSrcAddress().getHostAddress() + "\n";
            String l5 = ip4packet.getDestAddress().getHostAddress() + "\n";
            String l6 = networkInterface.getHostAddress();

            if (shouldAppend) dataOut.writeChars("\n\n");
            dataOut.writeChars(l1);
            dataOut.writeChars(l2);
            dataOut.writeChars(l3);
            dataOut.writeChars(l4);
            dataOut.writeChars(l5);
            dataOut.writeChars(l6);

            dataOut.close();
            oStream.close();

            return true;
        } catch (IOException e) {
            return false;
        }
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

        String[] source = packet.getSrcAddress().getHostAddress().split("\\.");
        packetBytes[12] = new Integer(source[0]).byteValue();
        packetBytes[13] = new Integer(source[1]).byteValue();
        packetBytes[14] = new Integer(source[2]).byteValue();
        packetBytes[15] = new Integer(source[3]).byteValue();

        String[] dest = packet.getDestAddress().getHostAddress().split("\\.");
        packetBytes[16] = new Integer(dest[0]).byteValue();
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

class IP4Packet implements Cloneable {

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

    public int getDFFlag() {
        return Integer.parseInt(flags.split("")[1]);
    }

    @Override
    protected IP4Packet clone() {
        return new IP4Packet(version, IHL, ToS, totalLength, identification, flags, fragmentOffset, TTL, protocol, checksum, srcAddress, destAddress);
    }

    public void setDFFlag(int flag) {
        String firstBit = Integer.toString(0);
        String secondBit = Integer.toString(flag);
        String thirdBit = flags.split("")[2];

        this.setFlags(firstBit + secondBit + thirdBit);
    }

    public int getMFFlag() {
        return Integer.parseInt(flags.split("")[2]);
    }

    public void setMFFlag(int flag) {
        String firstBit = Integer.toString(0);
        String secondBit = flags.split("")[1];
        String thirdBit = Integer.toString(flag);

        this.setFlags(firstBit + secondBit + thirdBit);
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
