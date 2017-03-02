package A4.server;

import static A4.DistributedSystemConfiguration.UDP_SERVER_THREAD_PORT;
import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A4.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A4.DistributedSystemConfiguration.VERBOSE;
import static A4.utils.Checksum.calculateProtocolBufferChecksum;

import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class UDPServerThread extends Thread {
    public static InetAddress localAddress;
    public static int localPort;
    private static final int DEFAULT_UDP_SERVER_PORT = 10129;
    private DatagramSocket socket;

    public UDPServerThread() throws IOException {
        this("UDPServerThread");
    }

    public UDPServerThread(String name) throws IOException {
        super();
        socket = new DatagramSocket(UDP_SERVER_THREAD_PORT);
        this.localAddress = InetAddress.getLocalHost();
        this.localPort = UDP_SERVER_THREAD_PORT;
    }

    public void run() {
        while (true) {
            if (SHUTDOWN_NODE) {
                socket.close();
                System.exit(0);
            }
            // TODO: break this monolithic run function into smaller functions
            byte[] buf = new byte[MAX_MSG_SIZE];
            
            // receive request
            DatagramPacket reqPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(reqPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // deserialize request into Msg
            Msg requestMsg = null;
            try {
                requestMsg = Msg.parseFrom(
                    Arrays.copyOf(reqPacket.getData(), reqPacket.getLength()));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

            byte[] reply = null;
            byte[] messageID = requestMsg.getMessageID().toByteArray();

            if (VERBOSE) {
                System.out
                    .println("Available Memory (bytes): " + Runtime.getRuntime().freeMemory());
            }

            byte[] payload = requestMsg.getPayload().toByteArray();

            // verify checksum
            if (requestMsg != null) {
                if (requestMsg.getCheckSum() != calculateProtocolBufferChecksum(messageID,
                    payload)) {
                    System.out.format("Invalid checksum detected in the response, retrying...\n");
                    // TODO: Return self-defined error code
                    return;
                }
            }

            RequestQueue.getInstance().getQueue().add(
                new MsgWrapper(requestMsg, reqPacket.getAddress(), reqPacket.getPort()));
        }
    }
}
