package A7.server;

import static A7.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A7.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A7.DistributedSystemConfiguration.UDP_SERVER_THREAD_POOL_NTHREADS;
import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;
import static A7.utils.UniqueIdentifier.generateUniqueID;

import A7.client.UDPClient;
import A7.core.ConsistentHashRing;
import A7.core.RequestCache;
import A7.proto.KeyValueRequest.KVRequest;
import A7.proto.Message.Msg;
import A7.resources.ProtocolBufferKeyValueStoreResponse;
import A7.utils.MsgWrapper;
import A7.utils.ProtocolBuffers;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class UDPServerThreadPool {
    private static UDPServerThreadPool instance = new UDPServerThreadPool();
    private static DatagramSocket socket;
    private static DatagramSocket sendSocket;

    protected static ThreadPoolExecutor executor =
		(ThreadPoolExecutor) Executors.newFixedThreadPool(UDP_SERVER_THREAD_POOL_NTHREADS);

    public static InetAddress localAddress;
    public static int localPort;

    private UDPServerThreadPool(){}

    public static void initialize(int port) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(port);
        sendSocket = new DatagramSocket(new Random().nextInt(10000));
        localAddress = InetAddress.getLocalHost();
        localPort = port;
    }

    public static UDPServerThreadPool getInstance() {
        return instance;
    }

    // process incoming requests
    public void receive() {
		while (true) {
            if (SHUTDOWN_NODE) {
                socket.close();
                sendSocket.close();
                System.exit(0);
            }
            byte[] buf = new byte[MAX_MSG_SIZE];
            
            // receive request
            DatagramPacket reqPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(reqPacket);
                executor.execute(new ReceiverWorker(reqPacket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	// duplicate request to two successors for replication
	public void replicate(KVRequest request) {
        executor.execute(new ReplicaWorker(request));
	}

	class ReceiverWorker implements Runnable {
		DatagramPacket reqPacket;

		ReceiverWorker(DatagramPacket received) throws IOException{
	    	this.reqPacket = received;
	    }

	    @Override
	    public void run() {
	    	Msg request = null;
			try {
				request = Msg.parseFrom(
			        Arrays.copyOf(reqPacket.getData(), reqPacket.getLength()));
			} catch (InvalidProtocolBufferException e) {
			    e.printStackTrace();
			}

			if (VERBOSE > 0) {
			    System.out
			        .println("Available Memory (bytes): " + Runtime.getRuntime().freeMemory());
			}

			ByteString currentID = request.getMessageID();
			byte[] payload = request.getPayload().toByteArray();

			// verify checksum
			if (request != null) {
			    if (request.getCheckSum() != calculateProtocolBufferChecksum(currentID.toByteArray(),
			        payload)) {
			        System.out.format("Invalid checksum detected in the response, retrying...\n");
			        // TODO: Return self-defined error code
			        return;
			    }

				// begin retrieval
			    InetAddress requestAddress = reqPacket.getAddress();
				int requestPort = reqPacket.getPort();

				MsgWrapper responseMsg;
			    MsgWrapper cached = RequestCache.getInstance().getCache().getIfPresent(currentID);

			    if (cached == null) {
			    	MsgWrapper messageWrap =
						ProtocolBufferKeyValueStoreResponse.serveRequest(request);
					if (messageWrap != null && (messageWrap.getPort() == 0
						|| messageWrap.getAddress() == null)) {
						if (request.hasFwdPort() && request.hasFwdAddress()) {
			                try {
			                	messageWrap.setAddress(
			                		InetAddress.getByName(request.getFwdAddress().toStringUtf8()));
			                	messageWrap.setPort(request.getFwdPort());
			                } catch (UnknownHostException e) {
			                    e.printStackTrace();
			                }
						} else {
							messageWrap.setAddress(requestAddress);
							messageWrap.setPort(requestPort);
						}
			        } else {
			        	messageWrap.setMessage(
							ProtocolBuffers.wrapFwdMessage(
                            request,
                            ByteString.copyFromUtf8(requestAddress.getHostAddress()),
                            requestPort));
			        }

					responseMsg = messageWrap;
					RequestCache.getInstance().getCache().put(currentID, messageWrap);
			    } else {
			    	responseMsg = cached;
			    }

				byte[] responseData = responseMsg.getMessage().toByteArray();
				DatagramPacket responsePacket = new DatagramPacket(
						responseData, responseData.length,
						responseMsg.getAddress(), responseMsg.getPort());
    	        try {
    	        	// address edge case for two threads trying to send response at same time
    	        	synchronized(sendSocket) {
						sendSocket.send(responsePacket);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	}

	class ReplicaWorker implements Runnable {
		KVRequest request;

		ReplicaWorker(KVRequest request) {
			this.request = request;
		}

		@Override
		public void run() {
			String originKey = null;
			String firstSuccessorKey = null;
            String secondSuccessorKey = null;

            try {
                originKey = ConsistentHashRing.getInstance().getKey(request.getKey());
                firstSuccessorKey = ConsistentHashRing.getInstance().getSuccessorKey(originKey);
                secondSuccessorKey = ConsistentHashRing.getInstance()
                    .getSuccessorKey(firstSuccessorKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            KVRequest replicateKVReq = KVRequest.newBuilder()
				.setCommand(request.getCommand())
				.setKey(request.getKey())
				.setValue(request.getValue())
				.setNotReplicated(true)
				.build();

            byte[] messageID = new byte[0];

            try {
                messageID = generateUniqueID();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            ByteString payload = replicateKVReq.toByteString();
            ByteString messageIDByteString = ByteString.copyFrom(messageID);

			Msg replicateMsg = Msg.newBuilder()
				.setMessageID(messageIDByteString)
				.setPayload(payload)
				.setCheckSum(calculateProtocolBufferChecksum(messageIDByteString, payload))
				.build();

			MsgWrapper firstSuccessorNode = ConsistentHashRing.getInstance()
				.getHashRing().get(firstSuccessorKey);
			MsgWrapper secondSuccessorNode = ConsistentHashRing.getInstance()
				.getHashRing().get(secondSuccessorKey);

			// send out replica requests to two successors optimistically, doesn't wait for response
			try {
				UDPClient.sendReplicaRequest(
                    replicateMsg.toByteArray(),
                    firstSuccessorNode.getAddress().getHostAddress(),
                    firstSuccessorNode.getPort());

                UDPClient.sendReplicaRequest(
                    replicateMsg.toByteArray(),
                    secondSuccessorNode.getAddress().getHostAddress(),
                    secondSuccessorNode.getPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}
