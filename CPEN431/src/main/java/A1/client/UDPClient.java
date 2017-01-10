package A1.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import static A1.DistributedSystemConfiguration.VERBOSE;
import static A1.utils.ByteRepresentation.bytesToHex;

public class UDPClient {
    private static final int TIMEOUT = 100; // default timeout of 100ms
    private static final int MAX_RETRIES = 3;

    public static byte[] sendRequest(byte[] req, String ip, int port) throws Exception {
        DatagramSocket socket = new DatagramSocket(port);
        InetAddress address = InetAddress.getByName(ip);

        for (int i = 0, timeoutMs = TIMEOUT; i <= MAX_RETRIES; i++, timeoutMs*=2) {
            socket.setSoTimeout(timeoutMs);
            if (VERBOSE) {
                System.out.println("Sending packet...");
            }
            // send request
            DatagramPacket packet = new DatagramPacket(req, req.length, address, port);
            socket.send(packet);

            try {
                socket.receive(packet);
            } catch(SocketTimeoutException e) {
                if (VERBOSE) {
                    System.out.format("Exceeded timeout of %d ms, retrying...\n", timeoutMs);
                }
                continue;
            }

            byte[] res = packet.getData();

            if (VERBOSE) {
                System.out.println("Received packet");
                System.out.println(bytesToHex(res));
            }

            socket.close();
            return res;
        }
        socket.close();
        throw new Exception("Failed to receive message after max retries attempted.");
    }
}
