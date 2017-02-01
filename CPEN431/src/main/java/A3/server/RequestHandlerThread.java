package A3.server;

import A3.proto.Message.Msg;
import A3.utils.MsgWrapper;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RequestHandlerThread extends Thread {
    public RequestHandlerThread(String name) throws IOException {
        super(name);
    }

    public void run() {
        while(!RequestQueue.getInstance().getQueue().isEmpty()) {
            MsgWrapper wrappedMsg = RequestQueue.getInstance().getQueue().poll();
            Msg requestMsg = wrappedMsg.getMessage();
            Msg msgRes = null;
            try {
                msgRes = RequestCache.getInstance().getCache().get(requestMsg);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            // Add processes response to ResponseQueue
            ResponseQueue.getInstance().getQueue().add(
                new MsgWrapper(msgRes, wrappedMsg.getAddress(), wrappedMsg.getPort()));
        }
    }
}
