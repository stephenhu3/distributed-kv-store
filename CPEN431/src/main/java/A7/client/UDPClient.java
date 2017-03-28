package A7.client;

import static A7.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A7.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;
import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.utils.ByteRepresentation.bytesToHex;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;

import A7.proto.Message.Msg;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class UDPClient {
    private static final int TIMEOUT = 100; // default timeout of 100ms
    private static final int MAX_RETRIES = 3;
    private static final int RES_UDP_SIZE = 36; // response size is 36 bytes

    public static byte[] sendRawBytesRequest(byte[] req, String ip, int port, byte[] uniqueID)
            throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(ip);

        byte[] res = new byte[RES_UDP_SIZE];
        DatagramPacket reqPacket = new DatagramPacket(req, req.length, address, port);
        DatagramPacket resPacket = new DatagramPacket(res, res.length, address, port);

        for (int i = 0, timeoutMs = TIMEOUT; i <= MAX_RETRIES; i++, timeoutMs*=2) {
            socket.setSoTimeout(timeoutMs);
            if (VERBOSE > 0) {
                System.out.println("Sending packet...");
            }
            // send request
            socket.send(reqPacket);

            try {
                socket.receive(resPacket);
            } catch(SocketTimeoutException e) {
                if (VERBOSE > 0) {
                    System.out.format("Exceeded timeout of %d ms, retrying...\n", timeoutMs);
                }
                continue;
            }

            res = resPacket.getData();

            if (VERBOSE > 0) {
                System.out.println("Received packet");
                System.out.println(bytesToHex(res));
            }

  	        // check matching uniqueID
            if (!Arrays.equals(Arrays.copyOf(res, UNIQUE_ID_UDP_SIZE), uniqueID)) {
                if (VERBOSE > 0) {
                    System.out.format("Mismatched uniqueID detected between request and response," +
                            "retrying...\n");
                }
                continue;
            }

            socket.close();
            return res;
        }
        socket.close();
        throw new Exception("Failed to receive message after max retries attempted.");
    }

    public static byte[] sendProtocolBufferRequest(byte[] msg, String ip, int port, byte[] messageID)
            throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(ip);

        // allocate response with max size of 16kB, truncate when number of byte received is known
        byte[] res = new byte[MAX_MSG_SIZE];

        DatagramPacket reqPacket = new DatagramPacket(msg, msg.length, address, port);
        DatagramPacket resPacket = new DatagramPacket(res, res.length, address, port);

        for (int i = 0, timeoutMs = TIMEOUT; i <= MAX_RETRIES; i++, timeoutMs*=2) {
            socket.setSoTimeout(timeoutMs);
            if (VERBOSE > 0) {
                System.out.println("Sending packet...");
            }
            // send request
            socket.send(reqPacket);

            try {
                socket.receive(resPacket);
            } catch(SocketTimeoutException e) {
                if (VERBOSE > 0) {
                    System.out.format("Exceeded timeout of %d ms, retrying...\n", timeoutMs);
                }
                continue;
            }

            res = resPacket.getData();
            // deserialize from response byte array exactly as large as number of bytes received
            Msg responseMsg = Msg.parseFrom(Arrays.copyOf(res, resPacket.getLength()));

            if (VERBOSE > 0) {
                System.out.println("Received packet: "
                        + bytesToHex(Arrays.copyOf(res, resPacket.getLength())));
                System.out.println("Received MessageID: "
                        + bytesToHex(responseMsg.getMessageID().toByteArray()));
                System.out.println("Received Payload: "
                        + bytesToHex(responseMsg.getPayload().toByteArray()));
                System.out.println("Received Checksum: " + responseMsg.getCheckSum());
            }

            // check matching messageID
            if (!Arrays.equals(responseMsg.getMessageID().toByteArray(), messageID)) {
                if (VERBOSE > 0) {
                    System.out.format("Mismatched messageID detected between request and response,"
                            + "retrying...\n");
                }
                continue;
            }

            // verify checksum
            if (responseMsg.getCheckSum() != calculateProtocolBufferChecksum(
                    responseMsg.getMessageID().toByteArray(),
                    responseMsg.getPayload().toByteArray())) {
                if (VERBOSE > 0) {
                    System.out.format("Invalid checksum detected in the response, retrying...\n");
                }
                continue;
            }

            socket.close();
            // return payload
            return responseMsg.getPayload().toByteArray();
        }
        socket.close();
        if (VERBOSE > 0) {
            System.out.println("Failed to receive message after max retries attempted.");
        }
        return null;
    }
}
