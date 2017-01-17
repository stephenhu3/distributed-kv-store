package A3.resources;

import static A3.utils.ByteRepresentation.bytesToHex;

import A3.proto.ResponsePayload.ResPayload;
import com.google.protobuf.InvalidProtocolBufferException;

public class ProtocolBufferStudentNumberResponse {
    public static void parseResponse(byte[] response) {
        ResPayload resPayload = null;

        // deserialize response payload
        try {
            resPayload = ResPayload.parseFrom(response);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        System.out.println("Secret code length: " + resPayload.getSecretKey().size());
        System.out.println("Secret: " + bytesToHex(resPayload.getSecretKey().toByteArray()));
    }
}
