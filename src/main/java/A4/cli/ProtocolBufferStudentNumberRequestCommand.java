package A4.cli;

import static A4.DistributedSystemConfiguration.VERBOSE;
import static A4.resources.ProtocolBufferStudentNumberRequest.generateRequest;
import static A4.utils.ByteRepresentation.bytesToHex;
import static A4.utils.UniqueIdentifier.generateUniqueID;

import A4.client.UDPClient;
import A4.proto.Message.Msg;
import A4.resources.ProtocolBufferStudentNumberResponse;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class ProtocolBufferStudentNumberRequestCommand extends io.dropwizard.cli.Command {
    public ProtocolBufferStudentNumberRequestCommand() {
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

        byte[] messageID = generateUniqueID();
        byte[] msgBytes = generateRequest(snum, messageID);

        if (VERBOSE) {
            Msg msg = Msg.parseFrom(msgBytes);
            System.out.println("Request HEX String: " + bytesToHex(msgBytes));
            System.out.println("Request Message ID: " + bytesToHex(messageID));
            System.out.println("Request Payload: " + bytesToHex(msg.getPayload().toByteArray()));
            System.out.println("Request Checksum: " + msg.getCheckSum());
        }

        System.out.println("Sending ID: " + snum);

        byte[] res = UDPClient.sendProtocolBufferRequest(msgBytes, ip, port, messageID);
        ProtocolBufferStudentNumberResponse.parseResponse(res);
    }
}
