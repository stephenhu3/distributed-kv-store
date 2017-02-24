package A4.server;

import A4.proto.Message.Msg;
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
