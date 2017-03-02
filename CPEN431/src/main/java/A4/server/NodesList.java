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

    public void addLiveNode(InetAddress addr, int hops) {
        liveNodes.put(addr, hops);
    }

    // Increment hops and clear old entries
    public void refreshLiveNodes() {
        for (Map.Entry<InetAddress, Integer> node : liveNodes.entrySet()) {
            if (node.getValue() > 9) {
                liveNodes.remove(node.getKey());
            } else {
                liveNodes.put(node.getKey(), node.getValue() + 1);
            }
        }
    }
}
