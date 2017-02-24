package A4.server;

import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;

import A4.utils.MsgWrapper;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ResponseHandlerThread extends Thread {
    private DatagramSocket socket;

    public ResponseHandlerThread(String name, int port) throws IOException {
        super(name);
        socket = new DatagramSocket(port);
    }

    public void run() {
        while (true) {
            if (SHUTDOWN_NODE) {
                socket.close();
                System.exit(0);
            }
            while (!ResponseQueue.getInstance().getQueue().isEmpty()) {
                MsgWrapper wrappedMsg = ResponseQueue.getInstance().getQueue().poll();
                byte[] reply = wrappedMsg.getMessage().toByteArray();
                DatagramPacket resPacket = new DatagramPacket(reply, reply.length,
                    wrappedMsg.getAddress(), wrappedMsg.getPort());
                try {
                    socket.send(resPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
