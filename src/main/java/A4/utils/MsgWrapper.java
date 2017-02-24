package A4.utils;

import A4.proto.Message.Msg;
import java.net.InetAddress;

public class MsgWrapper {
    private Msg message;
    private int port;
    private InetAddress address;

    public MsgWrapper(Msg message, InetAddress address, int port) {
        this.message = message;
        this.port = port;
        this.address = address;
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
