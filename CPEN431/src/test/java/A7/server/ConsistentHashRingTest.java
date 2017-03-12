package A7.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import A7.utils.MsgWrapper;
import A7.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ConsistentHashRingTest {
    ConsistentHashRing hashRing;
    NodesList nodesList;

    @org.junit.Before
    public void setUp() throws Exception {
        nodesList = NodesList.getInstance();
        Map<InetAddress, Integer> liveNodes = new HashMap<>();
        liveNodes.put(InetAddress.getByName("142.103.2.2"), 1);
        liveNodes.put(InetAddress.getByName("129.97.74.12"), 2);
        liveNodes.put(InetAddress.getByName("128.208.4.197"), 6);
        liveNodes.put(InetAddress.getByName("128.208.4.99"), 8);

        Map<String, Integer> allNodes = new HashMap<>();
        // contained in live nodes
        allNodes.put("142.103.2.2:10500", 10500);
        allNodes.put("129.97.74.12:10600", 10600);
        allNodes.put("141.212.113.178:10700", 10700);
        allNodes.put("128.208.4.197:10800", 10800);
        allNodes.put("128.208.4.99:10900", 10900);
        // not contained in live nodes
        allNodes.put("84.88.58.155:11000", 11000);
        allNodes.put("128.208.4.50:11100", 11100);
        allNodes.put("128.208.4.70:11200", 11200);
        allNodes.put("128.208.4.101:11300", 11300);

        nodesList.setAllNodes(allNodes);
        nodesList.setLiveNodes(liveNodes);
        hashRing = ConsistentHashRing.getInstance();
    }

    @org.junit.Test
    public void testAddNode() throws NoSuchAlgorithmException, UnknownHostException {
        String ip = "198.133.224.147";
        int port = 10800;
        String hashKey = UniqueIdentifier.MD5Hash(ip + ":" + port);
        hashRing.addNode(ip, port);

        MsgWrapper expectedValue = new MsgWrapper(null, InetAddress.getByName(ip), port);
        MsgWrapper actualValue = hashRing.getHashRing().get(hashKey);

        assertEquals(expectedValue, actualValue);
        // teardown
        hashRing.removeNode(ip, port);
    }

    @org.junit.Test
    public void testRemoveNode() throws NoSuchAlgorithmException {
        String ip = "128.153.241.117";
        int port = 10800;
        String hashKey = UniqueIdentifier.MD5Hash(ip + ":" + port);

        hashRing.addNode(ip, port);
        assertNotNull(hashRing.getHashRing().get(hashKey));

        hashRing.removeNode(ip, port);
        assertNull(hashRing.getHashRing().get(hashKey));
    }

    @org.junit.Test
    public void testGetNodeEmptyKey() throws NoSuchAlgorithmException {
        MsgWrapper actualValue = hashRing.getNode(ByteString.EMPTY);
        MsgWrapper expectedValue = new MsgWrapper(null, null, 0);
        assertEquals(actualValue, expectedValue);
    }

    @org.junit.Test
    public void testGetNodeFirstSuccessor() throws NoSuchAlgorithmException {
        String testKey = "141.212.113.178:10700";
        String expectedKey = "128.208.4.99:10900";
        MsgWrapper actualValue = hashRing.getNode(ByteString.copyFromUtf8(testKey));
        MsgWrapper expectedValue = hashRing.getNode(ByteString.copyFromUtf8(expectedKey));
        assertEquals(actualValue, expectedValue);
    }

    @org.junit.Test
    public void testGetNodeSecondSuccessor() throws NoSuchAlgorithmException {
        String testKey = "128.208.4.70:11200";
        String expectedKey = "128.208.4.99:10900";
        MsgWrapper actualValue = hashRing.getNode(ByteString.copyFromUtf8(testKey));
        MsgWrapper expectedValue = hashRing.getNode(ByteString.copyFromUtf8(expectedKey));
        assertEquals(actualValue, expectedValue);
    }

    @org.junit.Test
    public void testGetNodeFirstKey() throws NoSuchAlgorithmException {
        String testKey = "129.97.74.12:10600";
        MsgWrapper actualValue = hashRing.getNode(ByteString.copyFromUtf8(testKey));
        MsgWrapper expectedValue = hashRing.getNode(ByteString.copyFromUtf8(testKey));
        assertEquals(actualValue, expectedValue);
    }
}
