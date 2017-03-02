package A4.server;

import static A4.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateDeleteAllResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateGetPIDResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateIsAlive;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateRemoveResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateShutdownResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateUnrecognizedCommandResponse;

import A4.proto.KeyValueRequest.KVRequest;
import A4.proto.KeyValueResponse.KVResponse;
import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

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

                MsgWrapper fwdReq = null;
                try {
                    fwdReq = ConsistentHashRing.getInstance().getNode(kvReq.getKey());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                Msg res;
                // currentNode is correct node, find a response, set correct receiver
                if (fwdReq == null) {
		        	if (req.hasFwdPort() && req.hasFwdAddress()) {
		        		try {
		        			fwdReq = new MsgWrapper(null, InetAddress.getByAddress(
		        					req.getFwdAddress().toByteArray()), req.getFwdPort());
		        		} catch (UnknownHostException e) {
							e.printStackTrace();
						}
		        	}
		        	res = generateResponse(
		                    kvReq.getCommand(),
		                    kvReq.getKey(),
		                    kvReq.getValue(),
		                    req.getMessageID()
		                );
                    KVResponseQueue.getInstance().getQueue().add(res);
		        } else {
		        	KVResponseQueue.getInstance().getQueue().add(req);
		        }

		        ForwardingQueue.getInstance().getQueue().add(fwdReq);
            }
        }
    }

    private Msg generateResponse(int cmd, ByteString key, ByteString value, ByteString messageID) {
        Msg reply = null;

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
