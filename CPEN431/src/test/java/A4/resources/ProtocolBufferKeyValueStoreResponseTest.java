package A4.resources;

import static A4.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;

import A4.core.KeyValueStoreSingleton;
import A4.proto.Message.Msg;
import A4.utils.UniqueIdentifier;
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
