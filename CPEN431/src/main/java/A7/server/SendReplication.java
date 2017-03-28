package A7.server;

import static A7.DistributedSystemConfiguration.MAX_REP_PAYLOAD_SIZE;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;
import static A7.utils.UniqueIdentifier.generateUniqueID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import com.google.protobuf.ByteString;
import A7.client.UDPClient;
import A7.core.KeyValueStoreSingleton;
import A7.core.VersionedValue;
import A7.proto.KeyValueRequest.KVRequest;
import A7.proto.Message.Msg;
import A7.utils.MsgWrapper;

public class SendReplication implements Runnable {    
	MsgWrapper sendLocation;
	
	public SendReplication(MsgWrapper received) {
		this.sendLocation = received;
	}
	
	private ByteString createSubMap(int from, int to) throws IOException{
		// Populate new hashMap from ranges provided
		ConcurrentHashMap<ByteString, VersionedValue> newMap = new ConcurrentHashMap<ByteString, VersionedValue>();
		Object[] keySet = KeyValueStoreSingleton.getInstance().getMap().keySet().toArray();
		for(int i=from ; i < to; i++){
			newMap.put( (ByteString) keySet[i], 
				KeyValueStoreSingleton.getInstance().getMap().get(keySet[i]));
		}
		// Nothing in range to serialize; return null
		// (ie. do not serialize the HashMap object itself if there are no keys inside)
		if(newMap.size() == 0){
			return null;
		}
		// Write object to ByteString
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(newMap);
        out.flush();
        return ByteString.copyFrom(bos.toByteArray());
	}
	
	private void sendDupRequestMsg(ByteString value){
		KVRequest dupeKVReq= KVRequest.newBuilder()
			.setCommand(8)
			.setValue(value)
			.build();

		byte[] messageID = new byte[0];
		try {
			messageID = generateUniqueID();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		ByteString payload = dupeKVReq.toByteString();
		
		Msg dupeMsg = Msg.newBuilder()
			.setMessageID(ByteString.copyFrom(messageID))
			.setPayload(payload)
			.setCheckSum(calculateProtocolBufferChecksum(payload,
		        ByteString.copyFrom(messageID)))
			.build();

		// TODO: decide if we want retries based on response
		// Note: currently, UDPClient handles retries and blocks waiting for response
		try {
			UDPClient.sendProtocolBufferRequest(
					dupeMsg.toByteArray(),
                sendLocation.getAddress().getHostAddress(),
                sendLocation.getPort(),
                messageID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void serveReplication(int from, int mid, int to){
		try {
			ByteString headPayload;
				headPayload = createSubMap(from, mid);
			if(headPayload.size() > MAX_REP_PAYLOAD_SIZE){
				int midKey = (mid - from)/2;
				serveReplication(from, midKey, mid);
			}else if(headPayload != null && headPayload.size() != 0){
	
				sendDupRequestMsg(headPayload);
			}
			
			ByteString tailPayload = createSubMap(mid, to);
			if(tailPayload.size() > MAX_REP_PAYLOAD_SIZE){
				int midKey = (to - mid)/2;
				serveReplication(mid, midKey, to);
			}else if(headPayload != null && headPayload.size() != 0){
				sendDupRequestMsg(tailPayload);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
    @Override
    public void run() {
    	Object[] keySet = KeyValueStoreSingleton.getInstance().getMap().keySet().toArray();
    	int from = 0;
    	int mid = keySet.length;
    	int to = keySet.length;
    	
    	serveReplication(from, mid, to);
    }	
}
