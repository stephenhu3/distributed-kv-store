package A3.server;

import A3.proto.Message.Msg;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KVResponseQueue {
    private static KVResponseQueue instance = new KVResponseQueue();
    ConcurrentLinkedQueue<Msg> responseQueue;

    private KVResponseQueue() {
        responseQueue = new ConcurrentLinkedQueue<>();
    }

    public static KVResponseQueue getInstance() {
        return instance;
    }

    public ConcurrentLinkedQueue<Msg> getQueue() {
        return responseQueue;
    }
}
