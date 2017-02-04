package A4.server;

import A4.utils.MsgWrapper;
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
