package A3.utils;

import A3.proto.Message.Msg;
import com.google.protobuf.ByteString;

public class ProtocolBuffers {
    public static Msg wrapMessage(ByteString messageID, ByteString payload) {
        Msg.Builder msg = Msg.newBuilder();
        msg.setMessageID(messageID);
        msg.setPayload(payload);
        msg.setCheckSum(Checksum.calculateProtocolBufferChecksum(messageID, payload));
        return msg.build();
    }
}
