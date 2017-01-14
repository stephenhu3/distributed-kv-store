package A2.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import A2.proto.Message.Msg;

import static A2.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;
import static A2.DistributedSystemConfiguration.VERBOSE;
import static A2.utils.ByteRepresentation.bytesToHex;

public class UDPClient {
    private static final int TIMEOUT = 100; // default timeout of 100ms
    private static final int MAX_RETRIES = 3;
    private static final int RES_UDP_SIZE = 36; // response size is 36 bytes

    public static byte[] sendRawBytesRequest(byte[] req, String ip, int port, byte[] uniqueID)
            throws Exception {
        DatagramSocket socket = new DatagramSocket(port);
        InetAddress address = InetAddress.getByName(ip);

        byte[] res = new byte[RES_UDP_SIZE];
        DatagramPacket reqPacket = new DatagramPacket(req, req.length, address, port);
        DatagramPacket resPacket = new DatagramPacket(res, res.length, address, port);

        for (int i = 0, timeoutMs = TIMEOUT; i <= MAX_RETRIES; i++, timeoutMs*=2) {
            socket.setSoTimeout(timeoutMs);
            if (VERBOSE) {
                System.out.println("Sending packet...");
            }
            // send request
            socket.send(reqPacket);

            try {
                socket.receive(resPacket);
            } catch(SocketTimeoutException e) {
                if (VERBOSE) {
                    System.out.format("Exceeded timeout of %d ms, retrying...\n", timeoutMs);
                }
                continue;
            }

            res = resPacket.getData();

            if (VERBOSE) {
                System.out.println("Received packet");
                System.out.println(bytesToHex(res));
            }

  	        // check matching uniqueID
            if (!Arrays.equals(Arrays.copyOf(res, UNIQUE_ID_UDP_SIZE), uniqueID)) {
                if (VERBOSE) {
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

    public static byte[] sendProtocolBufferRequest(Msg msg, String ip, int port, byte[] messageID,
                                                   long checksum) throws Exception {
        DatagramSocket socket = new DatagramSocket(port);
        InetAddress address = InetAddress.getByName(ip);

        byte[] req = msg.toByteArray();
        // allocate response with 1kB, truncate when number of byte received is known
        byte[] res = new byte[1024];

        DatagramPacket reqPacket = new DatagramPacket(req, req.length, address, port);
        DatagramPacket resPacket = new DatagramPacket(res, res.length, address, port);
        Msg responseMsg = null;

        for (int i = 0, timeoutMs = TIMEOUT; i <= MAX_RETRIES; i++, timeoutMs*=2) {
            socket.setSoTimeout(timeoutMs);
            if (VERBOSE) {
                System.out.println("Sending packet...");
            }
            // send request
            socket.send(reqPacket);

            /*
            TODO: Sending has no exception, only when receiving
            com.google.protobuf.InvalidProtocolBufferException: While parsing a protocol message,
            the input ended unexpectedly in the middle of a field.  This could mean either that the
            input has been truncated or that an embedded message misreported its own length.
             */

            try {
                socket.receive(resPacket);
            } catch(SocketTimeoutException e) {
                if (VERBOSE) {
                    System.out.format("Exceeded timeout of %d ms, retrying...\n", timeoutMs);
                }
                continue;
            }

            res = reqPacket.getData();
            // deserialize from response byte array exactly as large as number of bytes received
            responseMsg = Msg.parseFrom(Arrays.copyOf(res, reqPacket.getLength()));

            if (VERBOSE) {
                System.out.println("Received packet");
                System.out.println(bytesToHex(res));
                System.out.println("MessageID:" + bytesToHex(responseMsg.getMessageID().toByteArray()));
                System.out.println("Payload:" + bytesToHex(responseMsg.getPayload().toByteArray()));
                System.out.println("Checksum:" + responseMsg.getCheckSum());
            }

            // check matching messageID
            if (!Arrays.equals(responseMsg.getMessageID().toByteArray(), messageID)) {
                if (VERBOSE) {
                    System.out.format("Mismatched uniqueID detected between request and response," +
                            "retrying...\n");
                }
                continue;
            }

            // verify checksum
            if (responseMsg.getCheckSum() != checksum) {
                if (VERBOSE) {
                    System.out.format("Mismatched checksum detected between request and response," +
                            "retrying...\n");
                }
                continue;
            }

            socket.close();
            // return payload
            return responseMsg.getPayload().toByteArray();
        }
        socket.close();
        throw new Exception("Failed to receive message after max retries attempted.");
    }
}
