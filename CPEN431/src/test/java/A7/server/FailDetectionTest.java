package A7.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static A7.DistributedSystemConfiguration.REP_FACTOR;
import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import A7.core.ConsistentHashRing;
import A7.core.NodesList;
import A7.utils.MsgWrapper;
import A7.utils.UniqueIdentifier;
import A7.server.GossipSenderThread;
public class FailDetectionTest {
    ConsistentHashRing hashRing;
    NodesList nodesList;
    Map<String, Integer> allNodes;
    Map<InetAddress, Integer> liveNodes;

    @org.junit.Before
    public void setUp() throws Exception {
        nodesList = NodesList.getInstance();
        liveNodes = new HashMap<>();
        liveNodes.put(InetAddress.getByName("142.103.2.2"), 1);
        liveNodes.put(InetAddress.getByName("129.97.74.12"), 2);
        liveNodes.put(InetAddress.getByName("128.208.4.197"), 6);
        liveNodes.put(InetAddress.getByName("128.208.4.99"), 8);
        liveNodes.put(InetAddress.getByName("84.88.58.155"), 1);
        liveNodes.put(InetAddress.getByName("128.208.4.50"), 1);
        liveNodes.put(InetAddress.getByName("128.208.4.70"), 1);
//        liveNodes.put("128.208.4.101:11300", 11300);

        allNodes = new HashMap<>();
        // contained in live nodes
        allNodes.put("142.103.2.2:10500", 10500);
        allNodes.put("129.97.74.12:10600", 10600);
        allNodes.put("141.212.113.178:10700", 10700);
        allNodes.put("128.208.4.197:10800", 10800);
        allNodes.put("128.208.4.99:10900", 10900);
        allNodes.put("84.88.58.155:11000", 11000);
        allNodes.put("128.208.4.50:11100", 11100);
        allNodes.put("128.208.4.70:11200", 11200);
        // not contained in live nodes
        allNodes.put("128.208.4.101:11300", 11300);
        nodesList.setAllNodes(allNodes);
        nodesList.setLiveNodes(liveNodes);
        hashRing = ConsistentHashRing.getInstance();
    }
    
    @org.junit.Test
    public void successorsDuplicateTest() throws NoSuchAlgorithmException, SocketException, UnknownHostException {
        String downedNode = UniqueIdentifier.MD5Hash("128.208.4.101:11300");
        Entry<String, MsgWrapper> entry = hashRing.getInstance().getHashRing().lowerEntry(downedNode);
        if (entry == null) {
            entry = hashRing.getInstance().getHashRing().lastEntry();
        }
        String currentNodeHash = UniqueIdentifier.MD5Hash(
                entry.getValue().getAddress().getHostAddress()
                + ":" + entry.getValue().getPort());
        MsgWrapper succTarget = GossipSenderThread.successorsDuplicate(currentNodeHash);
        Entry<String, MsgWrapper> checkEntry = hashRing.getInstance().getHashRing().higherEntry(downedNode);

        assertEquals(succTarget.getAddress().getHostName(), checkEntry.getValue().getAddress().getHostAddress());
        assertEquals(succTarget.getPort(), checkEntry.getValue().getPort());
    }

    @org.junit.Test
    public void predessorsDuplicateTest() throws NoSuchAlgorithmException, SocketException, UnknownHostException {
        String downedNode = UniqueIdentifier.MD5Hash("128.208.4.101:11300");
        Entry<String, MsgWrapper> entry = hashRing.getInstance().getHashRing().higherEntry(downedNode);
        if (entry == null) {
            entry = hashRing.getInstance().getHashRing().firstEntry();
        }
        String currentNodeHash = UniqueIdentifier.MD5Hash(
                entry.getValue().getAddress().getHostAddress()
                + ":" + entry.getValue().getPort());
        MsgWrapper[] pred = GossipSenderThread.predessorsDuplicate(currentNodeHash);

        // By logic, the node to duplicate onto is REP_FACTOR - 1 away
        Entry<String, MsgWrapper> checkEntry = hashRing.getInstance().getHashRing().higherEntry(downedNode);
        for (int skip = 0; skip < REP_FACTOR-1; skip++) {
            checkEntry = hashRing.getInstance().getHashRing().higherEntry(checkEntry.getKey());
            if (checkEntry == null) {
                checkEntry = hashRing.getInstance().getHashRing().firstEntry();
            }
        }
        //Should only duplicate to 1, only one node down
        assertEquals(pred.length, 1);
        assertEquals(pred[0].getAddress(), checkEntry.getValue().getAddress());
        assertEquals(pred[0].getPort(), checkEntry.getValue().getPort());
    }

    @org.junit.Test
    public void predessorsDuplicateTest2() throws NoSuchAlgorithmException, SocketException, UnknownHostException {
        String downedNode = UniqueIdentifier.MD5Hash("128.208.4.101:11300");
        // Remove second in line of dead node to get 2 in a row dead nodes
        Entry<String, MsgWrapper> entry = hashRing.getInstance().getHashRing().higherEntry(downedNode);
        if (entry == null) {
            entry = hashRing.getInstance().getHashRing().firstEntry();
        }

        liveNodes.remove(entry.getValue().getAddress());
        assertEquals(liveNodes.size(), 6);

        // CurrentNode is one more down from this one
        entry = hashRing.getInstance().getHashRing().higherEntry(entry.getKey());
        String currentNodeHash = UniqueIdentifier.MD5Hash(
                entry.getValue().getAddress().getHostAddress()
                + ":" + entry.getValue().getPort());
        MsgWrapper[] pred = GossipSenderThread.predessorsDuplicate(currentNodeHash);

        // By logic, the node to duplicate onto is REP_FACTOR - 1 away
        // As well as REP_FACTOR -1 -1
        Entry<String, MsgWrapper> checkEntry = hashRing.getInstance().getHashRing().higherEntry(currentNodeHash);
        for (int skip = pred.length; skip < REP_FACTOR-1; skip++) {
            checkEntry = hashRing.getInstance().getHashRing().higherEntry(checkEntry.getKey());
            if (checkEntry == null) {
                checkEntry = hashRing.getInstance().getHashRing().firstEntry();
            }
        }
        // Next pred.length (including current) nodes that are alive by logic
        for (int i = 0; i< pred.length; i++) {
            while (!liveNodes.containsKey(checkEntry.getValue().getAddress())) {
                checkEntry = hashRing.getInstance().getHashRing().higherEntry(checkEntry.getKey());
                if (checkEntry == null) {
                    checkEntry = hashRing.getInstance().getHashRing().firstEntry();
                }
            }
            assertEquals(pred[i].getAddress(), checkEntry.getValue().getAddress());
            assertEquals(pred[i].getPort(), checkEntry.getValue().getPort());
            checkEntry = hashRing.getInstance().getHashRing().higherEntry(checkEntry.getKey());
            if (checkEntry == null) {
                checkEntry = hashRing.getInstance().getHashRing().firstEntry();
            }

        }
        //Should only duplicate to 2, since max duplicates is 2 (as well 2 nodes down)
        assertEquals(pred.length, 2);
    }
}
