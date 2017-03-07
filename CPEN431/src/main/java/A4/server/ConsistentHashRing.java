package A4.server;

import A4.utils.MsgWrapper;
import A4.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConsistentHashRing {
    NodesList nodesList;

    private static ConsistentHashRing instance = new ConsistentHashRing();
    
    private final ConcurrentSkipListMap<String, MsgWrapper> hashRing;
    
    private ConsistentHashRing() {
        hashRing = new ConcurrentSkipListMap<String, MsgWrapper>();
        nodesList = NodesList.getInstance();
        initializeNodes();
    }

    public static ConsistentHashRing getInstance() {
        return instance;
    }
    
    private void initializeNodes() {
        try {
            for (Iterator<Map.Entry<InetAddress, Integer>> iter
                = nodesList.getAllNodes().entrySet().iterator(); iter.hasNext();) {
                Map.Entry<InetAddress, Integer> node = iter.next();
                addNode(node.getKey().getHostAddress(), node.getValue());
            }
            // add itself to ring
            addNode(InetAddress.getLocalHost().getHostAddress(), UDPServerThread.localPort);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // Add a server and port to the hash ring
    // TODO: Handle redistributing content on node addition
    public void addNode(String ip, int port) throws NoSuchAlgorithmException {
        String hashKey = UniqueIdentifier.MD5Hash(ip);
        try {
            InetAddress address = InetAddress.getByName(ip);
            hashRing.put(hashKey, new MsgWrapper(null, address, port));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    // Remove a server and port from the hash ring
    // TODO: Handle redistributing content on node removal
    public void removeNode(String nodeAddress) throws NoSuchAlgorithmException {
        String key = UniqueIdentifier.MD5Hash(nodeAddress);
        hashRing.remove(key);
    }

    // Determines the node and port that the key resides on
    // Result passed into forwarding queue, dequeued in RequestsCache to build response
    // Function returns msgWrapper with correct address and port that to send response
    // Function returns an empty MsgWrapper if current machine can service response
    public MsgWrapper getNode(ByteString key) throws NoSuchAlgorithmException {
        if (hashRing.isEmpty() || key.isEmpty()) {
            return new MsgWrapper(null, null, 0);
        }
        String hashKey = UniqueIdentifier.MD5Hash(key.toStringUtf8());
        // If key not contained in hash ring, use successor node (use first node if no successor)
        // TODO: Improve readability
        // TODO: Implement logic for handling when full loop around hash ring is made
        // !NodesList.getInstance().getLiveNodes().containsKey(hashRing.get(hashKey).getAddress())
        if (!hashRing.containsKey(hashKey)) {
            MsgWrapper target = null;
            while (target == null) {
//                hashKey = hashRing.ceilingKey(hashKey);
//                if (hashKey == null) {
//                    hashKey = hashRing.firstKey();
//                }
                SortedMap<String, MsgWrapper> tailMap = hashRing.tailMap(hashKey);
                hashKey = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();

                target = hashRing.get(hashKey);
                if (!NodesList.getInstance().getLiveNodes().containsKey(target.getAddress())) {
                    target = null;
                }
            }
            if (target.getAddress().equals(UDPServerThread.localAddress)
                    && target.getPort() == UDPServerThread.localPort) {
                // Command applies to current node
                return new MsgWrapper(null, null, 0);
            }
        }
        return hashRing.get(hashKey);
    }

    public ConcurrentSkipListMap<String, MsgWrapper> getHashRing() {
        return this.hashRing;
    }
}
