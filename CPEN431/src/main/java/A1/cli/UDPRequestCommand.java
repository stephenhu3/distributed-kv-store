package A1.cli;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import A1.core.UDPRequest;
import io.dropwizard.setup.Bootstrap;

import static A1.core.Constants.VERBOSE;

public class UDPRequestCommand extends io.dropwizard.cli.Command {
    public UDPRequestCommand() {
        super("request", "Prints IP entered");
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
                .help("IP address port number");

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
        UDPRequest.sendRequest(ip, port, snum);
    }
}
