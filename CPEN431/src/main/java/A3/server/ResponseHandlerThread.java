package A3.server;

import A3.utils.MsgWrapper;
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
        while(!ResponseQueue.getInstance().getQueue().isEmpty()) {
            MsgWrapper wrappedMsg = ResponseQueue.getInstance().getQueue().poll();
            byte[] reply = wrappedMsg.getMessage().toByteArray();
            DatagramPacket resPacket = new DatagramPacket(reply, reply.length,
                wrappedMsg.getAddress(), wrappedMsg.getPort());
            try {
                socket.send(resPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        }
    }
}
