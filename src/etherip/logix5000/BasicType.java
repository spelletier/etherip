package etherip.logix5000;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import etherip.protocol.Connection;

public enum BasicType implements Type {
	BOOL (0x00C1, 0),
	BOOL1 (0x01C1, 1),
	BOOL2 (0x02C1, 2),
	BOOL3 (0x03C1, 3),
	BOOL4 (0x04C1, 4),
	BOOL5 (0x05C1, 5),
	BOOL6 (0x06C1, 6),
	BOOL7 (0x07C1, 7),
	SINT (0x00C2, 0),
	INT  (0x00C3, 0),
	DINT (0x00C4, 0),
	LINT (0x00C5, 0),
	REAL (0x00CA, 0),
	BITS (0x00C1, 0),
	DWORD(0x00D3, 0),
	STRING(0x0FCE, 0);

	
private static final int StandardStringStructureSize = 88;
private static final byte[] StringTypeBytes = new byte[] {(byte) 0xA0, 0x02, (byte) 0xCE, 0x0F};

	private short code;
	private byte bitPosition;
	    
    private BasicType(int code, int byteLength) {
		this.code = (short) code;
    	this.bitPosition = (byte) bitPosition;
    }

	@Override
	public short code() {
		return code;
	}

	@Override
	public int encodedTypeSize() {
		if (this == STRING) {
			return 4;
		}
		return 2;
	}

	@Override
	public int encodedDataSize() {
		switch (this) {
		case BOOL:
		case BOOL1:
		case BOOL2:
		case BOOL3:
		case BOOL4:
		case BOOL5:
		case BOOL6:
		case BOOL7:
		case SINT:
			return 1;
		case INT:
			return 2;
		case DINT:
		case DWORD:
		case BITS:
		case REAL:
			return 4;
		case LINT:
			return 8;
		case STRING:
			return StandardStringStructureSize;
		}
		throw new RuntimeException("Undefined data size for BasicType: "+this);
	}

	@Override
	public void encodeType(ByteBuffer buffer) {
		if (this == BasicType.STRING) {
			buffer.put(StringTypeBytes);
		}
		else {
			buffer.putShort(code);
		}
	}

	@Override
	public void encode(Object value, List<byte[]> dataParts) {
		ByteBuffer buffer = ByteBuffer.allocate(encodedDataSize());
		buffer.order(Connection.BYTE_ORDER);
		switch (this) {
		case BOOL:
		case BOOL1:
		case BOOL2:
		case BOOL3:
		case BOOL4:
		case BOOL5:
		case BOOL6:
		case BOOL7:
			int currentPosition = buffer.position();
			byte currentByte = buffer.get();
			if ((boolean)value) {
				byte mask = (byte) (0x01 << bitPosition);
				currentByte = (byte) (currentByte | mask);
			}
			else {
				byte mask = (byte) (0xFE << bitPosition);
				currentByte = (byte) (currentByte & mask);
			}
			buffer.put(currentPosition, currentByte);
			break;
		case SINT:
			buffer.put((byte)value);
			break;
		case INT:
			buffer.putShort((short) value);
			break;
		case DINT:
		case DWORD:
		case BITS:
			buffer.putInt((int) value);
			break;
		case REAL:
			buffer.putFloat((float) value);
			break;
		case LINT:
			buffer.putLong((long) value);
			break;
		case STRING:
			String stringValue = (String)value;
			if (stringValue.length() > StandardStringStructureSize) {
				throw new RuntimeException("Trying to encode a string with more than 82 chars: "+stringValue);
			}
			buffer.putInt(stringValue.length());
			buffer.put(stringValue.getBytes(StandardCharsets.US_ASCII));
			int paddingLength = StandardStringStructureSize - 4 - stringValue.length();
			buffer.put(new byte[paddingLength]);
			break;
		default:
			throw new RuntimeException("Unhandled encoding of BasicType: "+this);
		}
		dataParts.add(buffer.array());
	}

	@Override
	public Object decode(ByteBuffer buffer) {
		switch (this) {
		case BOOL:
		case BOOL1:
		case BOOL2:
		case BOOL3:
		case BOOL4:
		case BOOL5:
		case BOOL6:
		case BOOL7:
			byte currentByte =  buffer.get();
			return (currentByte >> bitPosition & 0x01) == 0x01;
		case SINT:
			return buffer.get();
		case INT:
			return buffer.getShort();
		case DINT:
		case DWORD:
		case BITS:
			return buffer.getInt();
		case REAL:
			return buffer.getFloat();
		case LINT:
			return buffer.getLong();
		case STRING:
			int stringLength = buffer.getInt();
			byte[] stringBytes = new byte[stringLength];
			buffer.get(stringBytes);
			int paddingLength = StandardStringStructureSize - 4 - stringLength;
			buffer.get(new byte[paddingLength]);
			return new String(stringBytes, StandardCharsets.US_ASCII);
		default:
			throw new RuntimeException("Unhandled decoding of BasicType: "+this);
		}
	}    
}
