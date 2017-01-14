package A2.resources;

import com.google.protobuf.InvalidProtocolBufferException;

import A2.proto.ResponsePayload.ResPayload;

import static A2.utils.ByteRepresentation.bytesToHex;

public class ProtocolBufferStudentNumberResponse {
    public static void parseResponse(byte[] response) {
        ResPayload resPayload = null;

        // deserialize response payload
        try {
            resPayload = ResPayload.parseFrom(response);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        String secretCodeHexString = bytesToHex(resPayload.getSecretKey().toByteArray());
        System.out.println("Secret code length: " + secretCodeHexString.length());
        System.out.println("Secret: " + secretCodeHexString);
    }
}
