package A7.server;


import static A7.DistributedSystemConfiguration.MAX_HOPS;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

public class NodesList {
    private static NodesList instance = new NodesList();
    private NodesList() {}
    private Map<InetAddress, Integer> liveNodes;
    private Map<String, Integer> allNodes;

    public static NodesList getInstance() {
        return instance;
    }

    public Map<InetAddress, Integer> getLiveNodes() {
        return liveNodes;
    }

    public Map<String, Integer> getAllNodes() {
        return allNodes;
    }

    public void setLiveNodes(Map<InetAddress, Integer> liveNodes) {
        this.liveNodes = liveNodes;
    }

    public void setAllNodes(Map<String, Integer> allNodes) {
        this.allNodes = allNodes;
    }

    public void addLiveNode(InetAddress addr, int hops) {
        liveNodes.put(addr, hops);
    }

    // Increment hops and clear old entries
    public void refreshLiveNodes() {
        liveNodes.put(UDPServerThread.localAddress, 0);
        for (Iterator<Map.Entry<InetAddress, Integer>> iter = liveNodes.entrySet().iterator();
            iter.hasNext();) {
            Map.Entry<InetAddress, Integer> entry = iter.next();
            if (entry.getValue() > MAX_HOPS) {
                iter.remove();
            } else {
                liveNodes.put(entry.getKey(), entry.getValue() + 1);
            }
        }
    }
}
