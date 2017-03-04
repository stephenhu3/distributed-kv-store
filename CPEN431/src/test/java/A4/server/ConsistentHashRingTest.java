package A4.server;

import static A4.DistributedSystemConfiguration.UDP_SERVER_THREAD_PORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import A4.utils.MsgWrapper;
import A4.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        liveNodes.put(InetAddress.getByName("141.212.113.178"), 4);
        liveNodes.put(InetAddress.getByName("128.208.4.197"), 6);

        List<String> allNodes = new ArrayList<String>();
        // contained in live nodes
        allNodes.add("142.103.2.2");
        allNodes.add("129.97.74.12");
        allNodes.add("141.212.113.178");
        allNodes.add("128.208.4.197");
        // not contained in live nodes
        allNodes.add("84.88.58.155");
        allNodes.add("128.208.4.99");

        nodesList.setAllNodes(allNodes);
        nodesList.setLiveNodes(liveNodes);
        hashRing = ConsistentHashRing.getInstance();
    }

    @org.junit.Test
    public void testAddNode() throws NoSuchAlgorithmException, UnknownHostException {
        String ip = "198.133.224.147";
        String hashKey = UniqueIdentifier.MD5Hash(ip);
        hashRing.addNode(ip, UDP_SERVER_THREAD_PORT);

        MsgWrapper expectedValue = new MsgWrapper(null, InetAddress.getByName(ip),
            UDP_SERVER_THREAD_PORT);
        MsgWrapper actualValue = hashRing.getHashRing().get(hashKey);

        assertEquals(expectedValue, actualValue);
    }

    @org.junit.Test
    public void testRemoveNode() throws NoSuchAlgorithmException {
        String ip = "128.153.241.117";
        String hashKey = UniqueIdentifier.MD5Hash(ip);

        hashRing.addNode(ip, UDP_SERVER_THREAD_PORT);
        assertNotNull(hashRing.getHashRing().get(hashKey));

        hashRing.removeNode(ip);
        assertNull(hashRing.getHashRing().get(hashKey));
    }

    @org.junit.Test
    public void testGetNodeEmptyKey() throws NoSuchAlgorithmException {
        MsgWrapper actualValue = hashRing.getNode(ByteString.EMPTY);
        MsgWrapper expectedValue = new MsgWrapper(null, null, 0);
        assertEquals(actualValue, expectedValue);
    }

    @org.junit.Test
    public void testGetNodeFirstSuccessor() {
        // TODO
    }

    @org.junit.Test
    public void testGetNodeSecondSuccessor() {
        // TODO
    }

    @org.junit.Test
    public void testGetNodeFirstKey() {
        // TODO
    }
}
