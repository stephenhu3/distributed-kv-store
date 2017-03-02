package A4.server;

import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;

import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;
import A4.utils.ProtocolBuffers;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.protobuf.ByteString;

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

                // Logic here b/c no lower level have access to original sender address
                // If no returned wrapper does not define return address
                //     Response returned to sender (response is not and should not be forwarded)
                // If msgRes.getForward() == true, host forwards resquest
                //     recreate response to include the fwdAddress and fwdPort
                if (msgRes.getPort() == 0 || msgRes.getAddress().equals(null)) {
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
