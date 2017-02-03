package A3.server;

import static A3.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateDeleteAllResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetPIDResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateIsAlive;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateRemoveResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateShutdownResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateUnrecognizedCommandResponse;

import A3.proto.KeyValueRequest.KVRequest;
import A3.proto.Message.Msg;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;

public class KVOperationThread extends Thread {

    public KVOperationThread(String name) throws IOException {
        super(name);
    }

    public void run() {
        while (true) {
            if (SHUTDOWN_NODE) {
                System.exit(0);
            }
            while (!KVRequestQueue.getInstance().getQueue().isEmpty()) {
                Msg req = KVRequestQueue.getInstance().getQueue().poll();
                KVRequest kvReq = null;

                try {
                    kvReq = KVRequest.parseFrom(req.getPayload());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                byte[] res = generateResponse(
                    kvReq.getCommand(),
                    kvReq.getKey().toByteArray(),
                    kvReq.getValue().toByteArray(),
                    req.getMessageID().toByteArray()
                );

                Msg resMsg = null;
                try {
                    resMsg = Msg.parseFrom(res);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                KVResponseQueue.getInstance().getQueue().add(resMsg);
            }
        }
    }

    private byte[] generateResponse(int cmd, byte[] key, byte[] value, byte[] messageID) {
        byte[] reply = null;

        switch (cmd) {
            case 1:
                reply = generatePutResponse(key, value, messageID);
                break;
            case 2:
                reply = generateGetResponse(key, messageID);
                break;
            case 3:
                reply = generateRemoveResponse(key, messageID);
                break;
            case 4:
                reply = generateShutdownResponse(messageID);
                break;
            case 5:
                reply = generateDeleteAllResponse(messageID);
                break;
            case 6:
                reply = generateIsAlive(messageID);
                break;
            case 7:
                reply = generateGetPIDResponse(messageID);
                break;
            default:
                // return error code 5, unrecognized command
                reply = generateUnrecognizedCommandResponse(messageID);
        }
        return reply;
    }
}