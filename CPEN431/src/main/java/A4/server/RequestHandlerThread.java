package A4.server;

import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;

import A4.proto.KeyValueRequest.KVRequest;
import A4.proto.KeyValueResponse.KVResponse;
import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;
import java.util.concurrent.ExecutionException;

import com.google.protobuf.InvalidProtocolBufferException;

public class RequestHandlerThread extends Thread {
    public RequestHandlerThread(String name) {
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
                
                if (msgRes.getPort() == 0 || msgRes.getAddress().equals(null)) {
                		msgRes.setAddress(wrappedMsg.getAddress());
		        		msgRes.setPort(wrappedMsg.getPort());
	        	}
                // Add processes response to ResponseQueue
                        if (msgRes.getPort() != 0 || !msgRes.getAddress().equals(null)) {
                	ResponseQueue.getInstance().getQueue().add(msgRes);
                }
            }
        }
    }
}
