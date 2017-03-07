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
            if (!node[0].equals(UDPServerThread.localAddress.getHostAddress())) {
                allNodes.put(InetAddress.getByName(node[0]), Integer.parseInt(node[1]));
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
                for (Iterator<Entry<InetAddress, Integer>> it = liveNodes.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<InetAddress, Integer> entry = it.next();
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
            }

            InetAddress firstAddress, secondAddress;
            int firstPort, secondPort;

            Random rand = new Random();
            Object[] allNodes = nodesList.getAllNodes().values().toArray();

            // Reach out to two random nodes
            firstAddress = (InetAddress) allNodes[rand.nextInt(allNodes.length)];
            firstPort = nodesList.getAllNodes().get(firstAddress);

            secondAddress = (InetAddress) allNodes[rand.nextInt(allNodes.length)];
            secondPort = nodesList.getAllNodes().get(secondAddress);

            // Increment hops
            nodesList.refreshLiveNodes();

            // Build liveHostsReq protobuf
            byte[] serverList = ByteRepresentation.mapToBytes(nodesList.getLiveNodes());
            LiveHostsReq liveHostsReq = LiveHostsReq.newBuilder()
                    .setLiveHosts(ByteString.copyFrom(serverList))
                    .build();

            DatagramPacket firstPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, firstAddress, firstPort);
            DatagramPacket secondPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, secondAddress, secondPort);

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
