package A4.server;

import A4.proto.Message.Msg;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KVRequestQueue {
    private static KVRequestQueue instance = new KVRequestQueue();
    ConcurrentLinkedQueue<Msg> requestQueue;

    private KVRequestQueue() {
        requestQueue = new ConcurrentLinkedQueue<>();
    }

    public static KVRequestQueue getInstance() {
        return instance;
    }

    public ConcurrentLinkedQueue<Msg> getQueue() {
        return requestQueue;
    }
}
