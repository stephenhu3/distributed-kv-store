package A3.resources;

import static A3.utils.ProtocolBuffers.wrapMessage;

import A3.proto.KeyValueRequest.KVRequest;
import A3.proto.Message.Msg;
import com.google.protobuf.ByteString;
import java.util.HashMap;

public class ProtocolBufferKeyValueStoreRequest {
    /*
    1. Field “command” with tag number one can be:
       0x01 - Put: This is a put operation.
       0x02 - Get: This is a get operation.
       0x03 - Remove: This is a remove operation.
       0x04 - Shutdown: shuts-down the node (used for testing and management)
       0x05 - DeleteAll: deletes all keys stored in the node (used for testing)
       0x06 - IsAlive: does nothing but replies with success if the node is alive.
       0x07 - GetPID: the node is expected to reply with the processID of the Java process
       [Note: We may add some more management operations]
       anything > 0x20. Your own commands if you want.  They may be useful for debugging.
    2. Field “key” with tag number two is the identification of the value in the key-value store
       and it is up to 32 bytes long.
    3. Field “value” with tag number three is only used with “put” operation.
       Its maximum length is 10,000 bytes.
    4. Field ‘version’ for the value, for now left unused.
    */

    private static HashMap<String, Integer> commands;
    static {
        commands = new HashMap<>();
        commands.put("put", 1);
        commands.put("get", 2);
        commands.put("remove", 3);
        commands.put("shutdown", 4);
        commands.put("deleteAll", 5);
        commands.put("isAlive", 6);
        commands.put("getPID", 7);
    }

    public static Msg generatePutRequest(ByteString key, ByteString val, ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("put"), key, val);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    public static Msg generateGetRequest(ByteString key, ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("get"), key, null);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    public static Msg generateRemoveRequest(ByteString key, ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("remove"), key, null);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    public static Msg generateShutdownRequest(ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("shutdown"), null, null);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    public static Msg generateDeleteAllRequest(ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("deleteAll"), null, null);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    public static Msg generateIsAliveRequest(ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("isAlive"), null, null);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    public static Msg generateGetPIDRequest(ByteString messageID) {
        KVRequest reqPayload = generateKvRequest(commands.get("getPID"), null, null);
        Msg msg = wrapMessage(messageID, reqPayload.toByteString());
        return msg;
    }

    private static KVRequest generateKvRequest(int cmd, ByteString key, ByteString val) {
        KVRequest.Builder reqPayload = KVRequest.newBuilder();
        reqPayload.setCommand(cmd);
        if (key != null) {
            reqPayload.setKey(key);
        }

        if (val != null) {
            reqPayload.setValue(val);
        }

        return reqPayload.build();
    }
}
