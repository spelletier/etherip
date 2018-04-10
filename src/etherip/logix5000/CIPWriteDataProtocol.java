package etherip.logix5000;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import etherip.protocol.ProtocolAdapter;
import etherip.types.CNService;

/**
 * Protocol body for {@link CNService#CIP_WriteData} that use the Logix 5000 types and values.
 *
 * @author Samuel Pelletier
 */
public class CIPWriteDataProtocol extends ProtocolAdapter
{
	final private Type type;
	final private short elementCount;
    final private byte[] data;

    public CIPWriteDataProtocol(final Type type, short elementCount, List<byte[]> dataParts)
    {
    	this.type = type;
		this.elementCount = elementCount;
		
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			for (byte[] part : dataParts) {
				dataStream.write(part);
			}
	        this.data = dataStream.toByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @Override
    public int getRequestSize()
    {
        return type.encodedTypeSize() + 2 + this.data.length; // type + count + data
    }

    @Override
    public void encode(final ByteBuffer buffer, final StringBuilder log)
            throws Exception
    {
        this.type.encodeType(buffer);
        buffer.putShort(elementCount);
        buffer.put(data);
        if (log != null)
        {
            log.append("USINT type, data        : ").append(this.data)
                    .append("\n");
        }
    }
}
