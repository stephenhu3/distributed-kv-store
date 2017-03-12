package A7.resources;

import static A7.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;
import static A7.utils.ByteRepresentation.bytesToHex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class RawBytesStudentNumberResponse {
    private static final int SECRET_CODE_LEN_SIZE = 4;

    /*
    First 16 bytes are the unique ID of the corresponding request, rest is the application level
    payload. Maximum payload size 16KB.
    Payload has format: Secret code length (integer, 4 bytes, big-endian),
    Secret code (byte array), Possible padding (to be ignored)
    */
    public static void parseResponse(byte[] res) throws Exception {
        // order response in big-endian format
        ByteBuffer byteBuffer = ByteBuffer.allocate(res.length);
        byteBuffer.limit(res.length);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.put(res);

        res = byteBuffer.array();
        // get secret code's length
        int secretCodeLength = byteBuffer.getInt(UNIQUE_ID_UDP_SIZE);
        System.out.println("Secret code length: " + secretCodeLength);

        // get secret code (little-endian)
        byte[] secretCode = Arrays.copyOfRange(res, UNIQUE_ID_UDP_SIZE + SECRET_CODE_LEN_SIZE,
                UNIQUE_ID_UDP_SIZE + SECRET_CODE_LEN_SIZE + secretCodeLength);
        String secretCodeHexString = bytesToHex(secretCode);
        System.out.println("Secret: " + secretCodeHexString);
    }
}
