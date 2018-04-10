package etherip.logix5000;

/**
 * A class that represent a Logix 5000 symbol (root level tags).
 * 
 * A symbol know it's path and can return it's Tag with proper type.
 *
 * @author Samuel Pelletier
 */
public class Symbol {
	private Controller controller;
	public final int instanceId;
	public final String name;
	private final short typeCode;
	private boolean isStructure = false;
	private boolean isReserved = false;
	private byte nbDimensions = 0;
	private int arraySize[];
	private Type type;
	private Tag<?> tag;
	
	public Symbol(Controller controller, int instanceId, String name, short typeCode, int[] arraySize) {
		this.controller = controller;
		this.instanceId = instanceId;
		this.name = name;
		this.arraySize = arraySize;
		
		isStructure = (typeCode & 0x8000) == 0x8000;
		isReserved = (typeCode & 0x1000) == 0x1000;
		nbDimensions = (byte) (typeCode & 0x6000 >> 13);
		this.typeCode = (short) (typeCode & 0x0FFF);
	}

	public Type type() {
		if (type == null) {
			type = controller.typeForCode(typeCode);
		}
		return type;
	}

	public boolean isStructure() {
		return isStructure;
	}

	public boolean isReserved() {
		return isReserved;
	}

	public byte nbDimensions() {
		return nbDimensions;
	}
	
	public Tag<?> tag() {
		if (tag == null) {
			if (arraySize[0] > 0) {
				tag = new TagArray<>(controller, name, type(), arraySize[0]);
			}
			else if (isStructure) {
				tag = new TagStructure(controller, name, (Template) type());
			}
			try {
				tag.readFromController();
			}
			catch (Exception e) {
				tag = null;
				throw new RuntimeException("Exceptiuon reading a tag", e);
			}
		}
		return tag;
	}

	@Override
	public String toString() {
		StringBuilder description = new StringBuilder();
		description.append("<Symbol ").append(instanceId);
		description.append(" ").append(name);
		for (int i = 0; i < 3; i++) {
			if (arraySize[i] > 0) {
				description.append("[").append(arraySize[i]).append("]");
			}
		}
		if (isReserved) {
			description.append(" RESERVED");
		}
		if (isStructure) {
			description.append(" structure ");
		}
		description.append(" ").append(typeCode);
		description.append(" >");
		return description.toString();
	}
}
