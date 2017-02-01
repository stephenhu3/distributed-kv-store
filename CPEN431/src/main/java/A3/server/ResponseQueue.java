package A3.server;

import A3.utils.MsgWrapper;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResponseQueue {
    private static ResponseQueue instance = new ResponseQueue();
    ConcurrentLinkedQueue<MsgWrapper> responseQueue;

    private ResponseQueue() {
        responseQueue = new ConcurrentLinkedQueue<>();
    }

    public static ResponseQueue getInstance() {
        return instance;
    }

    public ConcurrentLinkedQueue<MsgWrapper> getQueue() {
        return responseQueue;
    }
}
