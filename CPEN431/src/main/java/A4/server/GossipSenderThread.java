package A4.server;

import static A4.DistributedSystemConfiguration.GOSSIP_RECEIVER_PORT;
import static A4.DistributedSystemConfiguration.GOSSIP_SENDER_PORT;

import A4.proto.LiveHostsRequest.LiveHostsReq;
import A4.utils.ByteRepresentation;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GossipSenderThread extends Thread {
    private DatagramSocket socket;
    HostList hostList;

    public GossipSenderThread(String name, String filename) throws FileNotFoundException, SocketException {
        super(name);
        HashMap<InetAddress, Integer> liveHosts = new HashMap<>();
        ArrayList<String> allHosts = new ArrayList<String>();

        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        // Populate server list
        while (scanner.hasNext()) {
            allHosts.add(scanner.nextLine());
        }

        // Add itself to live hosts list and initialize UDP socket
        liveHosts.put(UDPServerThread.localAddress, 0);
        hostList = HostList.getInstance();
        hostList.init(liveHosts, allHosts);

        socket = new DatagramSocket(GOSSIP_SENDER_PORT);
    }

    public void run() {
        while (true) {
            InetAddress firstAddress, secondAddress;
            Random rand = new Random();
            ArrayList<String> allHosts;

            allHosts = hostList.getAllHosts();

            // Reach out to two random nodes
            String firstHost = allHosts.get(rand.nextInt(allHosts.size()));
            String secondHost = allHosts.get(rand.nextInt(allHosts.size()));

            // Initialize protocol buffer
            byte[] serverList = ByteRepresentation.hashMapToBytes(hostList.getLiveHosts());
            LiveHostsReq liveHostsReq = LiveHostsReq.newBuilder()
                    .setLiveHosts(ByteString.copyFrom(serverList))
                    .build();
            try {
                firstAddress = InetAddress.getByName(firstHost);
                secondAddress = InetAddress.getByName(secondHost);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            DatagramPacket firstPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, firstAddress, GOSSIP_RECEIVER_PORT);
            DatagramPacket secondPacket = new DatagramPacket(liveHostsReq.toByteArray(),
                liveHostsReq.toByteArray().length, secondAddress, GOSSIP_RECEIVER_PORT);

            try {
                socket.send(firstPacket);
                socket.send(secondPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO:
        }
    }
}
