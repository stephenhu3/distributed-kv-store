package A4;

import io.dropwizard.Configuration;

public class DistributedSystemConfiguration extends Configuration {
    // Enable debugging print statements
    public static final boolean VERBOSE = true;
    // Greater debugging granularity
    public static final boolean DEBUG = true;
    // Keep alive if in server mode to serve multiple requests
    public static final boolean SERVER_MODE = true;
    // Used for marking node for shutdown after sending success response
    public static boolean SHUTDOWN_NODE = false;
    public static final int UNIQUE_ID_UDP_SIZE = 16;
    // Max protobuf message size is 16kB
    public static final int MAX_MSG_SIZE = 16384;
    // Heapsize set as 64mb
    public static final int JVM_HEAP_SIZE_KB = 64000;
    // Out of memory threshold triggers at 3.75% free memory remaining in verbose mode
    // set as 6.25% when VERBOSE is false
    public static final double OUT_OF_MEMORY_THRESHOLD = 0.09;
    public static final int UDP_SERVER_THREAD_PORT = 10696;
    public static final int GOSSIP_RECEIVER_PORT = 10697;
    public static final int GOSSIP_SENDER_PORT = 10698;
    public static final int MAX_HOPS = 5;
}
