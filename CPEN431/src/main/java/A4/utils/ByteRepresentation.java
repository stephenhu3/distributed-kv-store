package A4.utils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;

public class ByteRepresentation {
    public static String bytesToHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    public static byte[] hexToBytes(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }

    public static byte[] hashMapToBytes(HashMap<InetAddress, Integer> map) {
        ByteArrayOutputStream byteOut;
        ObjectOutputStream out;
        byte[] res;
        try {
            byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(map);
            res = byteOut.toByteArray();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<InetAddress, Integer> bytesToHashMap(byte[] bytes) {
        ByteArrayInputStream byteIn;
        ObjectInputStream in;
        HashMap<InetAddress, Integer> res;
        try {
            byteIn = new ByteArrayInputStream(bytes);
            in = new ObjectInputStream(byteIn);
            res = (HashMap<InetAddress, Integer>) in.readObject();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
