package A4.server;


import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateDeleteAllResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateGetPIDResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateIsAlive;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateRemoveResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateShutdownResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateUnrecognizedCommandResponse;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import A4.proto.KeyValueRequest.KVRequest;
import A4.proto.Message.Msg;

import java.security.NoSuchAlgorithmException;
import A4.utils.MsgWrapper;

public class KVOperationThread {
	public static MsgWrapper serveReq(Msg req) {
        KVRequest request = null;
        MsgWrapper wrap = null;
        try {
        	request = KVRequest.parseFrom(req.getPayload());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } 

        try {
        	wrap= ConsistentHashRing.getInstance().getNode(request.getKey());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Msg response;
        // currentNode is correct node, find a response, set correct receiver
        if (wrap != null && (wrap.getPort() == 0 || wrap.getAddress() == null)) {
            response = generateResponse(
            	request.getCommand(),
                request.getKey(),
                request.getValue(),
                req.getMessageID()
            );
            wrap.setMessage(response);
        } else {
        	wrap.setMessage(req);
        }
        return wrap;
    }

    private static Msg generateResponse(int cmd, ByteString key, ByteString value, ByteString messageID) {
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
