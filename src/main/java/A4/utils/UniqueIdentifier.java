package A4.utils;

import static A4.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;

import java.lang.management.ManagementFactory;
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
}
