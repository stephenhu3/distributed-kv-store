package A3.cli;

import static A3.DistributedSystemConfiguration.SERVER_MODE;
import static A3.DistributedSystemConfiguration.VERBOSE;

import A3.server.UDPServerThread;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class UDPServerThreadSpawnCommand extends io.dropwizard.cli.Command {
    public UDPServerThreadSpawnCommand() {
        super("spawn", "Spawn a UDP server thread");
    }

    @Override
    public void configure(Subparser subparser) {
        // Add command line option
        subparser.addArgument("-name")
            .dest("name")
            .type(String.class)
            .required(true)
            .help("Name of host server");

        subparser.addArgument("-port")
            .dest("port")
            .type(Integer.class)
            .required(true)
            .help("Port number to host server");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {

        String name = namespace.getString("name");
        int port = namespace.getInt("port");

        if (VERBOSE) {
            System.out.println("Name: " + name);
            System.out.println("Port: " + port);
        }

        // if in server mode, keep server running after each served request
        do {
            new UDPServerThread(name, port).run();
        } while (SERVER_MODE);
    }
}
