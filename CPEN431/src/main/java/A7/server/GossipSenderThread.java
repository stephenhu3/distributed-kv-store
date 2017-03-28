package A7.server;

import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.DistributedSystemConfiguration.REP_FACTOR;

import A7.core.ConsistentHashRing;
import A7.core.NodesList;
import A7.proto.LiveHostsRequest.LiveHostsReq;
import A7.utils.ByteRepresentation;
import A7.utils.MsgWrapper;
import A7.utils.UniqueIdentifier;

import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
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
        nodesList.addLiveNode(UDPServerThreadPool.localAddress, 0);

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
            try {
                FailDetection();
            } catch (NoSuchAlgorithmException | SocketException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
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
    
    // Returns the ConsistentHashRing entry of node to duplicate KVStore on in event
    // of node's successor going down.
    protected static MsgWrapper successorsDuplicate(String currentNodeHash) {
        Entry<String, MsgWrapper> successor =
            ConsistentHashRing.getInstance().getHashRing().higherEntry(currentNodeHash);

        if (successor == null) {
            successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
        }

        // if first successor alive, no need to duplicate
        if (NodesList.getInstance().getLiveNodes()
                .containsKey(successor.getValue().getAddress())) {
            return new MsgWrapper(null, null, 0);
        }

        // if no successor found, ends up being itself, as own node belongs in liveNodes
        while (!NodesList.getInstance().getLiveNodes()
            .containsKey(successor.getValue().getAddress())) {
            // successor is down, must send own replication store to next available successor
            successor =
                ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
            // loop around to first entry if last node reached
            if (successor == null) {
                successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
            }
        }
        return successor.getValue();
    }
    
    // Check if predecessor is down and if down, keeps checking previous predecessor
    // Checks only up to REP_FACTOR - 1 predecessors because REP_FACTOR is inclusive
    // Current node will not have keys that is REP_FACTOR away
    protected static MsgWrapper[] predessorsDuplicate(String currentNodeHash) {
        int deadPred = 0;
        MsgWrapper[] dupeNodes = null;
        Entry<String, MsgWrapper> predecessor =
            ConsistentHashRing.getInstance().getHashRing().lowerEntry(currentNodeHash);

        if (predecessor == null) {
            predecessor = ConsistentHashRing.getInstance().getHashRing().lastEntry();
        }

        while (!NodesList.getInstance().getLiveNodes()
            .containsKey(predecessor.getValue().getAddress()) && deadPred < REP_FACTOR - 1) {
            // Predecessor is down, see its predecessor is down
            deadPred++;
            predecessor =
                ConsistentHashRing.getInstance().getHashRing().lowerEntry(predecessor.getKey());
            if (predecessor == null) {
                predecessor = ConsistentHashRing.getInstance().getHashRing().lastEntry();
            }
        }

        if (deadPred > 0) {
            // Navigate to first node that needs duplication if deadPred == 2, 1 node up from
            // current; if deadPread == 1, 2 nodes from current, etc.
            Entry<String, MsgWrapper> successor =
                ConsistentHashRing.getInstance().getHashRing().higherEntry(currentNodeHash);

            if (successor == null) {
                successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
            }

            // find next successor that's alive since first duplicated node
            for (int skip = deadPred; skip < REP_FACTOR - 1; skip++) {
                successor =
                    ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
                if (successor == null) {
                    successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
                }
            }

            dupeNodes = new MsgWrapper[deadPred];
            for (int i = 0; i < deadPred; i++) {
                // if current successor dead, find next live node
                while (!NodesList.getInstance().getLiveNodes()
                    .containsKey(successor.getValue().getAddress())) {
                    // no live successor found yet, check next
                    successor = ConsistentHashRing.getInstance().getHashRing()
                        .higherEntry(successor.getKey());
                    if (successor == null) {
                        successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
                    }
                }

                dupeNodes[i] = successor.getValue();
                successor = ConsistentHashRing.getInstance().getHashRing()
                    .higherEntry(successor.getKey());
                if(successor == null) {
                    successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
                }
            }
        }
        return dupeNodes;
    }
    
    // Finds which node has failed. Run duplication on discovered targets from detecting failure on
    // successor and predecessor nodes
    protected static void FailDetection() throws NoSuchAlgorithmException, SocketException {
        // Check if successor is down
        String currentNodeHash = UniqueIdentifier.MD5Hash(
                UDPServerThreadPool.localAddress.getHostAddress()
                + ":" + UDPServerThreadPool.localPort);
        MsgWrapper succTarget = successorsDuplicate(currentNodeHash);
        // send to successor, but don't send if that successor happens to be own node 
        // or if detected that the successor is not dead
        if (succTarget != null && succTarget.getPort() != 0 && succTarget.getAddress() != null
                && !succTarget.getAddress().equals(UDPServerThreadPool.localAddress)) {
            UDPServerThreadPool.executor.execute(new SendReplication(succTarget));
        }
        
        MsgWrapper[] predTargets = predessorsDuplicate(currentNodeHash);
        // Don't bother duplicating if no predecessors are dead
        if (predTargets != null && predTargets.length != 0) {
            // duplicate to each successor found, but don't send if that successor is own node
            for (int i = 0; i < predTargets.length; i++) {
                if (!predTargets[i].getAddress().equals(UDPServerThreadPool.localAddress)) {
                    UDPServerThreadPool.executor.execute(new SendReplication(predTargets[i]));
                }
            }
        }
    }
}
