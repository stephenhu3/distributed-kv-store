package A4.server;

import A4.utils.MsgWrapper;
import A4.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
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
    public void removeNode(String nodeAddress) throws NoSuchAlgorithmException {
    	String key = UniqueIdentifier.MD5Hash(nodeAddress);
        hashRing.remove(key);
  	}

    // Determines the node and port that the key resides on
    // Result passed into forwarding queue, dequeued in RequestsCache to build response
    public MsgWrapper getNode(ByteString key) throws NoSuchAlgorithmException {
        String hashKey = UniqueIdentifier.MD5Hash(key.toString());

        if (!hashRing.containsKey(hashKey)) {
            SortedMap<String, MsgWrapper> tailMap = hashRing.tailMap(hashKey);
            hashKey = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        }

		// Command applies to current node
    	if (hashRing.isEmpty() || key == null
            || (hashRing.get(hashKey).getAddress().equals(UDPServerThread.localAddress)
            && hashRing.get(hashKey).getPort() == UDPServerThread.localPort)) {
        	return null;
        }

        return hashRing.get(hashKey);
  	}
}
