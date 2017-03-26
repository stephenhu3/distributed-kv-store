package A7.server;

import static A7.DistributedSystemConfiguration.MAX_MSG_SIZE;
import static A7.DistributedSystemConfiguration.SHUTDOWN_NODE;
import static A7.DistributedSystemConfiguration.UDP_SERVER_THREAD_POOL_NTHREADS;
import static A7.DistributedSystemConfiguration.VERBOSE;
import static A7.utils.Checksum.calculateProtocolBufferChecksum;

import A7.core.RequestCache;
import A7.resources.ProtocolBufferKeyValueStoreResponse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import A7.proto.Message.Msg;
import A7.utils.MsgWrapper;
import A7.utils.ProtocolBuffers;

public class UDPServerThreadPool {
	public static InetAddress localAddress;
	public static int localPort;
	private static ThreadPoolExecutor executor =
		(ThreadPoolExecutor) Executors.newFixedThreadPool(UDP_SERVER_THREAD_POOL_NTHREADS);
    private static DatagramSocket socket;
    private static DatagramSocket sendSocket;

    public UDPServerThreadPool(int port) throws IOException {
        socket = new DatagramSocket(port);
        sendSocket = new DatagramSocket(localPort+ new Random().nextInt(10000));
        localAddress = InetAddress.getLocalHost();
        localPort = port;
    }
    
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
			        			ProtocolBuffers.wrapFwdMessage(request,
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
}
