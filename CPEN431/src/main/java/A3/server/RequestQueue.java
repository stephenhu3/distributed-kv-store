package A3.server;

import A3.utils.MsgWrapper;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestQueue {
    private static RequestQueue instance = new RequestQueue();
    ConcurrentLinkedQueue<MsgWrapper> requestQueue;

    private RequestQueue() {
        requestQueue = new ConcurrentLinkedQueue<>();
    }

    public static RequestQueue getInstance() {
        return instance;
    }

    public ConcurrentLinkedQueue<MsgWrapper> getQueue() {
        return requestQueue;
    }
}
