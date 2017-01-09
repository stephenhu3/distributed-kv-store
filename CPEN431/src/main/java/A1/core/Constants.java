package A1.core;

public enum Constants {
    INSTANCE;

    public static final boolean VERBOSE = false;
    // based on my debugging, max message size is 250 bytes, not 16 KB as stated in criteria
    public static final int MAX_MSG_SIZE = 250;
    public static final int UNIQUE_ID_SIZE = 16;
    public static final int SECRET_CODE_LEN_SIZE = 4;
    public static final int TIMEOUT = 100; // default timeout of 100ms
    public static final int MAX_RETRIES = 3;
    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
}
