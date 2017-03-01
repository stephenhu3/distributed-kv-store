package A4.utils;

import static A4.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;

import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UniqueIdentifier {
    public static byte[] generateUniqueID() throws NoSuchAlgorithmException {
        byte[] uniqueID = new byte[UNIQUE_ID_UDP_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(uniqueID);
        return uniqueID;
    }

    public static int getCurrentPID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] parts = name.split("@");
        return Integer.parseInt(parts[0]);
    }

    // MD5 hash
    public static String MD5Hash(String hash) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(hash.getBytes());
        return new BigInteger(1, messageDigest).toString(16);
    }
}
