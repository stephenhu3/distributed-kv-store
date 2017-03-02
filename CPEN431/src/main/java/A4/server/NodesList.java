package A4.server;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class NodesList {
    private static NodesList instance = new NodesList();
    private NodesList() {}
    private Map<InetAddress, Integer> liveNodes;
    private List<String> allNodes;

    public static NodesList getInstance() {
        return instance;
    }

    public Map<InetAddress, Integer> getLiveNodes() {
        return liveNodes;
    }

    public List<String> getAllNodes() {
        return allNodes;
    }

    public void setLiveNodes(Map<InetAddress, Integer> liveNodes) {
        this.liveNodes = liveNodes;
    }

    public void setAllNodes(List<String> allNodes) {
        this.allNodes = allNodes;
    }

    public void addLiveNode(InetAddress addr) {
        liveNodes.put(addr, 0);
    }

    // Increment hops and clear old entries
    public void refreshLiveNodes() {
        for (InetAddress addr : liveNodes.keySet()) {
            int hops = liveNodes.get(addr);
            if (hops > 9) {
                liveNodes.remove(addr);
            } else {
                liveNodes.put(addr, hops + 1);
            }
        }
    }
}
