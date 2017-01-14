package A2.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static A2.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;

public class UniqueIdentifier {
    public static byte[] generateUniqueID() throws NoSuchAlgorithmException {
        byte[] uniqueID = new byte[UNIQUE_ID_UDP_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(uniqueID);
        return uniqueID;
    }
}
