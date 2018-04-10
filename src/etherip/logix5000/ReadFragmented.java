package etherip.logix5000;

import java.io.ByteArrayOutputStream;

import etherip.EtherNetIP;
import etherip.protocol.MessageRouterProtocol;
import etherip.types.CNClassPath;
import etherip.types.CNPath;
import etherip.types.CNService;

/**
 * An helper class to read large values from Logix 5000 in multiple requests using the read fragmented protocol.
 *
 * @author Samuel Pelletier
 */
public class ReadFragmented {
	
	private CNPath path;
	private short count;

	public ReadFragmented(final String tagName) {
		this.path = CNClassPath.Symbol(tagName);
		this.count = 1;
	}

	public ReadFragmented(final String tagName, final short count ) {
		this.path = CNClassPath.Symbol(tagName);
		this.count = count;
	}
	
	public byte[] readTagBytes(EtherNetIP etherNetIP) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		while (readTag(etherNetIP, os)) {
		}
		
		return os.toByteArray();
	}

	private boolean readTag(EtherNetIP etherNetIP, ByteArrayOutputStream os) throws Exception {
		CIPReadDataFragmentedProtocol reader = new CIPReadDataFragmentedProtocol(count, os.size());
		MessageRouterProtocol message = new MessageRouterProtocol(CNService.CIP_ReadDataFragmented, path, reader);
		
		etherNetIP.executeRequest(message);
		os.write(reader.getData());
		return message.isPartialTransfert();
	}	
}
