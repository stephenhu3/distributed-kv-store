package A7;

import io.dropwizard.Configuration;

public class DistributedSystemConfiguration extends Configuration {
    // Enable debugging print statements (detail levels 0, 1, or 2)
    public static final int VERBOSE = 1;
    // Used for marking node for shutdown after sending success response
    public static boolean SHUTDOWN_NODE = false;
    public static final int UNIQUE_ID_UDP_SIZE = 16;
    // Max protobuf message size is 16kB
    public static final int MAX_MSG_SIZE = 16384;
    // Heapsize set as 64mb
    public static final int JVM_HEAP_SIZE_KB = 64000;
    // Out of memory threshold triggers at 3.75% free memory remaining in verbose mode
    // set as 6.25% when VERBOSE is false
    public static final double OUT_OF_MEMORY_THRESHOLD = 0.05;
    public static final int MAX_HOPS = 10;
    public static final int UDP_SERVER_THREAD_POOL_NTHREADS = 30;
    public static final int CLIENT_TARGET_PORT = 10696;
    public static final int MAX_REP_PAYLOAD_SIZE= 15500;
    public static final int REP_FACTOR = 3;
}
