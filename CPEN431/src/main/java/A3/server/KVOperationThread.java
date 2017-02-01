package A3.server;

import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateDeleteAllResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetPIDResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateIsAlive;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateRemoveResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateShutdownResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateUnrecognizedCommandResponse;

public class KVOperationThread extends Thread {
    public byte[] generateResponse(int cmd, byte[] key, byte[] value, byte[] messageID) {
        byte[] reply = null;

        switch(cmd) {
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
