package etherip.logix5000;

import java.nio.ByteBuffer;

import etherip.protocol.ProtocolAdapter;

/**
 * Protocol body to read template definition (members names and type) from the Logix 5000.
 *
 * @author Samuel Pelletier
 */
public class CIPReadTemplateDefinitionProtocol extends ProtocolAdapter
{
	private short sizeToRead;
	private int startOffset;
	private byte[] readBytes;

    /**
     * Create a read protocol message that requests a single element
     */
    public CIPReadTemplateDefinitionProtocol(short sizeToRead, int startOffset)
    {
		this.sizeToRead = sizeToRead;
		this.startOffset = startOffset;
    }

    @Override
    public int getRequestSize()
    {
        return 6;
    }

    @Override
    public void encode(final ByteBuffer buf, final StringBuilder log)
    {
        buf.putInt(startOffset); 
        buf.putShort((short) (sizeToRead-startOffset));
        if (log != null)
        {
            log.append("UINT startOffset        : ").append(startOffset).append("\n");
            log.append("USINT sizeToRead        : ").append(sizeToRead).append("\n");
        }
    }

    @Override
    public void decode(final ByteBuffer buf, final int available,
    		final StringBuilder log) throws Exception
    {
    	readBytes = new byte[buf.remaining()];
    	buf.get(readBytes);

    	if (log != null)
    	{
    		log.append(readBytes.length).append(" Bytes read.\n");
    	}
    }

    final public byte[] getBytes() {
		return readBytes;
    }
}
