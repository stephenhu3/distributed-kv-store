package A2.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class Checksum {
    public static long calculateProtocolBufferChecksum(byte[] messageID, byte[] payload) {
        CRC32 crc32 = new CRC32();
        ByteBuffer byteBuffer = ByteBuffer.allocate(messageID.length + payload.length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(messageID);
        byteBuffer.put(payload);
        crc32.update(byteBuffer.array());
        return crc32.getValue();
    }
}
