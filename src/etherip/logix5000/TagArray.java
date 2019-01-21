package etherip.logix5000;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import etherip.protocol.Connection;

/**
 * An class that represent a Logix 5000 array value with a type.
 * 
 * A Tag know it's type and how to read and write it's value. If the type is a struct, it's member are available in it's template.
 *
 * @author Samuel Pelletier
 */
public class TagArray<T extends Type> extends Tag<T> {

	public TagArray(Controller controller, String path, T type, int valueCount) {
		super(controller, path, type, valueCount);
	}

	public TagStructure getOrCreateTag(int index) {
		TagStructure value = getTag(index);
		if (value == null) {
			String newPath = path + "[" + index + "]";
			value = new TagStructure(controller, newPath, (Template) type);
			set(index, value);
		}
		return value;
	}
	
	public <V extends Object> List<V> asList() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>) Arrays.asList(values);
		return list;
	}

	public void appendValuesToDescription(StringBuilder description) {
		description.append(" type: ").append(type).append("[").append(values.length).append("] ");
		description.append("[");
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				description.append(", ");
			}
			description.append(i).append(": ");
			if (values[i] instanceof Tag) {
				((Tag<?>) values[i]).appendValuesToDescription(description);
			}
			else {
				description.append(values[i]);
			}
		}
		description.append("]");
	}

	@Override
	public void readFromController() {
		try {
			ReadFragmented tagReader = new ReadFragmented(path, (short)values.length);
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
		for (Object value : values) {
			type.encode(value, dataParts);
		}
	}

	@Override
	public void decode(ByteBuffer buffer) {
		if (type instanceof Template) {
			Template template = (Template) type;
			if (template.isString()) {
				for (int i = 0; i < values.length; i++) {
					values[i] = type.decode(buffer);
				}
			}
			else {
				for (int i = 0; i < values.length; i++) {
					TagStructure value = getOrCreateTag(i);
					value.decode(buffer);
				}
			}
		}
		else {
			for (int i = 0; i < values.length; i++) {
				values[i] = type.decode(buffer);
			}
		}
	}
}
