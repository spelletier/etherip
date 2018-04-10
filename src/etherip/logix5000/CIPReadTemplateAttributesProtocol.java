package etherip.logix5000;

import java.nio.ByteBuffer;

import etherip.protocol.ProtocolAdapter;

/**
 * Protocol body to read template attributes from the Logix 5000.
 *
 * @author Samuel Pelletier
 */
public class CIPReadTemplateAttributesProtocol extends ProtocolAdapter
{
	private Controller controller;
	private short numberOfMembers;
	private int structureSize;
	private int objectSize;
	private short crcValue;

    /**
     * Create a read protocol message that requests a single element
     */
    public CIPReadTemplateAttributesProtocol(Controller controller)
    {
		this.controller = controller;
    }

    @Override
    public int getRequestSize()
    {
        return 10;
    }

    @Override
    public void encode(final ByteBuffer buf, final StringBuilder log)
    {
        buf.putShort((short) 4); // 4 attributes
        buf.putShort((short) 4); // Object definition size in words
        buf.putShort((short) 5); // Structure size in bytes
        buf.putShort((short) 2); // Member count
        buf.putShort((short) 1); // Structure handle (crc value)
        if (log != null)
        {
            log.append("USINT numberOfAttributes: 4").append("\n");
            log.append("USINT def size in words : 4").append("\n");
            log.append("USINT Struct size bytes : 5").append("\n");
            log.append("USINT Member count      : 2").append("\n");
            log.append("USINT Structure handle  : 1").append("\n");
        }
    }

    @Override
    public void decode(final ByteBuffer buf, final int available,
    		final StringBuilder log) throws Exception
    {
    	short receivedCount = buf.getShort();
		if (receivedCount != 4) {
    		throw new Exception("Expecting 4 attributes, received "+receivedCount);
    	}
		
    	short attributeNumber = buf.getShort();
    	short attributeStatus = buf.getShort();
		if (attributeNumber != 4 || attributeStatus != 0) {
    		throw new Exception("Expecting attribute 4, received "+attributeNumber+" with status "+attributeStatus);
    	}
		objectSize = buf.getInt();

		attributeNumber = buf.getShort();
    	attributeStatus = buf.getShort();
		if (attributeNumber != 5 || attributeStatus != 0) {
    		throw new Exception("Expecting attribute 5, received "+attributeNumber+" with status "+attributeStatus);
    	}
		structureSize = buf.getInt();
		
		attributeNumber = buf.getShort();
    	attributeStatus = buf.getShort();
		if (attributeNumber != 2 || attributeStatus != 0) {
    		throw new Exception("Expecting attribute 2, received "+attributeNumber+" with status "+attributeStatus);
    	}
		numberOfMembers = buf.getShort();

		attributeNumber = buf.getShort();
    	attributeStatus = buf.getShort();
		if (attributeNumber != 1 || attributeStatus != 0) {
    		throw new Exception("Expecting attribute 1, received "+attributeNumber+" with status "+attributeStatus);
    	}
		crcValue = buf.getShort();

    	if (log != null)
    	{
    	}
    }

    final public Template createTemplate(short instanceId)
    {
		return new Template(controller, instanceId, objectSize, structureSize, numberOfMembers, crcValue);
    }
}
