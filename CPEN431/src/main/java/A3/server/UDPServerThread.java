package A3.server;

import A3.proto.Message.Msg;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class UDPServerThread extends Thread {
    private static final int DEFAULT_UDP_SERVER_PORT = 10129;
    private DatagramSocket socket;

    public UDPServerThread() throws IOException {
        this("UDPServerThread", DEFAULT_UDP_SERVER_PORT);
    }

    public UDPServerThread(String name, int port) throws IOException {
        super(name);
        socket = new DatagramSocket(port);
    }

    public void run() {
        byte[] buf = new byte[1024];

        // receive request
        DatagramPacket resPacket = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(resPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // deserialize request into Msg
        try {
            Msg responseMsg = Msg.parseFrom(Arrays.copyOf(resPacket.getData(), resPacket.getLength()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        // TODO: verify checksum
        // TODO: deserialize payload into kvRequest
        // TODO: build kvReply, build Msg
        // TODO: calculate checksum, send Msg with uniqueID received

        // send response back to client
        InetAddress clientAddress = resPacket.getAddress();
        int clientPort = resPacket.getPort();
        DatagramPacket reqPacket = new DatagramPacket(buf, buf.length, clientAddress, clientPort);
        try {
            socket.send(reqPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
