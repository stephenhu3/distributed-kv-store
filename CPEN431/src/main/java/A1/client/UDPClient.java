package A1.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import static A1.DistributedSystemConfiguration.UNIQUE_ID_UDP_SIZE;
import static A1.DistributedSystemConfiguration.VERBOSE;
import static A1.utils.ByteRepresentation.bytesToHex;

public class UDPClient {
    private static final int TIMEOUT = 100; // default timeout of 100ms
    private static final int MAX_RETRIES = 3;
    private static final int RES_UDP_SIZE = 36; // response size is 36 bytes

    public static byte[] sendRequest(byte[] req, String ip, int port, byte[] uniqueID) throws Exception {
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
            // TODO: Maybe getting FFFFFFF mismatched uniqueID because retry issue? move this above for loop
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
}
