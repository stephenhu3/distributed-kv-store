package A7.core;

import java.io.Serializable;

import com.google.protobuf.ByteString;

public class VersionedValue implements Serializable {
	private int version;
	private ByteString value;
	
	public VersionedValue(ByteString value, int version) {
		this.version = version;
		this.value = value;
	}
	
	public int getVersion() {
		return this.version;
	}

	public ByteString getValue() {
		return this.value;
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !VersionedValue.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final VersionedValue other = (VersionedValue) obj;
        // logical OR, if one or the other is null, they are not equal
        if (this.getValue() == null ^ other.getValue() == null) {
            return false;
        }

        if (this.getValue() != null && !this.getValue().equals(other.getValue())) {
            return false;
        }

        if (this.getVersion() != other.getVersion()) {
            return false;
        }

        return true;
    }
}
