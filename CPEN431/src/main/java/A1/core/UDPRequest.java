package A1.core;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class UDPRequest {
    // max message size is 250 bytes, not 16 KB stated in criteria, based on my debuggin
    private static final int MAX_MSG_SIZE = 250;
    private static final int UNIQUE_ID_SIZE = 16;
    private static final int SECRET_CODE_LEN_SIZE = 4;
    // default timeout of 100ms
    private static final int TIMEOUT = 100;
    private static final int MAX_RETRIES = 3;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] generateRequest(int snum, byte[] uniqueID) throws NoSuchAlgorithmException {
        // Maximum payload size 16KB
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_MSG_SIZE);
        byteBuffer.limit(MAX_MSG_SIZE);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // Request message format: the first 16 bytes are the request’s unique ID,
        // the rest is the application level payload.
        byteBuffer.put(uniqueID);
        String requestHexString = bytesToHex(byteBuffer.array());
        System.out.println("Request: " + requestHexString);
        // The client’s payload (student id) is an integer (4 bytes), in little-endian format.
        // 8 digit student number, each hex digit is 4 bits, 4*8 = 32 bits = 4 bytes
        byteBuffer.putInt(snum);
        System.out.println("Position: " + byteBuffer.position());
        System.out.println("Remaining: " + byteBuffer.remaining());
        requestHexString = bytesToHex(byteBuffer.array());
        System.out.println("Request: " + requestHexString);
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

    public static byte[] sendRequest(String ip, int port, int snum) throws Exception {
        DatagramSocket socket = new DatagramSocket(port);
        byte[] uniqueID = generateUniqueID();
        byte[] req = generateRequest(snum, uniqueID);

        String hexString = bytesToHex(req);
        System.out.println("Request HEX String: " + hexString);

        InetAddress address = InetAddress.getByName(ip);
        System.out.println("Sending ID: " + snum);

        for (int i = 0, timeoutMs = TIMEOUT; i <= MAX_RETRIES; i++, timeoutMs*=2) {
            socket.setSoTimeout(timeoutMs);
            // send request
            System.out.println("Sending packet");
            DatagramPacket packet = new DatagramPacket(req, req.length, address, port);
            socket.send(packet);

            /*
            First 16 bytes are the unique ID of the corresponding request, rest is the application level
            payload. Maximum payload size 16KB.
            Payload has format: Secret code length (integer, 4 bytes, big-endian),
            Secret code (byte array), Possible padding (to be ignored)
            */

            try {
                socket.receive(packet);
            } catch(SocketTimeoutException e) {
                System.out.format("Exceeded timeout of %d ms, retrying...\n", timeoutMs);
                continue;
            }

            System.out.println("Received packet");
            System.out.println(bytesToHex(packet.getData()));

            // check matching uniqueID
            byte[] res = packet.getData();
            if (!Arrays.equals(Arrays.copyOf(res, UNIQUE_ID_SIZE), uniqueID)) {
                System.out.println("uniqueID not matching");
                throw new Exception("Mismatched uniqueID detected between request and response.");
            }
            // TODO: Break this parsing logic into separate function or class
            // get secret code's length
            ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_MSG_SIZE);
            byteBuffer.limit(MAX_MSG_SIZE);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            byteBuffer.put(res);
            int secretCodeLength = byteBuffer.getInt(UNIQUE_ID_SIZE - 1);
            System.out.println("Secret code length: " + secretCodeLength);

            // get secret code
            byteBuffer = ByteBuffer.allocate(MAX_MSG_SIZE);
            byteBuffer.limit(MAX_MSG_SIZE);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.put(res);

            byte[] secretCode = new byte[secretCodeLength];
            byteBuffer.get(secretCode, SECRET_CODE_LEN_SIZE, MAX_MSG_SIZE - SECRET_CODE_LEN_SIZE);
            String secretCodeHexString = bytesToHex(secretCode);
            System.out.println("Secret: " + secretCodeHexString);
            socket.close();
            return res;
        }

        socket.close();
        throw new Exception("Failed to receive message after max retries attempted.");
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
