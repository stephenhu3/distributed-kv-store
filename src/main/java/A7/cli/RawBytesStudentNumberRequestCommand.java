package A7.cli;

import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.resources.RawBytesStudentNumberRequest.generateRequest;
import static A7.utils.ByteRepresentation.bytesToHex;
import static A7.utils.UniqueIdentifier.generateUniqueID;

import A7.client.UDPClient;
import A7.resources.RawBytesStudentNumberResponse;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class RawBytesStudentNumberRequestCommand extends io.dropwizard.cli.Command {
    public RawBytesStudentNumberRequestCommand() {
        super("request", "Send student number UDP request");
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

        if (VERBOSE > 0) {
            System.out.println("IP Address: " + ip);
            System.out.println("Port: " + port);
            System.out.println("Student Number: " + snum);
        }

        byte[] uniqueID = generateUniqueID();
        byte[] req = generateRequest(snum, uniqueID);

        if (VERBOSE > 0) {
            System.out.println("Request HEX String: " + bytesToHex(req));
        }

        System.out.println("Sending ID: " + snum);

        byte[] res = UDPClient.sendRawBytesRequest(req, ip, port, uniqueID);
        RawBytesStudentNumberResponse.parseResponse(res);
    }
}
