package A2.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class Checksum {
    public static long calculateProtocolBufferChecksum(byte[] messageID, byte[] payload) {
        CRC32 crc32 = new CRC32();
        ByteBuffer byteBuffer = ByteBuffer.allocate(messageID.length + payload.length);
        // messageID and payload in little-endian, then concatenated
        ByteBuffer messageIDBuffer = ByteBuffer.allocate(messageID.length);
        ByteBuffer payloadBuffer = ByteBuffer.allocate(payload.length);
        messageIDBuffer.put(messageID);
        messageIDBuffer.order(ByteOrder.LITTLE_ENDIAN);
        payloadBuffer.put(payload);
        payloadBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(messageIDBuffer.array());
        byteBuffer.put(payloadBuffer.array());
        crc32.update(byteBuffer.array());
        return crc32.getValue();
    }
}
