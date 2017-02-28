package A4.server;

import A4.utils.MsgWrapper;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ForwardingQueue {
    private static ForwardingQueue instance = new ForwardingQueue();
    ConcurrentLinkedQueue<MsgWrapper> forwardingQueue;

    private ForwardingQueue() {
    	forwardingQueue = new ConcurrentLinkedQueue<>();
    }

    public static ForwardingQueue getInstance() {
        return instance;
    }

    public ConcurrentLinkedQueue<MsgWrapper> getQueue() {
        return forwardingQueue;
    }
}
