package A4.server;

import static A4.DistributedSystemConfiguration.MAX_MSG_SIZE;

import A4.proto.LiveHostsRequest.LiveHostsReq;
import A4.utils.ByteRepresentation;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class GossipReceiverThread extends Thread {
    NodesList nodesList;
    private DatagramSocket socket;
    private int gossipReceiverPort;

    public GossipReceiverThread(String name) throws SocketException {
        super(name);
        gossipReceiverPort = UDPServerThread.localPort + 1;
        socket = new DatagramSocket(gossipReceiverPort);
        nodesList = NodesList.getInstance();
    }

    public int getPort() {
        return this.gossipReceiverPort;
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

            Map<InetAddress, Integer> liveNodes = ByteRepresentation.bytesToMap(
                liveHostsReq.getLiveHosts().toByteArray());

            // Add node if it's new or its hops number is lower
            if (liveNodes != null) {
                for (Iterator<Map.Entry<InetAddress, Integer>> it = liveNodes.entrySet().iterator();
                    it.hasNext(); ) {
                    Map.Entry<InetAddress, Integer> entry = it.next();
                    if (!nodesList.getLiveNodes().containsKey(entry.getKey())
                        || nodesList.getLiveNodes().get(entry.getKey()) > entry.getValue()) {
                        nodesList.addLiveNode(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }
}
