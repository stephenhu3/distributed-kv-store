package A3.utils;

import A3.proto.Message.Msg;
import com.google.protobuf.ByteString;

public class ProtocolBuffers {
    public static Msg wrapMessage(byte[] messageID, byte[] payload) {
        Msg.Builder msg = Msg.newBuilder();
        msg.setMessageID(ByteString.copyFrom(messageID));
        msg.setPayload(ByteString.copyFrom(payload));
        msg.setCheckSum(Checksum.calculateProtocolBufferChecksum(messageID, payload));
        return msg.build();
    }
}
