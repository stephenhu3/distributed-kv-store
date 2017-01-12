package A1.resources;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static A1.DistributedSystemConfiguration.MSG_MAX_UDP_SIZE;
import static A1.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;
import static A1.DistributedSystemConfiguration.VERBOSE;

public class StudentNumberRequest {
    /*
    Request message format: the first 16 bytes are the request’s unique ID,
    the rest is the application level payload.
    */
    public static byte[] generateRequest(int snum, byte[] uniqueID) throws NoSuchAlgorithmException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(MSG_MAX_UDP_SIZE);
        byteBuffer.limit(MSG_MAX_UDP_SIZE);
        byteBuffer.order(ByteOrder.BIG_ENDIAN); // DEBUGGING: Try big-endian see if sample still works
        byteBuffer.put(uniqueID);
        /*
        The client’s payload (student id) is an integer (4 bytes), in little-endian format.
        8 digit student number, each hex digit is 4 bits, 4*8 = 32 bits = 4 bytes
        */
        byteBuffer.putInt(snum); // TODO: Double check this, seems like snum maybe entered in big-endian order instead of little endian
        if (VERBOSE) {
            System.out.println("Position: " + byteBuffer.position());
            System.out.println("Remaining: " + byteBuffer.remaining());
        }
        return byteBuffer.array();
    }

    public static byte[] generateUniqueID() throws NoSuchAlgorithmException {
        byte[] uniqueID = new byte[UNIQUE_ID_UDP_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(uniqueID);
        return uniqueID;
    }
}
