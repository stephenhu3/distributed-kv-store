package A4.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Emmett on 3/2/2017.
 */
public class HostList {
    private HashMap<InetAddress, Integer> liveHosts;
    private ArrayList<String> allHosts;
    private static HostList ourInstance = new HostList();

    public static HostList getInstance() {
        return ourInstance;
    }

    private HostList() {

    }

    public HashMap<InetAddress, Integer> getLiveHosts() { return liveHosts; }
    public ArrayList<String> getAllHosts() { return allHosts; }

    public void init(HashMap<InetAddress, Integer> liveHosts, ArrayList<String> allHosts) {
        this.liveHosts = liveHosts;
        this.allHosts = allHosts;
    }

    public void refreshEntry(InetAddress addr) {
        liveHosts.put(addr, 0);
    }

    /* Increment hops and clear old entries */
    public void incrementHops() {
        for (InetAddress addr : liveHosts.keySet()) {
            int numhops = liveHosts.get(addr);
            if (numhops > 9) {
                liveHosts.remove(addr);
            } else {
                liveHosts.put(addr, numhops + 1);
            }
        }
    }
}
