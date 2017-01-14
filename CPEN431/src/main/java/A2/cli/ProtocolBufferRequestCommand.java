package A2.cli;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import A2.client.ProtocolBufferClient;
import A2.client.UDPClient;
import A2.proto.Message.Msg;
import A2.proto.RequestPayload.ReqPayload;
import A2.resources.ProtocolBufferStudentNumberResponse;
import io.dropwizard.setup.Bootstrap;

import static A2.DistributedSystemConfiguration.VERBOSE;
import static A2.resources.ProtocolBufferStudentNumberRequest.generateRequest;
import static A2.utils.ByteRepresentation.bytesToHex;
import static A2.utils.UniqueIdentifier.generateUniqueID;


public class ProtocolBufferRequestCommand extends io.dropwizard.cli.Command {
    public ProtocolBufferRequestCommand() {
        super("protoc", "Send student number protocol buffer request");
    }

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

        subparser.addArgument("-snum")
                .dest("snum")
                .type(Integer.class)
                .required(true)
                .help("Student number to send");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String ip = namespace.getString("ip");
        int port = namespace.getInt("port");
        int snum = namespace.getInt("snum");

        if (VERBOSE) {
            System.out.println("IP Address: " + ip);
            System.out.println("Port: " + port);
            System.out.println("Student Number: " + snum);
        }

        // sample test
        byte[] serialized = ProtocolBufferClient.serializeRequestPayload(snum);
        ReqPayload deserialized = ProtocolBufferClient.deserializeRequestPayload(serialized);


        byte[] messageID = generateUniqueID();
        Msg msg = generateRequest(snum, messageID);
        long checksum = msg.getCheckSum();

        if (VERBOSE) {
            System.out.println("Request HEX String: " + bytesToHex(msg.toByteArray()));
            System.out.println("Request Message ID: " + bytesToHex(messageID));
            System.out.println("Request Payload: " + bytesToHex(msg.getPayload().toByteArray()));
            System.out.println("Request Checksum: " + msg.getCheckSum());
        }

        System.out.println("Sending ID: " + snum);

        byte[] res = UDPClient.sendProtocolBufferRequest(msg, ip, port, messageID, checksum);
        ProtocolBufferStudentNumberResponse.parseResponse(res);
    }
}
