package A7.resources;

import static A7.DistributedSystemConfiguration.JVM_HEAP_SIZE_KB;
import static A7.DistributedSystemConfiguration.OUT_OF_MEMORY_THRESHOLD;
import static A7.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.utils.ByteRepresentation.bytesToHex;
import static A7.utils.ProtocolBuffers.wrapMessage;

import A7.core.ConsistentHashRing;
import A7.core.KeyValueStoreSingleton;
import A7.proto.KeyValueRequest.KVRequest;
import A7.proto.KeyValueResponse.KVResponse;
import A7.proto.Message.Msg;
import A7.utils.MsgWrapper;
import A7.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.NoSuchAlgorithmException;
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

    Note: all requests that return a non-zero (failure) error code should not modify server state
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

    // note, ConcurrentHashMap throws NullPointerException if specified key or value is null
    public static Msg generatePutResponse(ByteString key, ByteString value, ByteString messageID) {
        KVResponse resPayload;
        int pid = UniqueIdentifier.getCurrentPID();

        if (key == null || value == null) {
            resPayload = generateKvReply(codes.get("KVStore failure"), null, pid);
        } else {
            if (Runtime.getRuntime().freeMemory() >
                (JVM_HEAP_SIZE_KB * OUT_OF_MEMORY_THRESHOLD) * 1024) {
                KeyValueStoreSingleton.getInstance().getMap().put(key, value);
                if (VERBOSE > 0) {
                    System.out.println("Put Value: " + bytesToHex(value.toByteArray()));
                }
                resPayload = generateKvReply(codes.get("success"), value, pid);
            } else {
                if (VERBOSE > 0) {
                    System.out.println("Out of memory, remaining: "
                        + Runtime.getRuntime().freeMemory());
                }
                // return out of memory response if GC limit about to be exceeded
                return generateOutOfMemoryResponse(messageID);
            }
        }
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static Msg generateGetResponse(ByteString key, ByteString messageID) {
        KVResponse resPayload;
        int pid = UniqueIdentifier.getCurrentPID();

        if (KeyValueStoreSingleton.getInstance().getMap().containsKey(key)) {
            ByteString value = KeyValueStoreSingleton.getInstance().getMap().get(key);
            resPayload = generateKvReply(codes.get("success"), value, pid);
            if (VERBOSE > 0) {
                System.out.println("Get Value: " + bytesToHex(value.toByteArray()));
            }
        } else {
            resPayload = generateKvReply(codes.get("key does not exist"), null, pid);
            if (VERBOSE > 0) {
                System.out.println("Attempted Get Key: " + bytesToHex(key.toByteArray())
                    + " does not exist");
            }
        }
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static Msg generateRemoveResponse(ByteString key, ByteString messageID) {
        KVResponse resPayload;
        int pid = UniqueIdentifier.getCurrentPID();

        if (KeyValueStoreSingleton.getInstance().getMap().containsKey(key)) {
            KeyValueStoreSingleton.getInstance().getMap().remove(key);
            resPayload = generateKvReply(codes.get("success"), null, pid);
            if (VERBOSE > 0) {
                System.out.println("Removed Key: " + bytesToHex(key.toByteArray()));
            }
        } else {
            resPayload = generateKvReply(codes.get("key does not exist"), null, pid);
            if (VERBOSE > 0) {
                System.out.println("Failed attempted to remove key: "
                    + bytesToHex(key.toByteArray()) + " does not exist");
            }
        }
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static Msg generateShutdownResponse(ByteString messageID) {
        int pid = UniqueIdentifier.getCurrentPID();
        KVResponse resPayload = generateKvReply(codes.get("success"), null, pid);
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        // requirement states sending success response on shutdown
        SHUTDOWN_NODE = true;
        return msg;
    }

    public static Msg generateDeleteAllResponse(ByteString messageID) {
        KeyValueStoreSingleton.getInstance().getMap().clear();
        int pid = UniqueIdentifier.getCurrentPID();
        KVResponse resPayload = generateKvReply(codes.get("success"), null, pid);
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static Msg generateIsAlive(ByteString messageID) {
        return generateGetPIDResponse(messageID);
    }

    public static Msg generateGetPIDResponse(ByteString messageID) {
        int pid = UniqueIdentifier.getCurrentPID();
        KVResponse resPayload = generateKvReply(codes.get("success"), null, pid);
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static Msg generateUnrecognizedCommandResponse(ByteString messageID) {
        int pid = UniqueIdentifier.getCurrentPID();
        KVResponse resPayload = generateKvReply(codes.get("unrecognized command"), null, pid);
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static Msg generateOutOfMemoryResponse(ByteString messageID) {
        int pid = UniqueIdentifier.getCurrentPID();
        KVResponse resPayload = generateKvReply(codes.get("out of memory"), null, pid);
        Msg msg = wrapMessage(messageID, resPayload.toByteString());
        return msg;
    }

    public static KVResponse generateKvReply(int err, ByteString val, int pid) {
        KVResponse.Builder resPayload = KVResponse.newBuilder();
        resPayload.setErrCode(err);

        if (val != null) {
            resPayload.setValue(val);
        }

        if (pid != -1) {
            resPayload.setPid(pid);
        }

        return resPayload.build();
    }

    private static Msg generateResponse(int cmd, ByteString key, ByteString value,
        ByteString messageID) {
        Msg reply;

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

    public static void parseResponse(ByteString response) {
        KVResponse reply = null;

        // deserialize response payload
        try {
            reply = KVResponse.parseFrom(response);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if (VERBOSE > 0) {
            System.out.println("Error Code: " + reply.getErrCode());
            System.out.println("Value: " + bytesToHex(reply.getValue().toByteArray()));
            System.out.println("PID: " + reply.getPid());
            // Latest protocol buffer definitions removed version field, uncomment once reintroduced
            // System.out.println("Version: " + reply.getVersion());
        }
    }

    public static MsgWrapper serveRequest(Msg req) {
        KVRequest request = null;
        MsgWrapper forwardRequest = null;

        try {
            request = KVRequest.parseFrom(req.getPayload());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        // duplicate request to next two successors to maintain replication factor 3
        try {
            forwardRequest = ConsistentHashRing.getInstance().getNode(request.getKey());
            // TODO: use reworked client to send request to successors
            // use UDPClient and generateXRequest here to duplicate requests to replicas
            String origin = ConsistentHashRing.getInstance().getKey(request.getKey());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Msg response;
        // currentNode is correct node, find a response, set correct receiver
        if (forwardRequest != null && (forwardRequest.getPort() == 0
            || forwardRequest.getAddress() == null)) {
            response = generateResponse(
                request.getCommand(),
                request.getKey(),
                request.getValue(),
                req.getMessageID()
            );
            forwardRequest.setMessage(response);
        } else {
            forwardRequest.setMessage(req);
        }
        return forwardRequest;
    }
}
