package A4.server;

import static A4.DistributedSystemConfiguration.GOSSIP_RECEIVER_PORT;
import static A4.DistributedSystemConfiguration.MAX_MSG_SIZE;

import A4.proto.LiveHostsRequest.LiveHostsReq;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class GossipReceiverThread extends Thread {
    private DatagramSocket socket;

    public GossipReceiverThread(String name) throws SocketException {
        super(name);
        socket = new DatagramSocket(GOSSIP_RECEIVER_PORT);
    }

    public void run() {
        while (true) {
            // listen for incoming gossip packets
            byte[] buf = new byte[MAX_MSG_SIZE];

            // receive request
            DatagramPacket reqPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(reqPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // deserialize request into LiveHostsRequest
            LiveHostsReq liveHostsReq = null;
            try {
                liveHostsReq = LiveHostsReq.parseFrom(
                    Arrays.copyOf(reqPacket.getData(), reqPacket.getLength()));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

            // TODO: add node to current liveHosts, age existing entries

        }
    }
}
