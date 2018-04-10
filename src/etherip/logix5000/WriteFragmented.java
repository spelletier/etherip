package etherip.logix5000;

import java.io.ByteArrayOutputStream;
import java.util.List;

import etherip.EtherNetIP;
import etherip.protocol.MessageRouterProtocol;
import etherip.types.CNClassPath;
import etherip.types.CNPath;
import etherip.types.CNService;

/**
 * An helper class to write large values to Logix 5000 in multiple requests using the write fragmented protocol.
 *
 * @author Samuel Pelletier
 */
public class WriteFragmented {
	
	private static final int MaxPacketSize = 480;
	private CNPath path;
	private Type type;
	private short elementCount;

	public WriteFragmented(final String tagName, Type type) {
		this.type = type;
		this.elementCount = 1;
		this.path = CNClassPath.Symbol(tagName);
	}

	public WriteFragmented(final String tagName, Type type, short elementCount) {
		this.type = type;
		this.elementCount = elementCount;
		this.path = CNClassPath.Symbol(tagName);
	}
	
	public void writeTagBytes(EtherNetIP etherNetIP, List<byte[]> dataParts) throws Exception {
		int dataSize = type.encodedDataSize() * elementCount;
		if (dataSize > MaxPacketSize-path.getRequestSize()) {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			int offset = 0;
			for (byte[] part : dataParts) {
				if (dataStream.size() + part.length > MaxPacketSize) {
					writeTag(etherNetIP, offset, dataStream.toByteArray());
					offset += dataStream.size();
					dataStream.reset();
				}
				dataStream.write(part);
			}
			if (dataStream.size() > 0) { // Send remaining data
				writeTag(etherNetIP, offset, dataStream.toByteArray());
			}
		}
		else {
			CIPWriteDataProtocol writer = new CIPWriteDataProtocol(type, elementCount, dataParts);
			MessageRouterProtocol message = new MessageRouterProtocol(CNService.CIP_WriteData, path, writer);	
			etherNetIP.executeRequest(message);
		}
	}

	private void writeTag(EtherNetIP etherNetIP, int offset, byte[] dataChunk) throws Exception {
		CIPWriteDataFragmentedProtocol writer = new CIPWriteDataFragmentedProtocol(type, elementCount, offset, dataChunk);
		MessageRouterProtocol message = new MessageRouterProtocol(CNService.CIP_WriteDataFragmented, path, writer);	
		etherNetIP.executeRequest(message);
	}
}
