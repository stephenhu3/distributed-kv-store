package A3.cli;

import static A3.DistributedSystemConfiguration.VERBOSE;
import static A3.utils.ByteRepresentation.bytesToHex;
import static A3.utils.ByteRepresentation.hexToBytes;
import static A3.utils.UniqueIdentifier.generateUniqueID;

import A3.client.UDPClient;
import A3.proto.Message.Msg;
import A3.resources.ProtocolBufferKeyValueStoreRequest;
import A3.resources.ProtocolBufferKeyValueStoreResponse;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class ProtocolBufferKeyValueStoreRequestCommand extends io.dropwizard.cli.Command {
    public ProtocolBufferKeyValueStoreRequestCommand() {
        super("kv", "Send key value store protocol buffer request");
    }

    /*
    Field “command” with tag number one can be:
    0x01 - Put: This is a put operation.
    0x02 - Get: This is a get operation.
    0x03 - Remove: This is a remove operation.
    0x04 - Shutdown: shuts-down the (used for testing and management)
    0x05 - DeleteAll: deletes all keys stored in the node (used for testing)
    0x06 - IsAlive: does nothing but replies with success if the node is alive.
    0x07 - GetPID: the node is expected to reply with the processID of the Java process
    [Note: We may add some more management operations]
    anything > 0x20. Your own commands if you want.  They may be useful for debugging.
    2. Field “key” with tag number two is the identification of the value in the key-value store
    and it is up to 32 bytes long.
    3. Field “value” with tag number three is only used with “put” operation.
    Its maximum length is 10,000 bytes.
    */

    @Override
    public void configure(Subparser subparser) {
        // Add command line option
        subparser.addArgument("-ip")
            .dest("ip")
            .type(String.class)
            .required(true)
            .help("IP address to send request");

        subparser.addArgument("-port")
            .dest("port")
            .type(Integer.class)
            .required(true)
            .help("Port number of request");

        subparser.addArgument("-cmd")
            .dest("cmd")
            .type(String.class)
            .required(true)
            .help("Operation to perform");

        subparser.addArgument("-key")
            .dest("key")
            .type(String.class)
            .required(false)
            .help("Key to send");

        subparser.addArgument("-value")
            .dest("value")
            .type(String.class)
            .required(false)
            .help("Value to send");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String ip = namespace.getString("ip");
        int port = namespace.getInt("port");
        String cmd = namespace.getString("cmd");
        String key = namespace.getString("key");
        String value = namespace.getString("value");

        if (VERBOSE) {
            System.out.println("IP Address: " + ip);
            System.out.println("Port: " + port);
            System.out.println("Command: " + cmd);
            System.out.println("Key: " + key);
            System.out.println("Value: " + value);
        }

        byte[] messageID = generateUniqueID();
        byte[] msg;

        // map command to respective operation
        switch(cmd) {
            case "put":
                msg = ProtocolBufferKeyValueStoreRequest.generatePutRequest(
                    hexToBytes(key), hexToBytes(value), messageID);
                break;
            case "get":
                msg = ProtocolBufferKeyValueStoreRequest.generateGetRequest(
                    hexToBytes(key), messageID);
                break;
            case "remove":
                msg = ProtocolBufferKeyValueStoreRequest.generateRemoveRequest(
                    hexToBytes(key), messageID);
                break;
            case "shutdown":
                msg = ProtocolBufferKeyValueStoreRequest.generateShutdownRequest(messageID);
                break;
            case "deleteAll":
                msg = ProtocolBufferKeyValueStoreRequest.generateShutdownRequest(messageID);
                break;
            case "isAlive":
                msg = ProtocolBufferKeyValueStoreRequest.generateIsAliveRequest(messageID);
                break;
            case "getPID":
                msg = ProtocolBufferKeyValueStoreRequest.generateGetPIDRequest(messageID);
                break;
            default :
                System.out.println("Invalid command entered. Please try again");
                return;
        }

        if (VERBOSE) {
            Msg msgProto = Msg.parseFrom(msg);
            System.out.println("Request HEX String: " + bytesToHex(msg));
            System.out.println("Request Message ID: " + bytesToHex(messageID));
            System.out.println("Request Payload: " + bytesToHex(msgProto .getPayload().toByteArray()));
            System.out.println("Request Checksum: " + msgProto .getCheckSum());
        }


        // client sends request, res is the response
        byte[] res = UDPClient.sendProtocolBufferRequest(msg, ip, port, messageID);
        ProtocolBufferKeyValueStoreResponse.parseResponse(res);
    }
}
