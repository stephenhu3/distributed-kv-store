package A7.server;

import static A7.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import A7.core.KeyValueStoreSingleton;
import A7.core.VersionedValue;
import A7.proto.Message.Msg;
import A7.utils.MsgWrapper;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;

public class SendReplicationTest {
	SendReplication sendReplication;
	ConcurrentHashMap<ByteString, VersionedValue> KVStore;
    
	@org.junit.Before
    public void setUp() throws Exception {
        KVStore = KeyValueStoreSingleton.getInstance().getMap();
        sendReplication = new SendReplication(null);
        KVStore.put(ByteString.copyFrom("Key1".getBytes()), new VersionedValue(ByteString.copyFrom("Value1".getBytes()), 1));
        KVStore.put(ByteString.copyFrom("Key2".getBytes()), new VersionedValue(ByteString.copyFrom("Value2".getBytes()), 2));
        KVStore.put(ByteString.copyFrom("Key3".getBytes()), new VersionedValue(ByteString.copyFrom("Value3".getBytes()), 3));
        KVStore.put(ByteString.copyFrom("Key4".getBytes()), new VersionedValue(ByteString.copyFrom("Value4".getBytes()), 4));
        KVStore.put(ByteString.copyFrom("Key5".getBytes()), new VersionedValue(ByteString.copyFrom("Value5".getBytes()), 5));    
	}

	@org.junit.Test
	public void testCreateSubMap1() throws NoSuchAlgorithmException, IOException {
	    int from = 0;
	    int to = 1;
	    ByteString key = null;
        ByteString subMap = sendReplication.createSubMap(from, to);
        ConcurrentHashMap<ByteString, VersionedValue> dupeMap =
            new ConcurrentHashMap<ByteString, VersionedValue>();
        // Parse ByteString back to Map
        try {
   		    ByteArrayInputStream byteIn = new ByteArrayInputStream(subMap.toByteArray());
   	        ObjectInputStream in;
            in = new ObjectInputStream(byteIn);
   	        dupeMap = (ConcurrentHashMap<ByteString, VersionedValue>) in.readObject();
   		} catch (IOException | ClassNotFoundException e) {
   	        e.printStackTrace();
   	    }
        assertEquals(dupeMap.size(), to-from);
        Object[] keySet = KeyValueStoreSingleton.getInstance().getMap().keySet().toArray();
	    for(int i = from; i < to; i++) {
            key = (ByteString) keySet[i];
            assertEquals(dupeMap.get(key).getValue(), KVStore.get(key).getValue());
        }
    }

	@org.junit.Test
	public void testCreateSubMap2() throws NoSuchAlgorithmException, IOException {
	    int from = 0;
	    int to = 3;
	    ByteString key = null;
        ByteString subMap = sendReplication.createSubMap(from, to);
        ConcurrentHashMap<ByteString, VersionedValue> dupeMap =
            new ConcurrentHashMap<ByteString, VersionedValue>();
        // Parse ByteString back to Map
        try {
   		    ByteArrayInputStream byteIn = new ByteArrayInputStream(subMap.toByteArray());
   	        ObjectInputStream in;
            in = new ObjectInputStream(byteIn);
   	        dupeMap = (ConcurrentHashMap<ByteString, VersionedValue>) in.readObject();
   		} catch (IOException | ClassNotFoundException e) {
   	        e.printStackTrace();
   	    }
        assertEquals(dupeMap.size(), to-from);
        Object[] keySet = KeyValueStoreSingleton.getInstance().getMap().keySet().toArray();
	    for(int i = from; i < to; i++) {
            key = (ByteString) keySet[i];
            assertEquals(dupeMap.get(key).getValue(), KVStore.get(key).getValue());
        }
    }

	@org.junit.Test
	public void sendDupeRequestMsg() throws NoSuchAlgorithmException, IOException {
		int from = 0;
	    int to = 1;
	    ByteString subMapByteString = sendReplication.createSubMap(from, to);
		
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
        
        Msg request = null;
		try {
			request = Msg.parseFrom(
		        Arrays.copyOf(reqPacket.getData(), reqPacket.getLength()));
		} catch (InvalidProtocolBufferException e) {
		    e.printStackTrace();
		}

		ByteString currentID = request.getMessageID();
		byte[] payload = request.getPayload().toByteArray();

		// verify checksum
		assertNotNull(request);
		assertEquals(request.getCheckSum(),
            calculateProtocolBufferChecksum(currentID.toByteArray(), payload));
	}
    
}
