package A7.utils;

import A7.proto.Message.Msg;
import com.google.protobuf.ByteString;

public class ProtocolBuffers {
    public static Msg wrapMessage(ByteString messageID, ByteString payload) {
        Msg.Builder msg = Msg.newBuilder();
        msg.setMessageID(messageID);
        msg.setPayload(payload);
        msg.setCheckSum(Checksum.calculateProtocolBufferChecksum(messageID, payload));
        return msg.build();
    }

    public static Msg wrapFwdMessage(Msg msgBase, ByteString FwdAddress, int FwdPort) {
        Msg.Builder msg = Msg.newBuilder();
        msg.setMessageID(msgBase.getMessageID());
        msg.setPayload(msgBase.getPayload());
        msg.setCheckSum(Checksum.calculateProtocolBufferChecksum
            (msgBase.getMessageID(), msgBase.getPayload()));
        msg.setFwdAddress(FwdAddress);
        msg.setFwdPort(FwdPort);
        return msg.build();
    }
}
