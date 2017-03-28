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
    
 // Finds which node has failed
    private void FailDetection() throws NoSuchAlgorithmException, SocketException{
    	// Check if successor is down
    	String currentNodeHash = UniqueIdentifier.MD5Hash(
    			UDPServerThreadPool.localAddress.getHostAddress()
    			+":"+ UDPServerThreadPool.localPort);
        
    	Entry<String, MsgWrapper> successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(currentNodeHash);
    	if(successor == null)
    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
    	if (!NodesList.getInstance().getLiveNodes().containsKey(successor.getValue().getAddress())) {
            //successor is down, must send own replicatoin store to next available successor
    		successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
    		if(successor == null)
	    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
    		while(!NodesList.getInstance().getLiveNodes().containsKey(successor.getValue().getAddress())){
    			//no live successor found yet, check next
    			successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
    			if(successor == null)
    	    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
            }
    		//send to successor
    		if (!successor.getValue().getAddress().equals(UDPServerThreadPool.localAddress)){
	    		UDPServerThreadPool.executor.execute(new SendReplication(successor.getValue()));
    		}
        }
    	
    	// Check if predecessor is down and if down keep checking next predecessor
    	// Check's only up to REP_FACTOR -1 predecessors b/c REP_FACTOR inclusive
    	// 		current node will not have keys that is REP_FACTOR away
    	int deadPred = 0;
    	Entry<String, MsgWrapper> predecessor = ConsistentHashRing.getInstance().getHashRing().lowerEntry(currentNodeHash);
    	if(predecessor == null)
    		predecessor = ConsistentHashRing.getInstance().getHashRing().lastEntry();
    	while(!NodesList.getInstance().getLiveNodes().containsKey(predecessor.getValue().getAddress()) 
    			&& deadPred < (REP_FACTOR -1) ){
			//Predecessor is down, see it IT'S predecessor is down
    		deadPred++;
    		predecessor = ConsistentHashRing.getInstance().getHashRing().lowerEntry(predecessor.getKey());
    		if(predecessor == null)
        		predecessor = ConsistentHashRing.getInstance().getHashRing().lastEntry();
        }
    	
    	if(deadPred > 0){
    		// Navigate to first node that needs duplication
    		// if deadPred == 2, 1 node up from current; if deadPread == 1, 2 nodes from current, etc
	    	successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(currentNodeHash);
	    	if(successor == null)
	    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
    		
	    	//find next successor that's alive since first duplicated node
	    	for(int skip = deadPred; skip < (REP_FACTOR-1); skip++ ){
	    		successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
	    		if(successor == null)
    	    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
			}
	    	for(int i = 0; i < deadPred; i++){
	    		// if current successor dead, find next linve
	    		while(!NodesList.getInstance().getLiveNodes().containsKey(successor.getValue().getAddress())){
	    			//no live successor found yet, check next
	    			successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
	    			if(successor == null)
	    	    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
	            }
				//Landed on live successor node, execute
	    		if (!successor.getValue().getAddress().equals(UDPServerThreadPool.localAddress)){
		    		UDPServerThreadPool.executor.execute(new SendReplication(successor.getValue()));
	    		}
	    		successor = ConsistentHashRing.getInstance().getHashRing().higherEntry(successor.getKey());
	    		if(successor == null)
    	    		successor = ConsistentHashRing.getInstance().getHashRing().firstEntry();
			}
    	}
    }
}
