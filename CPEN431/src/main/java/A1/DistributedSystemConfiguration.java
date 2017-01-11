package A1;

import io.dropwizard.Configuration;

public class DistributedSystemConfiguration extends Configuration {
    // Enable debugging print statements
    public static final boolean VERBOSE = false;
    // based on my debugging, max UDP message size is 250 bytes, not 16 KB as stated in criteria
    public static final int MSG_MAX_UDP_SIZE = 250;
    public static final int UNIQUE_ID_UDP_SIZE = 16;
}
