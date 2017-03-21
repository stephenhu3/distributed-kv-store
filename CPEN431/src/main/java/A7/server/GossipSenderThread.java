package A7.server;

import static A7.DistributedSystemConfiguration.VERBOSE;

import A7.core.NodesList;
import A7.proto.LiveHostsRequest.LiveHostsReq;
import A7.utils.ByteRepresentation;
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
import java.util.concurrent.ConcurrentHashMap;

public class GossipSenderThread extends Thread {
    NodesList nodesList = NodesList.getInstance();
    private DatagramSocket socket;
    private int gossipSenderPort;

    public GossipSenderThread(String name, String filename, int port) throws FileNotFoundException,
        SocketException, UnknownHostException {
        gossipSenderPort = port + 2;

        Map<InetAddress, Integer> liveNodes = new ConcurrentHashMap<>();
        Map<String, Integer> allNodes = new HashMap<>();

        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        // Populate all nodes list (including itself)
        while (scanner.hasNext()) {
            String address = scanner.next();
            String[] node = address.split(":");
            allNodes.put(address, Integer.parseInt(node[1]));
        }

        nodesList.setAllNodes(allNodes);
        // Add itself to live hosts list
        nodesList.setLiveNodes(liveNodes);
        nodesList.addLiveNode(UDPServerThread.localAddress, 0);

        socket = new DatagramSocket(gossipSenderPort);
    }

    public void run() {
        while (true) {
            if (VERBOSE > 1) {
                Map<InetAddress, Integer> liveNodes = nodesList.getLiveNodes();
                System.out.println("NODES LIST");
                System.out.println("==========");
                for (Iterator<Entry<InetAddress, Integer>> iter = liveNodes.entrySet().iterator();
                    iter.hasNext();) {
                    Map.Entry<InetAddress, Integer> entry = iter.next();
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
            }

            Map.Entry<String, Integer> firstNode, secondNode;
            String[] firstAddress, secondAddress;

            Random rand = new Random();
            Object[] allNodes = nodesList.getAllNodes().entrySet().toArray();

            // Reach out to two random nodes
            firstNode = (Map.Entry<String, Integer>) allNodes[rand.nextInt(allNodes.length)];
            firstAddress = firstNode.getKey().split(":");

            secondNode = (Map.Entry<String, Integer>) allNodes[rand.nextInt(allNodes.length)];
            secondAddress = secondNode.getKey().split(":");

            // Increment hops
            nodesList.refreshLiveNodes();

            // Build liveHostsReq protobuf
            byte[] serverList = ByteRepresentation.mapToBytes(nodesList.getLiveNodes());
            LiveHostsReq liveHostsReq = LiveHostsReq.newBuilder()
                    .setLiveHosts(ByteString.copyFrom(serverList))
                    .build();

            // gossip receiver thread port is port offset by +1
            DatagramPacket firstPacket = null;
            DatagramPacket secondPacket = null;
            try {
                firstPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                    liveHostsReq.toByteArray().length, InetAddress.getByName(firstAddress[0]),
                    firstNode.getValue() + 1);
                secondPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                    liveHostsReq.toByteArray().length, InetAddress.getByName(secondAddress[0]),
                    secondNode.getValue() + 1);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

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
