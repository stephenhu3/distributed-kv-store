package A4.server;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.protobuf.ByteString;

import A4.utils.MsgWrapper;


public class DistributedServerHash {
    private static DistributedServerHash instance = new DistributedServerHash();
    
    private final ConcurrentSkipListMap<String, MsgWrapper> circle;
    
    private DistributedServerHash(){
    	circle = new ConcurrentSkipListMap<String, MsgWrapper>();
    }

    public static DistributedServerHash getInstance() {
        return instance;
    }
    
    //Add a server and port to the has circle
    public void addNode(String name, int port) throws NoSuchAlgorithmException{
    	String hashKey = hashFunction(name);
		try {
			InetAddress address = InetAddress.getByAddress(name.getBytes());
	        circle.put(hashKey, new MsgWrapper(null, address, port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }
    
    //Remove a server and port to the has circle
    public void removeNode(String nodeAddress) throws NoSuchAlgorithmException {
    	String hashKey = hashFunction(nodeAddress);
        circle.remove(hashKey);
      }

    // Determines the node and port that the key resides on
    // Result passed into forwarding queue, dequeued in RequestsCache to build response
    public MsgWrapper getNode(ByteString key) {
    	if (circle.isEmpty()) {
          return null;
        }
        
    	//Command applies to current node
    	if(key == null){
    		return null;
    	}
    	
    	String hashKey;
		try {
			hashKey = hashFunction(key.toString());
			if (!circle.containsKey(hashKey)) {
		          SortedMap<String, MsgWrapper> tailMap = circle.tailMap(hashKey);
		          hashKey = tailMap.isEmpty() ?
		                 circle.firstKey() : tailMap.firstKey();
		        }
				if(circle.get(hashKey).getAddress() == currentAddress 
						&& circle.get(hashKey).getPort() == currentPort){
					return null;
				}
				return circle.get(hashKey);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        
      }
    
    //MDA5 hashing
    private String hashFunction(String hash) throws NoSuchAlgorithmException{
    	MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(hash.getBytes());
        return new BigInteger(1, messageDigest).toString(16);
    }
    

}
