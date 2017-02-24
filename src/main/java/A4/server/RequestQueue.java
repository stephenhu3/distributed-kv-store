package A4.server;

import A4.utils.MsgWrapper;
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
