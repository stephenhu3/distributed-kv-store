package A2.resources;

import com.google.protobuf.ByteString;

import java.security.NoSuchAlgorithmException;

import A2.proto.Message.Msg;
import A2.proto.RequestPayload.ReqPayload;
import A2.utils.Checksum;

import static A2.utils.UniqueIdentifier.generateUniqueID;

public class ProtocolBufferStudentNumberRequest {
    public static Msg generateRequest(int snum, byte[] uniqueID) throws NoSuchAlgorithmException {
        ReqPayload.Builder reqPayload = ReqPayload.newBuilder();
        reqPayload.setStudentID(snum);

        Msg.Builder msg = Msg.newBuilder();

        byte[] messageID = generateUniqueID();
        byte[] payload = reqPayload.build().toByteArray();

        msg.setMessageID(ByteString.copyFrom(messageID));
        msg.setPayload(ByteString.copyFrom(payload));
        msg.setCheckSum(Checksum.calculateProtocolBufferChecksum(messageID, payload));

        return msg.build();
    }
}
