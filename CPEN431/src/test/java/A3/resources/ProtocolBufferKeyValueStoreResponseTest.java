package A3.resources;

import static A3.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;

import A3.core.KeyValueStoreSingleton;
import A3.proto.Message.Msg;
import A3.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import org.junit.Test;

public class ProtocolBufferKeyValueStoreResponseTest {
    @Test
    public void testGeneratePutResponse() throws NoSuchAlgorithmException {
        byte[] messageID = UniqueIdentifier.generateUniqueID();
        byte[] key = new byte[32];
        byte[] value = new byte[32];

        SecureRandom.getInstanceStrong().nextBytes(key);
        SecureRandom.getInstanceStrong().nextBytes(value);

        Msg res = generatePutResponse(ByteString.copyFrom(key), ByteString.copyFrom(value),
            ByteString.copyFrom(messageID));
        ByteString retrieved = KeyValueStoreSingleton.getInstance().getMap().get(
            ByteString.copyFrom(key));
        Arrays.equals(value, retrieved.toByteArray());
    }
}
