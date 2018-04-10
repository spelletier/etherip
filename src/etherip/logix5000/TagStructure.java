package etherip.logix5000;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import etherip.logix5000.Template.Member;
import etherip.protocol.Connection;

/**
 * An class that represent a Logix 5000 structure value with a template.
 * 
 * A Tag know it's type and how to read and write it's value. Member can be accessed with their index or name.
 *
 * @author Samuel Pelletier
 */
public class TagStructure extends Tag<Template> {

	public TagStructure(Controller controller, String path, Template template) {
		super(controller, path, template, template.numberOfMembers());
	}
	
	public String getString(String name) {
		return (String) get(name);
	}
	public Integer getInt(String name) {
		return (Integer) get(name);
	}
	public boolean getBoolean(String name) {
		return (boolean) get(name);
	}
	public TagStructure getTag(String name) {
		return (TagStructure) get(name);
	}
	public TagArray<?> getArray(String name) {
		return (TagArray<?>) get(name);
	}

	public TagStructure getTag(Member member) {
		TagStructure value = getTag(member.index());
		if (value == null) {
			String newPath = path + "." + member.name();
			value = new TagStructure(controller, newPath, (Template) member.type());
			set(member.index(), value);
		}
		return value;
	}
	
	public Object get(String name) {
		int index = type.indexOfMember(name);
		return values[index];
	}

	public TagArray<? extends Type> getArray(Member member) {
		TagArray<? extends Type> value = getArray(member.index());
		if (value == null) {
			String newPath = path + "." + member.name();
			value = new TagArray<Type>(controller, newPath, member.type(), member.arraySize());				
			set(member.index(), value);
		}
		return value;
	}
	
	public void set(String name, Object value) {
		int index = type.indexOfMember(name);
		set(index, value);
	}

	@Override
	public String toString() {
		StringBuilder description = new StringBuilder();
		description.append("<Tag ").append(path);
		appendValuesToDescription(description);
		description.append(">");
		return description.toString();
	}

	public void appendValuesToDescription(StringBuilder description) {
		description.append("{");
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				description.append(", ");
			}
			description.append(type.memberName(i)).append(": ");
			if (values[i] instanceof Tag) {
				((Tag<?>) values[i]).appendValuesToDescription(description);
			}
			else {
				description.append(values[i]);
			}
		}
		description.append("}");
	}

	@Override
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

			WriteFragmented writer = new WriteFragmented(path, type);
			writer.writeTagBytes(controller.plcLink(), dataParts);

		}
		catch (Exception e) {
			controller.disconnect();
			throw new RuntimeException("Exception writing tag "+path+" to controller.", e);
		}
	}

	@Override
	public void encode(List<byte[]> dataParts) {
		type.encode(this, dataParts);
	}

	@Override
	public void decode(ByteBuffer buffer) {
		type.decodeInTag(buffer, this);
	}
}
