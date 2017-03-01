package A4.server;

import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;

import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RequestHandlerThread extends Thread {
    public RequestHandlerThread(String name) throws IOException {
        super(name);
    }

    public void run() {
        while (true) {
            if (SHUTDOWN_NODE) {
                System.exit(0);
            }
            while (!RequestQueue.getInstance().getQueue().isEmpty()) {
                MsgWrapper wrappedMsg = RequestQueue.getInstance().getQueue().poll();
                Msg requestMsg = wrappedMsg.getMessage();
                MsgWrapper msgRes = null;
                try {
                    msgRes = RequestCache.getInstance().getCache().get(requestMsg);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (msgRes.getAddress() == null && msgRes.getPort() == 0) {
                	msgRes.setAddress(wrappedMsg.getAddress());
                	msgRes.setPort(wrappedMsg.getPort());
                }
                // Add processes response to ResponseQueue
                ResponseQueue.getInstance().getQueue().add(msgRes);
            }
        }
    }
}
