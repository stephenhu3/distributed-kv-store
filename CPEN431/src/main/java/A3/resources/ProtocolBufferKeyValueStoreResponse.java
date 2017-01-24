package A3.resources;

import static A3.DistributedSystemConfiguration.VERBOSE;
import static A3.utils.ByteRepresentation.bytesToHex;
import static A3.utils.ProtocolBuffers.wrapMessage;

import A3.core.KeyValueStoreSingleton;
import A3.proto.KeyValueReply.kvReply;
import A3.proto.Message.Msg;
import A3.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashMap;

public class ProtocolBufferKeyValueStoreResponse {
    /*
    The error code can be:
    0x00. Operation is successful.
    0x01. Non-existent key requested in a get or delete operation
    0x02. Out of space  (returned when there is no space left for a put).
    0x03. System overload.  The system decides to refuse the operation due to temporary overload
    0x04. Internal KVStore failure
    0x05. Unrecognized command.
    0x06. Invalid key:  the key length does not match the expected length.
    0x07. Invalid value length:  the value length does not match the expected length.
    [possibly more standard codes will get defined here]
    anything > 0x20. Your own error codes. [Define them in your Readme]

    Note: all requests that return a non-zero (failure) error code should not modify the state of the server
    */

    private static HashMap<String, Integer> codes;
    static {
        codes = new HashMap<>();
        codes.put("success", 0);
        codes.put("key does not exist", 1);
        codes.put("out of memory", 2);
        codes.put("system overload", 3);
        codes.put("KVStore failure", 4);
        codes.put("unrecognized command", 5);
        codes.put("invalid key", 6);
        codes.put("invalid value length", 7);
    }

    // TODO: probably move some of the KV mutating functions into UDPServerThread
    // on second thought, most of the operations are just simple calls to ConcurrentHashMap, which is exposed by the singleton's getInstance

    public static byte[] generateGetResponse(byte[] key, byte[] messageID) {
        byte[] value = KeyValueStoreSingleton.getInstance().getMap().get(key).toByteArray();
        kvReply resPayload;
        int pid = UniqueIdentifier.getCurrentPID();

        if (value != null) {
            resPayload = generateKvReply(codes.get("success"), value, pid);
        } else {
            resPayload = generateKvReply(codes.get("key does not exist"), null, pid);
        }
        Msg msg = wrapMessage(messageID, resPayload.toByteArray());
        return msg.toByteArray();
    }

    // note, ConcurrentHashMap throws NullPointerException if specified key or value is null
    public static byte[] generatePutResponse(byte[] key, byte[] value, byte[] messageID) {
        kvReply resPayload;
        int pid = UniqueIdentifier.getCurrentPID();

        if (key == null || value == null) {
            resPayload = generateKvReply(codes.get("KVStore failure"), null, pid);
        } else {
            try {
                KeyValueStoreSingleton.getInstance().getMap().put(ByteString.copyFrom(key),
                    ByteString.copyFrom(value));
            } catch(OutOfMemoryError e) {
                // return out of space error response, clear map
                KeyValueStoreSingleton.getInstance().getMap().clear();
                resPayload = generateKvReply(codes.get("out of memory"), null, pid);
                Msg msg = wrapMessage(messageID, resPayload.toByteArray());
                return msg.toByteArray();
            }
            resPayload = generateKvReply(codes.get("success"), null, pid);
        }
        Msg msg = wrapMessage(messageID, resPayload.toByteArray());
        return msg.toByteArray();
    }

    // TODO: operations for deleteAll, remove, etc.

    private static kvReply generateKvReply(int err, byte[] val, int pid) {
        kvReply.Builder resPayload = kvReply.newBuilder();
        resPayload.setErrCode(err);

        if (val != null) {
            resPayload.setValue(ByteString.copyFrom(val));
        }

        if (pid != -1) {
            resPayload.setPid(pid);
        }

        return resPayload.build();
    }

    public static void parseResponse(byte[] response) {
        kvReply reply = null;

        // deserialize response payload
        try {
            reply = kvReply.parseFrom(response);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if (VERBOSE) {
            System.out.println("Error Code: " + reply.getErrCode());
            System.out.println("Value: " + bytesToHex(reply.getValue().toByteArray()));
            System.out.println("PID: " + reply.getPid());
            System.out.println("Version: " + reply.getVersion());
        }
    }
}
