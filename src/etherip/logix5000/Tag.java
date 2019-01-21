package etherip.logix5000;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * An abstract class that represent a Logix 5000 array or struct value. Basic types are member of a struct or accessed directly.
 * 
 * A Tag know it's type and how to read and write it's value. If the type is a struct, it's member are available in it's template.
 *
 * @author Samuel Pelletier
 */
public abstract class Tag<T extends Type> {

	protected final Controller controller;
	protected final String path;
	protected final T type;
	protected final Object values[];

	public Tag(Controller controller, String path, T type, int valueCount) {
		this.controller = controller;
		this.path = path;
		this.type = type;
		values = new Object[valueCount];
	}

	public String path() {
		return path;
	}
	
	@SuppressWarnings("unchecked")
	public <U extends Type> U getType() {
		return (U)type;
	}
	
	public int numberOfValues() {
		return values.length;
	}

	public Object get(int index) {
		return values[index];
	}

	public void set(int index, Object value) {
		values[index] = value;
	}

	public String getString(int index) {
		return (String) values[index];
	}

	public Integer getInt(int index) {
		return (Integer) values[index];
	}

	public TagStructure getTag(int index) {
		return (TagStructure) values[index];
	}

	@SuppressWarnings("unchecked")
	public TagArray<? extends Type> getArray(int index) {
		return (TagArray<? extends Type>) values[index];
	}

	abstract public void readFromController();
	abstract public void writeToController();

	abstract public void encode(List<byte[]> dataParts);
	abstract public void decode(ByteBuffer buffer);
	abstract public void appendValuesToDescription(StringBuilder description);

	@Override
	public String toString() {
		StringBuilder description = new StringBuilder();
		description.append("<Tag ").append(path).append(": ");
		appendValuesToDescription(description);
		description.append(">");
		return description.toString();
	}
}