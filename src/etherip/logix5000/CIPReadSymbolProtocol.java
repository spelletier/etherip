package etherip.logix5000;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import etherip.protocol.ProtocolAdapter;

/**
 * Protocol body to read symbol list from the Logix 5000.
 *
 * @author Samuel Pelletier
 */
public class CIPReadSymbolProtocol extends ProtocolAdapter
{
    private List<Symbol> symbols = new ArrayList<>();
	private Controller controller;

    /**
     * Create a read protocol message that requests a single element
     */
    public CIPReadSymbolProtocol(Controller controller)
    {
		this.controller = controller;
    }

    @Override
    public int getRequestSize()
    {
        return 8;
    }

    @Override
    public void encode(final ByteBuffer buf, final StringBuilder log)
    {
        buf.putShort((short) 3); // 2 attributes
        buf.putShort((short) 1); // Symbol name
        buf.putShort((short) 2); // Symbol type
        buf.putShort((short) 8); // Array size
        if (log != null)
        {
            log.append("USINT numberOfElements  : 2").append("\n");
            log.append("USINT Symbol name       : 1").append("\n");
            log.append("USINT Symbol type       : 2").append("\n");
            log.append("USINT Array size        : 8").append("\n");
        }
    }

    @Override
    public void decode(final ByteBuffer buf, final int available,
    		final StringBuilder log) throws Exception
    {
    	while (buf.hasRemaining()) {
    		int instanceId = buf.getInt();
    		int nameLength = buf.getShort();
    		byte[] name = new byte[nameLength];
    		buf.get(name);
    		short type = buf.getShort();
    		int arraySize[] = new int[3];
    		arraySize[0] = buf.getInt();
    		arraySize[1] = buf.getInt();
    		arraySize[2] = buf.getInt();

    		Symbol symbol = new Symbol(controller, instanceId, new String(name, StandardCharsets.ISO_8859_1), type, arraySize);
    		symbols.add(symbol);
    	}

    	if (log != null)
    	{
    		log.append(symbols.size()).append(" symbols read.\n");
    	}
    }

    final public List<Symbol> getSymbols()
    {
        return this.symbols;
    }
}
