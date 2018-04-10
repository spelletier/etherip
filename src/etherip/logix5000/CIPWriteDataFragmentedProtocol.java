package etherip.logix5000;

import java.nio.ByteBuffer;

import etherip.protocol.ProtocolAdapter;

/**
 * Protocol body for Logix 5000 write fragmented protocol
 *
 * @author Samuel Pelletier
 */
public class CIPWriteDataFragmentedProtocol extends ProtocolAdapter
{
	final private Type type;
	final private short elementCount;
	private int offset;
    final private byte[] data;

    public CIPWriteDataFragmentedProtocol(final Type type, short elementCount, int offset, byte[] data)
    {
    	this.type = type;
		this.elementCount = elementCount;
		this.offset = offset;
        this.data = data;
    }

    @Override
    public int getRequestSize()
    {
        return type.encodedTypeSize() + 6 + this.data.length; // type + count + offset + data
    }

    @Override
    public void encode(final ByteBuffer buffer, final StringBuilder log)
            throws Exception
    {
        this.type.encodeType(buffer);
        buffer.putShort(elementCount);
        buffer.putInt(offset);
        buffer.put(data);
        if (log != null)
        {
            log.append("USINT type, data        : ").append(this.data)
                    .append("\n");
        }
    }
}
