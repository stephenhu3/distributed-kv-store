package A4.server;

import static A4.DistributedSystemConfiguration.DEBUG;
import static A4.DistributedSystemConfiguration.GOSSIP_RECEIVER_PORT;
import static A4.DistributedSystemConfiguration.GOSSIP_SENDER_PORT;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class GossipSenderThread extends Thread {
    NodesList nodesList = NodesList.getInstance();
    private DatagramSocket socket;

    public GossipSenderThread(String name, String filename) throws FileNotFoundException,
        SocketException {
        super(name);
        Map<InetAddress, Integer> liveNodes = new HashMap<>();
        List<String> allNodes = new ArrayList<String>();

        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        // Populate all nodes list (excluding itself)
        while (scanner.hasNext()) {
            String ip = scanner.nextLine();
            if (!ip.equals(UDPServerThread.localAddress.getHostAddress())) {
                allNodes.add(ip);
            }
        }

        nodesList.setAllNodes(allNodes);
        // Add itself to live hosts list
        nodesList.setLiveNodes(liveNodes);
        nodesList.addLiveNode(UDPServerThread.localAddress, 0);

        socket = new DatagramSocket(GOSSIP_SENDER_PORT);
    }

    public void run() {
        while (true) {
            if (VERBOSE && DEBUG) {
                Map<InetAddress, Integer> liveNodes = NodesList.getInstance().getLiveNodes();
                System.out.println("NODES LIST");
                System.out.println("==========");
                for (Iterator<Entry<InetAddress, Integer>> it = liveNodes.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<InetAddress, Integer> entry = it.next();
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
            }

            InetAddress firstAddress, secondAddress;
            Random rand = new Random();
            List<String> allNodes = NodesList.getInstance().getAllNodes();

            // Reach out to two random nodes
            String firstHost = allNodes.get(rand.nextInt(allNodes.size()));
            String secondHost = allNodes.get(rand.nextInt(allNodes.size()));

            // Increment hops
            NodesList.getInstance().refreshLiveNodes();

            // Build liveHostsReq protobuf
            byte[] serverList = ByteRepresentation.mapToBytes(nodesList.getLiveNodes());
            LiveHostsReq liveHostsReq = LiveHostsReq.newBuilder()
                    .setLiveHosts(ByteString.copyFrom(serverList))
                    .build();
            try {
                firstAddress = InetAddress.getByName(firstHost);
                secondAddress = InetAddress.getByName(secondHost);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            DatagramPacket firstPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, firstAddress, GOSSIP_RECEIVER_PORT);
            DatagramPacket secondPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, secondAddress, GOSSIP_RECEIVER_PORT);

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
