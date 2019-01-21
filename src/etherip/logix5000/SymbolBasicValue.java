package etherip.logix5000;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import etherip.protocol.Connection;

public class SymbolBasicValue extends Tag<BasicType> {
	
	public SymbolBasicValue(Controller controller, String path, BasicType type) {
		super(controller, path, type, 1);
	}
	
	public void readFromController() {
		try {
			ReadFragmented tagReader = new ReadFragmented(path);
			byte[] bytes = tagReader.readTagBytes(controller.plcLink());
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			buffer.order(Connection.BYTE_ORDER);
			decode(buffer);
		}
		catch (Exception e) {
			controller.disconnect();
			throw new RuntimeException("Exception reading tag "+path+" from controller.", e);
		}
	}

	public void writeToController() {
		try {
			List<byte[]> dataParts = new ArrayList<>();
			encode(dataParts);

			WriteFragmented writer = new WriteFragmented(path, type, (short) values.length);
			writer.writeTagBytes(controller.plcLink(), dataParts);
		}
		catch (Exception e) {
			controller.disconnect();
			throw new RuntimeException("Exception writing tag "+path+" to controller.", e);
		}
	}

	@Override
	public void encode(List<byte[]> dataParts) {
		type.encode(values[0], dataParts);
	}

	@Override
	public void decode(ByteBuffer buffer) {
		set(0, type.decode(buffer));
	}

	@Override
	public void appendValuesToDescription(StringBuilder description) {
		description.append(values[0]);
	}
	
	public Object value() {
		return values[0];
	}
	
	public void setValue(Object value) {
		set(0, value);
	}
	
	public Integer intValue() {
		return (Integer) values[0];
	}

	public Long longValue() {
		return (Long) values[0];
	}

	public Float floatValue() {
		return (Float) values[0];
	}
}
