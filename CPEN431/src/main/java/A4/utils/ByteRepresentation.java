package A4.utils;

import javax.xml.bind.DatatypeConverter;

public class ByteRepresentation {
    public static String bytesToHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    public static byte[] hexToBytes(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }
}
