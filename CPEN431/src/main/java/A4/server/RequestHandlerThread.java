package A4.server;

import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;

import A4.proto.KeyValueRequest.KVRequest;
import A4.proto.KeyValueResponse.KVResponse;
import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;
import A4.utils.ProtocolBuffers;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.concurrent.ExecutionException;

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
                
                
                // Logic here because no lower level have access to original sender address
                // If no returned wrapper, does not define return address
                //  response returned to sender (response is not and should not be forwarded)
                // If msgRes.getForward() is true, host forwards request
                //  recreate response to include the fwdAddress and fwdPort
                if (msgRes.getPort() == 0 || msgRes.getAddress() == null) {
	                msgRes.setAddress(wrappedMsg.getAddress());
                    msgRes.setPort(wrappedMsg.getPort());
                } else if (msgRes.getForward()) {
                    Msg fwdRequest = ProtocolBuffers.wrapFwdMessage(msgRes.getMessage(),
                        ByteString.copyFromUtf8(wrappedMsg.getAddress().getHostAddress()),
                        wrappedMsg.getPort());
                    msgRes.setMessage(fwdRequest);
                }
                // Add processes response to ResponseQueue
                ResponseQueue.getInstance().getQueue().add(msgRes);
            }
        }
    }
}
