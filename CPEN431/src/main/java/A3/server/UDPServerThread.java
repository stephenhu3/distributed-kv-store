package A3.server;

import static A3.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A3.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A3.DistributedSystemConfiguration.VERBOSE;
import static A3.utils.Checksum.calculateProtocolBufferChecksum;

import A3.proto.Message.Msg;
import A3.utils.MsgWrapper;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UDPServerThread extends Thread {
    private static final int DEFAULT_UDP_SERVER_PORT = 10129;
    private DatagramSocket socket;

    public UDPServerThread() throws IOException {
        this("UDPServerThread", DEFAULT_UDP_SERVER_PORT);
    }

    public UDPServerThread(String name, int port) throws IOException {
        super(name);
        socket = new DatagramSocket(port);
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
