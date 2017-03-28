package A7.server;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;

import A7.core.ConsistentHashRing;
import A7.core.KeyValueStoreSingleton;
import A7.core.NodesList;
import A7.core.VersionedValue;
import A7.utils.MsgWrapper;
import A7.utils.UniqueIdentifier;

public class SendReplicationTest {
	SendReplication sendReplication;
	ConcurrentHashMap<ByteString, VersionedValue> KVStore;
    
	@org.junit.Before
    public void setUp() throws Exception {
        KVStore = KeyValueStoreSingleton.getInstance().getMap();
        sendReplication = new SendReplication(null)
        KVStore.put(ByteString.copyFrom("Key1".getBytes()), new VersionedValue(ByteString.copyFrom("Value1".getBytes()), 1));
        KVStore.put(ByteString.copyFrom("Key2".getBytes()), new VersionedValue(ByteString.copyFrom("Value2".getBytes()), 2));
        KVStore.put(ByteString.copyFrom("Key3".getBytes()), new VersionedValue(ByteString.copyFrom("Value3".getBytes()), 3));
        KVStore.put(ByteString.copyFrom("Key4".getBytes()), new VersionedValue(ByteString.copyFrom("Value4".getBytes()), 4));
        KVStore.put(ByteString.copyFrom("Key5".getBytes()), new VersionedValue(ByteString.copyFrom("Value5".getBytes()), 5));
    }

	@org.junit.Test
	public void testCreateSubMap() throws NoSuchAlgorithmException, IOException {
       ByteString subMap = sendReplication.createSubMap(0, 1);
       ConcurrentHashMap<ByteString, VersionedValue> dupeMap =
               new ConcurrentHashMap<ByteString, VersionedValue>();
       // Parse byte array to Map
        try {
   		    ByteArrayInputStream byteIn = new ByteArrayInputStream(value.toByteArray());
   	        ObjectInputStream in;
   			in = new ObjectInputStream(byteIn);
   			dupeMap = (ConcurrentSkipListMap<ByteString, VersionedValue>) in.readObject();
   		} catch (IOException | ClassNotFoundException e) {
   			resPayload = generateKvReply(codes.get("KVStore failure"), null, pid, -1);
   			e.printStackTrace();
   		}
	}

}
