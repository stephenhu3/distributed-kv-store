package A7.utils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ByteRepresentationTest {
    @org.junit.Test
    public void bytesToHex() throws Exception {
        String str = "byte";
        byte[] strBytes = str.getBytes();
        assertEquals ("62797465", ByteRepresentation.bytesToHex(strBytes));
    }

    @org.junit.Test
    public void hexToBytes() throws Exception {
        String str = "byte";
        String byteStr = "62797465";
        assert (Arrays.equals(str.getBytes(), ByteRepresentation.hexToBytes(byteStr)));
    }

    @org.junit.Test
    public void mapToBytes() throws Exception {
        Map<InetAddress, Integer> map = new HashMap<InetAddress, Integer>();
        InetAddress addr = InetAddress.getLocalHost();
        map.put(addr, 0);
        byte[] mapBytes = ByteRepresentation.mapToBytes(map);
        assertEquals (map, ByteRepresentation.bytesToMap(mapBytes));
    }
}