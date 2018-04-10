package etherip.logix5000;

import java.nio.ByteBuffer;

import etherip.protocol.ProtocolAdapter;

/**
 * Protocol body for Logix 5000 read fragmented protocol
 *
 * @author Samuel Pelletier
 */
public class CIPReadDataFragmentedProtocol extends ProtocolAdapter
{
    private byte[] data;
    private final short count;
	private int offset;
	private short dataType;
	private short templateCrcValue;

    /**
     * Create a read protocol message that requests a single element
     */
    public CIPReadDataFragmentedProtocol()
    {
        this.count = 1;
        this.offset = 0;
    }

    /**
     * Create a read protocol message that requests one or more elements if request is an array
     *
     * @param count
     */
    public CIPReadDataFragmentedProtocol(final short count)
    {
        this.count = count;
        this.offset = 0;
    }

    public CIPReadDataFragmentedProtocol(final short count, final int offset)
    {
        this.count = count;
        this.offset = offset;
    }

    @Override
    public int getRequestSize()
    {
        return 6;
    }

    @Override
    public void encode(final ByteBuffer buf, final StringBuilder log)
    {
        buf.putShort(this.count); // elements
        buf.putInt(this.offset);
        if (log != null)
        {
            log.append("USINT elements          : "+this.count+"\n");
            log.append("UINT offset             : "+this.offset+"\n");
        }
    }

    @Override
    public void decode(final ByteBuffer buf, int available,
            final StringBuilder log) throws Exception
    {
        if (available <= 0)
        {
            this.data = null;
            if (log != null)
            {
                log.append("USINT type, data        : - nothing-\n");
            }
            return;
        }
        dataType = buf.getShort();
        available -= 2;
        if (dataType == Type.StructTypeCode) {
        	templateCrcValue = buf.getShort();
            available -= 2;
        }
		data = new byte[available];
        buf.get(data);
        if (log != null)
        {
            log.append("USINT type, data       ").append(dataType).append("; ").append(available).append(" bytes read.\n");
        }
    }
    
    final public byte[] getData()
    {
        return this.data;
    }
    
    final public short getTemplateCrcValue() {
    	return templateCrcValue;
    }
}
