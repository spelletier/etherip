package etherip.logix5000;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * A controller type, either a basic atomic type (SINT, INT, ...) a String or a template (for struct).
 *
 * @author Samuel Pelletier
 */
public interface Type {
	public final static short StructTypeCode = 0x02A0; 
	public short code();

	public int encodedTypeSize();
	public int encodedDataSize();
	public void encodeType(ByteBuffer buffer);
	public void encode(Object value, List<byte[]> dataParts);
	public Object decode(ByteBuffer buffer);
}
