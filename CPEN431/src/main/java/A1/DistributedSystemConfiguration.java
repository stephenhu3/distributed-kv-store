package A1;

import io.dropwizard.Configuration;

public class DistributedSystemConfiguration extends Configuration {
    // Enable debugging print statements
    public static final boolean VERBOSE = false;
    // UDP request consists of 16 bytes of uniqueID, 4 bytes of student number
    public static final int REQ_UDP_SIZE = 20;
    public static final int UNIQUE_ID_UDP_SIZE = 16;
}
