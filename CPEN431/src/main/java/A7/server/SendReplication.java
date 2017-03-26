package A7.server;

import static A7.DistributedSystemConfiguration.CLIENT_TARGET_PORT;
import static A7.DistributedSystemConfiguration.MAX_REP_PAYLOAD_SIZE;
import static A7.DistributedSystemConfiguration.REP_FACTOR;
import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.Random;
import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import A7.core.KeyValueStoreSingleton;
import A7.core.RequestCache;
import A7.proto.LiveHostsRequest.LiveHostsReq;
import A7.proto.Message.Msg;
import A7.resources.ProtocolBufferKeyValueStoreResponse;
import A7.server.UDPServerThreadPool.ReceiverWorker;
import A7.utils.ByteRepresentation;
import A7.utils.MsgWrapper;
import A7.utils.ProtocolBuffers;

public class SendReplication implements Runnable {    
	InetAddress sendLocation;
	DatagramSocket sendSocket;
	
	public SendReplication(InetAddress received) throws SocketException{
		this.sendLocation = received;
		sendSocket = new DatagramSocket();
	}
	
	//Builds the application level payload for replication message
	private ByteString buildPayload(ByteString from, ByteString to, boolean include) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(KeyValueStoreSingleton.getInstance().getMap().subMap(from, true, to, include));
        out.flush();
        ByteString repPayload = ByteString.copyFrom(bos.toByteArray());
        return repPayload;   
	}
	
	private void serveReplication(ByteString from, ByteString mid, ByteString to){
		ByteString headMapPayload = buildPayload(from, mid, false);
		if(headMapPayload.size() > MAX_REP_PAYLOAD_SIZE){
			Object[] headMap = KeyValueStoreSingleton.getInstance().getMap().headMap(mid, false).keySet().toArray();
			ByteString midKey =  (ByteString) headMap[(int) headMap.length/2];
			serveReplication(from, midKey, mid);
		}else if(headMapPayload.size() > 0){
			Msg msg = generateMessage();
			sendOut(msg);
		}
		
		ByteString tailMapPayload = buildPayload(mid, to, true);
		if(tailMapPayload.size() > MAX_REP_PAYLOAD_SIZE){
			Object[] headMap = KeyValueStoreSingleton.getInstance().getMap().tailMap(mid, true).keySet().toArray();
			ByteString midKey =  (ByteString) headMap[(int) headMap.length/2];
			serveReplication(mid, midKey, to);
		}else if(tailMapPayload.size() > 0){
			Msg msg = generateMessage();
			sendOut(msg);
		}
	}

	private void sendOut(Msg responseMsg){
		byte[] responseData = responseMsg.toByteArray();
		DatagramPacket responsePacket = new DatagramPacket(
			responseData, responseData.length,
			sendLocation, CLIENT_TARGET_PORT);
        try {
        	// address edge case for two threads trying to send response at same time
        	synchronized(sendSocket) {
				sendSocket.send(responsePacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void run() {
    	ByteString from = KeyValueStoreSingleton.getInstance().getMap().firstKey();
    	ByteString to = KeyValueStoreSingleton.getInstance().getMap().lastKey();
    	ByteString mid = KeyValueStoreSingleton.getInstance().getMap().lastKey();
    	
    	serveReplication(from, mid, to);
    }	
}
