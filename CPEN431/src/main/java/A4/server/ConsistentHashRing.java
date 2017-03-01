package A4.server;

import A4.utils.MsgWrapper;
import A4.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentSkipListMap;


public class ConsistentHashRing {
    private static ConsistentHashRing instance = new ConsistentHashRing();
    
    private final ConcurrentSkipListMap<String, MsgWrapper> hashRing;
    
    private ConsistentHashRing() {
    	hashRing = new ConcurrentSkipListMap<String, MsgWrapper>();
    }

    public static ConsistentHashRing getInstance() {
        return instance;
    }
    
    // Add a server and port to the hash ring
    // TODO: Handle redistributing content on node addition
    public void addNode(String name, int port) throws NoSuchAlgorithmException{
    	String hashKey = UniqueIdentifier.MD5Hash(name);
		try {
			InetAddress address = InetAddress.getByAddress(name.getBytes());
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
    public MsgWrapper getNode(ByteString key) throws NoSuchAlgorithmException {
        if (hashRing.isEmpty() || key == null) {
            return null;
        }

        String hashKey = UniqueIdentifier.MD5Hash(key.toString());
        // If key not contained in hash ring, use successor node (use first node if no successor)
        if (!hashRing.containsKey(hashKey)) {
            hashKey = hashRing.ceilingKey(hashKey);
            if (hashKey == null) {
                hashKey = hashRing.firstKey();
            }
        } else if (hashRing.get(hashKey).getAddress().equals(UDPServerThread.localAddress)
            && hashRing.get(hashKey).getPort() == UDPServerThread.localPort) {
            // Command applies to current node
        	return null;
        }
        return hashRing.get(hashKey);
  	}
}
