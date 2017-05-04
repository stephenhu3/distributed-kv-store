package A7.resources;

import static A7.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A7.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import A7.core.KeyValueStoreSingleton;
import A7.core.VersionedValue;
import A7.proto.KeyValueRequest.KVRequest;
import A7.proto.Message.Msg;
import A7.server.SendReplication;
import A7.utils.MsgWrapper;
import A7.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class ProtocolBufferKeyValueStoreResponseTest {
    ConcurrentHashMap<ByteString, VersionedValue> KVStore;
    SendReplication sendReplication;

    @Before
    public void setUp() throws Exception {
        KVStore = KeyValueStoreSingleton.getInstance().getMap();
        sendReplication = new SendReplication(null);
        KVStore.put(ByteString.copyFrom("Key1".getBytes()), new VersionedValue(ByteString.copyFrom("Value1".getBytes()), 1));
        KVStore.put(ByteString.copyFrom("Key2".getBytes()), new VersionedValue(ByteString.copyFrom("Value2".getBytes()), 2));
        KVStore.put(ByteString.copyFrom("Key3".getBytes()), new VersionedValue(ByteString.copyFrom("Value3".getBytes()), 3));
        KVStore.put(ByteString.copyFrom("Key4".getBytes()), new VersionedValue(ByteString.copyFrom("Value4".getBytes()), 4));
        KVStore.put(ByteString.copyFrom("Key5".getBytes()), new VersionedValue(ByteString.copyFrom("Value5".getBytes()), 5));
    }

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


    @Test
    public void testGeneratePutDupesResponse() throws NoSuchAlgorithmException, IOException {
        Msg resMsg;
        ByteString value;
        ByteString messageId;
        KVRequest kvReq;
        ByteString key;
        int originalSize = KVStore.size();

        assert(originalSize > 0);

        key = ByteString.copyFrom("Key1".getBytes());


        sendReplication = new SendReplication(new MsgWrapper(null, InetAddress.getByName("localhost"), 11111) );
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(sendReplication, 2, TimeUnit.SECONDS);

        DatagramSocket socket = new DatagramSocket(11111);
        byte[] buf = new byte[MAX_MSG_SIZE];
        DatagramPacket reqPacket = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(reqPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        KVStore.clear();
        Msg request = null;
        try {
            request = Msg.parseFrom(
                    Arrays.copyOf(reqPacket.getData(), reqPacket.getLength()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        ByteString currentID = request.getMessageID();
        byte[] payload = request.getPayload().toByteArray();

        try {
            kvReq = KVRequest.parseFrom(payload);
        } catch (Exception e) {
            e.printStackTrace();
            socket.close();
            return;
        }

        assert(KVStore.size() == 0);

        resMsg = ProtocolBufferKeyValueStoreResponse.generatePutDupesResponse(ByteString.copyFrom(kvReq.getValue().toByteArray()), currentID);

        // verify checksum
        assertNotNull(request);
        assertEquals(request.getCheckSum(), calculateProtocolBufferChecksum(currentID.toByteArray(), payload));
        socket.close();
        assert(KVStore.size() == originalSize);
        assertEquals(KVStore.get(ByteString.copyFrom("Key1".getBytes())).getValue(), new VersionedValue(ByteString.copyFrom("Value1".getBytes()), 1).getValue());
        assertEquals(KVStore.get(ByteString.copyFrom("Key2".getBytes())).getValue(), new VersionedValue(ByteString.copyFrom("Value2".getBytes()), 2).getValue());
        assertEquals(KVStore.get(ByteString.copyFrom("Key3".getBytes())).getValue(), new VersionedValue(ByteString.copyFrom("Value3".getBytes()), 3).getValue());
        assertEquals(KVStore.get(ByteString.copyFrom("Key4".getBytes())).getValue(), new VersionedValue(ByteString.copyFrom("Value4".getBytes()), 4).getValue());
        assertEquals(KVStore.get(ByteString.copyFrom("Key5".getBytes())).getValue(), new VersionedValue(ByteString.copyFrom("Value5".getBytes()), 5).getValue());
        assertEquals(KVStore.get(ByteString.copyFrom("Key1".getBytes())).getVersion(), new VersionedValue(ByteString.copyFrom("Value1".getBytes()), 1).getVersion());
        assertEquals(KVStore.get(ByteString.copyFrom("Key2".getBytes())).getVersion(), new VersionedValue(ByteString.copyFrom("Value2".getBytes()), 2).getVersion());
        assertEquals(KVStore.get(ByteString.copyFrom("Key3".getBytes())).getVersion(), new VersionedValue(ByteString.copyFrom("Value3".getBytes()), 3).getVersion());
        assertEquals(KVStore.get(ByteString.copyFrom("Key4".getBytes())).getVersion(), new VersionedValue(ByteString.copyFrom("Value4".getBytes()), 4).getVersion());
        assertEquals(KVStore.get(ByteString.copyFrom("Key5".getBytes())).getVersion(), new VersionedValue(ByteString.copyFrom("Value5".getBytes()), 5).getVersion());
    }
}
