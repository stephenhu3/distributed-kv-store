package A4.utils;

import A4.proto.Message.Msg;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MsgWrapper {
    private Msg message;
    private int port;
    private InetAddress address;

    public MsgWrapper(Msg message, InetAddress address, int port) {
        this.message = message;
        this.port = port;
        this.address = address;
    }
    
    //Create MsgWrapper around messages forwarded
    public MsgWrapper(Msg requestMessage) {
    	this.message = null;
        this.port = 0;
        this.address = null;
    	if(requestMessage.hasFwdAddress() && requestMessage.hasFwdPort() ){
	        try {
	        	this.message = null;
		        this.port = requestMessage.getFwdPort();
		        this.address = InetAddress.getByAddress(requestMessage.getFwdAddress().toByteArray());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public Msg getMessage() {
        return message;
    }

    public void setMessage(Msg message) {
        this.message = message;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
