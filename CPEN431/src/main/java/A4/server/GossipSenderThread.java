package A4.server;

import static A4.DistributedSystemConfiguration.DEBUG;
import static A4.DistributedSystemConfiguration.VERBOSE;

import A4.proto.LiveHostsRequest.LiveHostsReq;
import A4.utils.ByteRepresentation;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class GossipSenderThread extends Thread {
    NodesList nodesList = NodesList.getInstance();
    private DatagramSocket socket;
    private int gossipSenderPort;

    public GossipSenderThread(String name, String filename) throws FileNotFoundException,
        SocketException, UnknownHostException {
        super(name);
        gossipSenderPort = UDPServerThread.localPort + 2;

        Map<InetAddress, Integer> liveNodes = new HashMap<>();
        Map<InetAddress, Integer> allNodes = new HashMap<>();

        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        // Populate all nodes list (excluding itself)
        while (scanner.hasNext()) {
            String[] node = scanner.next().split(":");
            int port = Integer.parseInt(node[1]);
            if (!node[0].equals(UDPServerThread.localAddress.getHostAddress())
                && port != UDPServerThread.localPort) {
                allNodes.put(InetAddress.getByName(node[0]), port);
            }
        }

        nodesList.setAllNodes(allNodes);
        // Add itself to live hosts list
        nodesList.setLiveNodes(liveNodes);
        nodesList.addLiveNode(UDPServerThread.localAddress, 0);

        socket = new DatagramSocket(gossipSenderPort);
    }

    public void run() {
        while (true) {
            if (VERBOSE && DEBUG) {
                Map<InetAddress, Integer> liveNodes = nodesList.getLiveNodes();
                System.out.println("NODES LIST");
                System.out.println("==========");
                for (Iterator<Entry<InetAddress, Integer>> iter = liveNodes.entrySet().iterator();
                    iter.hasNext();) {
                    Map.Entry<InetAddress, Integer> entry = iter.next();
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
            }

            Map.Entry<InetAddress, Integer> firstNode, secondNode;

            Random rand = new Random();
            Object[] allNodes = nodesList.getAllNodes().entrySet().toArray();

            // Reach out to two random nodes
            firstNode = (Map.Entry<InetAddress, Integer>) allNodes[rand.nextInt(allNodes.length)];
            secondNode = (Map.Entry<InetAddress, Integer>) allNodes[rand.nextInt(allNodes.length)];

            // Increment hops
            nodesList.refreshLiveNodes();

            // Build liveHostsReq protobuf
            byte[] serverList = ByteRepresentation.mapToBytes(nodesList.getLiveNodes());
            LiveHostsReq liveHostsReq = LiveHostsReq.newBuilder()
                    .setLiveHosts(ByteString.copyFrom(serverList))
                    .build();

            // gossip receiver thread port is port offset by +1
            DatagramPacket firstPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, firstNode.getKey(), firstNode.getValue() + 1);
            DatagramPacket secondPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, secondNode.getKey(), secondNode.getValue() + 1);

            try {
                socket.send(firstPacket);
                socket.send(secondPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Sleep so gossiping only performed every half second
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
