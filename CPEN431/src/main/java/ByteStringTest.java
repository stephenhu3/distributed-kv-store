package A3;

import com.google.protobuf.ByteString;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

public class ByteStringTest {
    public static void test() throws NoSuchAlgorithmException {
        HashMap<ByteString, ByteString> map = new HashMap<>();

        byte[] key = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(key);

        byte[] value = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(value);

        map.put(ByteString.copyFrom(key), ByteString.copyFrom(value));


        if (Arrays.equals(value, map.get(ByteString.copyFrom(key)).toByteArray())) {
            System.out.println("Equals");
        } else {
            System.out.println("Do not equal");
        }
    }
}
