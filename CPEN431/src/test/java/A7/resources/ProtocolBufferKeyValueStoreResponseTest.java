package A7.resources;

import static A7.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static org.junit.Assert.assertEquals;

import A7.core.KeyValueStoreSingleton;
import A7.core.VersionedValue;
import A7.proto.Message.Msg;
import A7.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.junit.Test;

public class ProtocolBufferKeyValueStoreResponseTest {
    @Test
    public void testGeneratePutResponse() throws NoSuchAlgorithmException {
        byte[] messageID = UniqueIdentifier.generateUniqueID();
        byte[] key = new byte[32];
        byte[] value = new byte[32];

        SecureRandom.getInstanceStrong().nextBytes(key);
        SecureRandom.getInstanceStrong().nextBytes(value);
        VersionedValue expectedValue = new VersionedValue(ByteString.copyFrom(value), 1);

        Msg res = generatePutResponse(ByteString.copyFrom(key), ByteString.copyFrom(value),
            ByteString.copyFrom(messageID), 1);
        VersionedValue actualValue = KeyValueStoreSingleton.getInstance().getMap().get(
            ByteString.copyFrom(key));
        assertEquals(expectedValue, actualValue);
    }
}
