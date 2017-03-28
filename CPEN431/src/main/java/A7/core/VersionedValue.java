package A7.core;

import com.google.protobuf.ByteString;

public class VersionedValue {
	private int version;
	private ByteString value;
	
	public VersionedValue(ByteString value,  int version) {
		this.version = version;
    		this.value = value;
	}
	
	public int getVersion() {
		return this.version;
	}

	public ByteString getValue() {
		return this.value;
	}	
}
