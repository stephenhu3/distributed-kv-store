package A3;

import io.dropwizard.Configuration;

public class DistributedSystemConfiguration extends Configuration {
    // Enable debugging print statements
    public static final boolean VERBOSE = true;
    // Keep alive if in server mode to serve multiple requests
    public static final boolean SERVER_MODE = true;
    // Used for marking node for shutdown after sending success response
    public static boolean SHUTDOWN_NODE = false;
    public static final int UNIQUE_ID_UDP_SIZE = 16;
    // Max protobuf message size is 16kB
    public static final int MAX_MSG_SIZE = 16384;
}
