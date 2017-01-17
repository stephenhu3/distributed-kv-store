package A3.utils;

import static A3.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UniqueIdentifier {
    public static byte[] generateUniqueID() throws NoSuchAlgorithmException {
        byte[] uniqueID = new byte[UNIQUE_ID_UDP_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(uniqueID);
        return uniqueID;
    }
}
