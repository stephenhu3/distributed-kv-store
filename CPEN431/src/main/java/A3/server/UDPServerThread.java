package A3.server;

import static A3.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A3.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A3.utils.Checksum.calculateProtocolBufferChecksum;

import A3.proto.KeyValueRequest.kvRequest;
import A3.proto.Message.Msg;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

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

    public void run() {
        // TODO: break this monolithic run function into smaller functions
        byte[] buf = new byte[1024];

        // receive request
        DatagramPacket resPacket = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(resPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // deserialize request into Msg
        Msg responseMsg = null;
        try {
            responseMsg = Msg.parseFrom(
                Arrays.copyOf(resPacket.getData(), resPacket.getLength()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        byte[] messageID = responseMsg.getMessageID().toByteArray();
        byte[] payload = responseMsg.getPayload().toByteArray();

        // verify checksum
        if (responseMsg != null) {
            if (responseMsg.getCheckSum() != calculateProtocolBufferChecksum(messageID, payload)) {
                System.out.format("Invalid checksum detected in the response, retrying...\n");
                // TODO: Return self-defined error code
                return;
            }
        }

        // deserialize payload into kvRequest
        kvRequest kvReq = null;

        try {
            kvReq = kvRequest.parseFrom(responseMsg.getPayload());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        byte[] key = kvReq.getKey().toByteArray();
        byte[] value = kvReq.getKey().toByteArray();


        // figure out what response to send based on command given
        int cmd = kvReq.getCommand();

        byte[] reply = null;
        // note: request is not sent if not valid command, but enforce on server-side as well
        switch(cmd) {
            case 1:
                reply = generatePutResponse(key, value, messageID);
                break;
            case 2:
                reply = generateGetResponse(key, messageID);
                break;
            case 3:
                // TODO
                break;
            case 4:
                // TODO
                break;
            case 5:
                // TODO
                break;
            case 6:
                // TODO
                break;
            case 7:
                // TODO
                break;
            default:
                // TODO
                // return some error
        }

        // send response back to client
        InetAddress clientAddress = resPacket.getAddress();
        int clientPort = resPacket.getPort();
        DatagramPacket reqPacket = new DatagramPacket(
            reply, reply.length, clientAddress, clientPort);
        try {
            socket.send(reqPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
