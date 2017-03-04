package A4.server;

import java.io.File;
import java.net.InetAddress;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Emmett on 3/3/2017.
 */
public class ConsistentHashRingTest {
    NodesList nodesList;

    @org.junit.Before
    public void setUp() throws Exception {
        nodesList = NodesList.getInstance();
        Map<InetAddress, Integer> liveNodes = new HashMap<>();
        List<String> allNodes = new ArrayList<String>();

        String ipStr = InetAddress.getLocalHost().getHostAddress();
        allNodes.add(ipStr);

        nodesList.setAllNodes(allNodes);
        // Add itself to live hosts list
        nodesList.setLiveNodes(liveNodes);
        nodesList.addLiveNode(InetAddress.getLocalHost(), 0);
        ConsistentHashRing.getInstance().initializeNodes();
    }


    @org.junit.Test
    public void addNode() throws Exception {

    }

    @org.junit.Test
    public void removeNode() throws Exception {

    }

    @org.junit.Test
    public void getNode() throws Exception {

    }

}