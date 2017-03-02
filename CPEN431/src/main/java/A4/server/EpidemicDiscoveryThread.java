package A4.server;


import A4.utils.ByteRepresentation;

import static A4.DistributedSystemConfiguration.EPIDEMIC_PORT;
import static A4.DistributedSystemConfiguration.VERBOSE;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import A4.proto.LiveHostsRequest.LiveHostsReq;
import com.google.protobuf.ByteString;

public class EpidemicDiscoveryThread extends Thread{

    private DatagramSocket socket;
    private HashMap<InetAddress, Integer> aliveHosts;
    private ArrayList<String> allHosts;
    //TODO: find a way to reference this as an argument in the command line
    private String filename = "src/main/resources/serverlist.txt";

    public EpidemicDiscoveryThread() throws IOException {
        super();
        File file;
        Scanner scanner;

        this.aliveHosts = new HashMap<>();
        this.allHosts = new ArrayList<>();
        file = new File(filename);
        scanner = new Scanner(file);

        /* Populate server list */
        while (scanner.hasNext()) {
            allHosts.add(scanner.nextLine());
        }

        /* Add self to own list and initialize udp socket*/
        this.aliveHosts.put(UDPServerThread.localAddress, 0);
        socket = new DatagramSocket();

        if (VERBOSE) {
            System.out.println("Epidemic Discovery Thread Initialized");
        }
    }

    public void run() {
        int i, j;
        Random rand;
        DatagramPacket packet1;
        DatagramPacket packet2;
        byte[] serverListBytes;
        String host1, host2;
        InetAddress addr1, addr2;
        LiveHostsReq liveHostsReq;

        rand = new Random();

        while (true) {

            /* Reach out to two random nodes */
            i = rand.nextInt(allHosts.size());
            j = rand.nextInt(allHosts.size());
            host1 = allHosts.get(i);
            host2 = allHosts.get(j);

            /* Initialize protocol buffer */
            serverListBytes = ByteRepresentation.hashMapToBytes(aliveHosts);
            liveHostsReq = LiveHostsReq.newBuilder()
                    .setLiveHosts(ByteString.copyFrom(serverListBytes))
                    .build();
            try {
                addr1 = InetAddress.getByName(host1);
                addr2 = InetAddress.getByName(host2);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            packet1 = new DatagramPacket(liveHostsReq.toByteArray(), liveHostsReq.toByteArray().length, addr1, EPIDEMIC_PORT);
            packet2 = new DatagramPacket(liveHostsReq.toByteArray(), liveHostsReq.toByteArray().length, addr2, EPIDEMIC_PORT);
            try {
                socket.send(packet1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                socket.send(packet2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (VERBOSE) {
            }
        }
    }
}
