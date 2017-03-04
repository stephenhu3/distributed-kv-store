package A4.utils;

import A4.proto.Message.Msg;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MsgWrapper {
    private Msg message;
    private int port;
    private InetAddress address;
    private boolean forward = false;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !MsgWrapper.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final MsgWrapper other = (MsgWrapper) obj;
        // logical OR, if one or the other is null, they are not equal
        if (this.getMessage() == null ^ other.getMessage() == null) {
            return false;
        }

        if (this.getMessage() != null && !this.getMessage().equals(other.getMessage())) {
            return false;
        }

        if (this.getPort() != other.getPort()) {
            return false;
        }

        if (this.getAddress() == null ^ other.getAddress() == null) {
            return false;
        }

        if (this.getAddress() != null && !this.getAddress().equals(other.getAddress())) {
            return false;
        }

        return true;
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

    public boolean getForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }
}
