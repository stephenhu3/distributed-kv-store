package A1.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class UDPRequest {

    private static final int MAX_MSG_SIZE = 16384;
    private static final int UNIQUE_ID_SIZE = 16;
    private static final int SECRET_CODE_LEN_SIZE = 4;

    public static byte[] generateRequest(int snum, byte[] uniqueID) throws NoSuchAlgorithmException {
        // Maximum payload size 16KB
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_MSG_SIZE);
        byteBuffer.limit(16384);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // Request message format: the first 16 bytes are the request’s unique ID,
        // the rest is the application level payload.
        byteBuffer.put(uniqueID);
        // The client’s payload (student id) is an integer (4 bytes), in little-endian format.
        // 8 digit student number, each hex digit is 4 bits, 4*8 = 32 bits = 4 bytes
        byteBuffer.putInt(snum);
        System.out.println("Position: " + byteBuffer.position());
        System.out.println("Remaining: " + byteBuffer.remaining());
        return byteBuffer.array();
    }

    /*
    Request message format: the first 16 bytes are the request’s unique ID,
    the rest is the application level payload.
    */
    private static byte[] generateUniqueID() throws NoSuchAlgorithmException {
        byte[] uniqueId = new byte[UNIQUE_ID_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(uniqueId);
        return uniqueId;
    }

    public static byte[] sendRequest(String ip, int port, int snum)
            throws IOException, NoSuchAlgorithmException {
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] uniqueID = generateUniqueID();
        byte[] req = generateRequest(snum, uniqueID);
        InetAddress address = InetAddress.getByName(ip);
        DatagramPacket reqPacket = new DatagramPacket(req, req.length, address, port);
        socket.send(reqPacket);

        /*
        First 16 bytes are the unique ID of the corresponding request, rest is the application level
        payload. Maximum payload size 16KB.
        Payload has format: Secret code length (integer, 4 bytes, big-endian),
        Secret code (byte array), Possible padding (to be ignored)
        */

        DatagramPacket resPacket = new DatagramPacket(req, req.length);
        socket.receive(resPacket);

        // check matching uniqueID
        byte[] res = resPacket.getData();
        if (!Arrays.equals(Arrays.copyOf(res, UNIQUE_ID_SIZE), uniqueID)) {
            System.out.println("uniqueID not matching");
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(SECRET_CODE_LEN_SIZE);
        byteBuffer.limit(SECRET_CODE_LEN_SIZE);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.put(resPacket.getData());
        int secretCodeLength = byteBuffer.getInt();

        System.out.println("Secret code length: " + SECRET_CODE_LEN_SIZE);

        socket.close();
        return null;
    }
}
