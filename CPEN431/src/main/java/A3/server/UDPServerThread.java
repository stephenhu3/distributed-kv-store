package A3.server;

import static A3.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A3.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateDeleteAllResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetPIDResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateIsAlive;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateOutOfMemoryResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateRemoveResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateShutdownResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateUnrecognizedCommandResponse;
import static A3.utils.Checksum.calculateProtocolBufferChecksum;

import A3.proto.Message.Msg;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class UDPServerThread extends Thread {
    private static final int DEFAULT_UDP_SERVER_PORT = 10129;
    private DatagramSocket socket;

    public UDPServerThread() throws IOException {
        this("UDPServerThread", DEFAULT_UDP_SERVER_PORT);
    }

    public UDPServerThread(String name, int port) throws IOException {
        super(name);
        socket = new DatagramSocket(port);
    }

    public static byte[] generateResponse(int cmd, byte[] key, byte[] value, byte[] messageID) {
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

    public void run() {
        // TODO: break this monolithic run function into smaller functions
        byte[] buf = new byte[MAX_MSG_SIZE];

        // receive request
        DatagramPacket reqPacket = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(reqPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // deserialize request into Msg
        Msg requestMsg = null;
        try {
            requestMsg = Msg.parseFrom(
                Arrays.copyOf(reqPacket.getData(), reqPacket.getLength()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        // return out of space error if GC overhead limit exceeded
        byte[] messageID = null;
        byte[] payload = null;
        byte[] reply = null;

        try {
            messageID = requestMsg.getMessageID().toByteArray();
            payload = requestMsg.getPayload().toByteArray();

            // verify checksum
            if (requestMsg != null) {
                if (requestMsg.getCheckSum() != calculateProtocolBufferChecksum(messageID, payload)) {
                    System.out.format("Invalid checksum detected in the response, retrying...\n");
                    // TODO: Return self-defined error code
                    return;
                }
            }

            // TODO: Use request cache, expand self-defined exceptions
            Msg msgRes = null;
            try {
                msgRes = RequestCache.getInstance().getCache().get(requestMsg);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            reply = msgRes.toByteArray();
        } catch (OutOfMemoryError e) {
            messageID = null;
            payload = null;
            reply = generateOutOfMemoryResponse(requestMsg.getMessageID().toByteArray());
        }

        // send response back to client
        InetAddress clientAddress = reqPacket.getAddress();
        int clientPort = reqPacket.getPort();
        DatagramPacket resPacket = new DatagramPacket(
            reply, reply.length, clientAddress, clientPort);
        try {
            socket.send(resPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
        if (SHUTDOWN_NODE) {
            System.exit(0);
        }
    }
}
