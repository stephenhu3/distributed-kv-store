package A4.cli;

import static A4.DistributedSystemConfiguration.VERBOSE;

import A4.resources.ListOfServers;
import A4.server.KVOperationThread;
import A4.server.RequestHandlerThread;
import A4.server.ResponseHandlerThread;
import A4.server.UDPServerThread;
import io.dropwizard.setup.Bootstrap;

import java.util.Iterator;
import java.util.Random;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class UDPServerThreadSpawnCommand extends io.dropwizard.cli.Command {
    public UDPServerThreadSpawnCommand() {
        super("spawn",
            "Run server that listens, performs operation, and responds to KV requests");
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
        
        subparser.addArgument("-servers")
	        .dest("servers")
	        .type(String.class)
	        .required(true)
	        .help("Path to list of line separated list of server ips and ports of system");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {

        String name = namespace.getString("name");
        int port = namespace.getInt("port");
        String servers = namespace.getString("servers");
        ListOfServers.initializeNodes(servers);

        if (VERBOSE) {
            System.out.println("Name: " + name);
            System.out.println("Port: " + port);
            System.out.println("Servers: " + servers);
        }

        // if in server mode, keep server running after each served request
        new UDPServerThread(name + "-server-thread", port).start();
        new RequestHandlerThread(name + "-request-handler").start();
        new KVOperationThread(name + "-kv-operation-thread").start();
        new ResponseHandlerThread(name + "-response-handler",
            port + new Random().nextInt(10000)).start();
    }
}
