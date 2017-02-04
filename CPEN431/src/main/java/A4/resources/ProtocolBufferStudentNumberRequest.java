package A4.resources;

import com.google.protobuf.ByteString;

import java.security.NoSuchAlgorithmException;

import A4.proto.Message.Msg;
import A4.proto.RequestPayload.ReqPayload;
import A4.utils.Checksum;

public class ProtocolBufferStudentNumberRequest {
    public static byte[] generateRequest(int snum, byte[] messageID) throws NoSuchAlgorithmException {
        ReqPayload.Builder reqPayload = ReqPayload.newBuilder();
        reqPayload.setStudentID(snum);

        Msg.Builder msg = Msg.newBuilder();

        byte[] payload = reqPayload.build().toByteArray();

        msg.setMessageID(ByteString.copyFrom(messageID));
        msg.setPayload(ByteString.copyFrom(payload));
        msg.setCheckSum(Checksum.calculateProtocolBufferChecksum(messageID, payload));

        return msg.build().toByteArray();
    }
}
