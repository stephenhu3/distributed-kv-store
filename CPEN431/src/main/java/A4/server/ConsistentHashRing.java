package A4.server;

import static A4.DistributedSystemConfiguration.UDP_SERVER_THREAD_PORT;

import A4.utils.MsgWrapper;
import A4.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConsistentHashRing {
    private static ConsistentHashRing instance = new ConsistentHashRing();
    
    private final ConcurrentSkipListMap<String, MsgWrapper> hashRing;
    
    private ConsistentHashRing() {
        hashRing = new ConcurrentSkipListMap<String, MsgWrapper>();
        initializeNodes();
    }

    public static ConsistentHashRing getInstance() {
        return instance;
    }
    
    public void initializeNodes() {
        try {
            for (Iterator<String> i = NodesList.getInstance().getAllNodes().iterator();
                i.hasNext();) {
                String nodeAddress = i.next();
                addNode(nodeAddress, UDP_SERVER_THREAD_PORT);
            }
            // add itself to ring
            addNode(UDPServerThread.localAddress.getHostAddress(), UDP_SERVER_THREAD_PORT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Add a server and port to the hash ring
    // TODO: Handle redistributing content on node addition
    public void addNode(String name, int port) throws NoSuchAlgorithmException {
        String hashKey = UniqueIdentifier.MD5Hash(name);
        try {
            InetAddress address = InetAddress.getByName(name);
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
        if (!hashRing.containsKey(hashKey)) {
            MsgWrapper target = null;
            while (target == null) {
               hashKey = hashRing.ceilingKey(hashKey);
               if (hashKey == null) {
                   hashKey = hashRing.firstKey();
               }
                target = hashRing.get(hashKey);
                if (!NodesList.getInstance().getLiveNodes().containsKey(target.getAddress())) {
//                    hashRing.remove(hashKey);
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
}
