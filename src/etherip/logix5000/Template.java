package etherip.logix5000;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import etherip.protocol.Connection;

/**
 * A template (struct definition) that know how to encode and decode a struct.
 *
 * @author Samuel Pelletier
 */
public class Template implements Type {
	private static final byte[] StructTypeBytes = new byte[] {(byte) 0xA0, 0x02};

	private Controller controller;	
	private short instanceId;
	private int objectSize;
	private int structureSize;
	private short numberOfMembers;
	private short crcValue;
	private Member[] members;

	private String name;

	private int structurePadding;

	public Template(Controller controller, final short instanceId, final int objectSize, final int structureSize, final short numberOfMembers, final short crcValue) {
		this.instanceId = instanceId;
		this.controller = controller;
		this.objectSize = objectSize;
		this.structureSize = structureSize;
		this.numberOfMembers = numberOfMembers;
		this.crcValue = crcValue;	
	}
	
	public short code() {
		return instanceId;
	}
	
	public short definitionByteCount() {
		return (short) (objectSize * 4 - 23);
	}
	
	public short crcValue() {
		return crcValue;
	}
	
	public int numberOfMembers() {
		return members.length;
	}
	
	public String memberName(int index) {
		return members[index].name;
	}
	
	public int indexOfMember(String name) {
		for (Member member : members) {
			if (member.name.equals(name)) {
				return member.index;
			}
		}
		throw new IllegalArgumentException("There is no member with name "+name+" in this template named "+name);
	}

	public int elementIndexOfMember(String name) {
		for (Member member : members) {
			if (member.name.equals(name)) {
				return member.elementIndex;
			}
		}
		throw new IllegalArgumentException("There is no member with name "+name+" in this template named "+name);
	}

	public <T extends Type> T typeOfMember(String name) {
		return typeOfMember(indexOfMember(name));
	}

	@SuppressWarnings("unchecked")
	public <T extends Type> T typeOfMember(int index) {
		return (T) members[index].type;
	}
	
	@Override
	public String toString() {
		StringBuilder description = new StringBuilder();
		description.append("<Template ").append(instanceId);
		description.append(" ").append(objectSize).append(" words");
		description.append(" ").append(structureSize).append(" bytes");
		description.append(" ").append(" members ").append(Arrays.asList(members));
		description.append(">");
		return description.toString();
	}

	public void readDefinition(byte[] byteArray) throws Exception {
		ByteBuffer buffer = ByteBuffer.wrap(byteArray);
		buffer.order(Connection.BYTE_ORDER);
		
		List<Member> memberArray = new ArrayList<Member>();
		
		int nextDataOffset = 0;
		for (short i = 0; i < numberOfMembers; i++) {
			short arraySize = buffer.getShort();
			short typeCode = buffer.getShort();
			int offset = buffer.getInt();
			Member member = new Member(i, typeCode, arraySize, offset, nextDataOffset);
			nextDataOffset = offset + member.encodedDataSize();
			memberArray.add(member);
		}
		structurePadding = Math.max(structureSize - nextDataOffset, 0);
		
		name = readName(buffer);
		
		short index = 0;
		for (Member member : new ArrayList<>(memberArray)) {
			name = readName(buffer);
			member.setName(name);
			if (name.startsWith("ZZZZZZZZZZ")) {
				memberArray.remove(member);
			}
			else {
				member.setIndex(index++);
			}
		}
		members = memberArray.toArray(new Member[memberArray.size()]);
	}

	private String readName(ByteBuffer buffer) {
		ByteArrayOutputStream nameBytes = new ByteArrayOutputStream();
		int readByte;
		while ( (readByte = buffer.get()) != 0) {
			nameBytes.write(readByte);
		}
		String name = new String(nameBytes.toByteArray(), StandardCharsets.ISO_8859_1);
		return name;
	}
	
	@Override
	public int encodedTypeSize() {
		return 4;
	}
	
	@Override
	public void encodeType(ByteBuffer buffer) {
		buffer.put(StructTypeBytes);
		buffer.putShort(crcValue);
	}
	
	@Override
	public int encodedDataSize() {
		return structureSize;
	}

	@Override
	public void encode(Object value, List<byte[]> dataParts) {
		TagStructure tag = (TagStructure) value;	
		int lastOffset = -1;
		for (int i = 0; i < members.length; i++) {
			Object memberValue = tag.get(i);
			if (lastOffset == members[i].offset) {
				byte[] lastValue = dataParts.remove(dataParts.size()-1);
				members[i].encode(memberValue, dataParts);				
				byte[] newValue = dataParts.get(dataParts.size()-1);
				if (lastValue.length != 1 || newValue.length != 1) {
					throw new RuntimeException("Trying to encode 2 members at same offset but with differents size. Only bool are supported. Type: "+members[i].type);
				}
				newValue[0] = (byte) (lastValue[0] | newValue[0]);
			}
			else {
				if (members[i].padding > 0) {
					dataParts.add(new byte[members[i].padding]);
				}
				members[i].encode(memberValue, dataParts);
			}
			lastOffset = members[i].offset;
		}
		if (structurePadding > 0) {
			dataParts.add(new byte[structurePadding]);
		}
	}
	
	public void decodeInTag(ByteBuffer buffer, TagStructure tag) {
		ByteBuffer localBuffer = buffer.slice();
		localBuffer.order(Connection.BYTE_ORDER);
		for (int i = 0; i < members.length; i++) {
			members[i].decodeInTag(localBuffer, tag);
		}
		buffer.position(buffer.position()+structureSize);
	}
	
	@Override
	public Object decode(ByteBuffer buffer) {
		throw new RuntimeException("Not supposed to reach this method.");
	}
	
	public class Member {
		private final short elementIndex;
		private short index;
		private final short typeCode;
		private final short arraySize;
//		private boolean isStructure = false;
		private boolean isReserved = false;
//		private byte nbDimensions = 0;
		private final Type type;
		private final int offset;
		private final int padding;
		private String name;

		public Member(short elementIndex, short typeCode, short arraySize, int offset, int nextDataOffset) {
			this.elementIndex = elementIndex;
//			isStructure = (typeCode & 0x8000) == 0x8000;
			isReserved = (typeCode & 0x1000) == 0x1000;
//			nbDimensions = (byte) (typeCode & 0x6000);
			this.typeCode = (short) (typeCode & 0x0FFF);
			Type type = controller.typeForCode(this.typeCode);

			this.type = type;
			this.arraySize = arraySize;
			this.offset = offset;
			
			this.padding = Math.max(offset - nextDataOffset, 0);
		}
		
		public int encodedDataSize() {
			if (arraySize > 0) {
				return type.encodedDataSize() * arraySize;
			} else {
				return type.encodedDataSize();
			}
		}

		private void setIndex(short index) {
			this.index = index;
		}

		public void decodeInTag(ByteBuffer buffer, TagStructure tag) {
			buffer.position(offset);
			if (arraySize > 0) {
				TagArray<?> value = tag.getArray(this);
				value.decode(buffer);
			}
			else if (type instanceof Template) {
				TagStructure value = tag.getTag(this);
				value.decode(buffer);
			}
			else {
				Object value = type.decode(buffer);
				tag.set(index, value);
			}
		}

		public void encode(Object memberValue, List<byte[]> dataParts) {
			if (memberValue instanceof Tag) {
				((Tag<?>) memberValue).encode(dataParts);
			}
			else {
				type.encode(memberValue, dataParts);
			}
		}

		@SuppressWarnings("unchecked")
		public <V extends Type> V type() {
			return (V) type;
		}
		
		public short elementIndex() {
			return elementIndex;
		}

		public short index() {
			return index;
		}

		public short arraySize() {
			return arraySize;
		}
		
		public int offset() {
			return offset;
		}
		
		public String name() {
			return name;
		}
		
		private void setName(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			StringBuilder description = new StringBuilder();
			description.append(name).append(" ");
			if (isReserved) {
				description.append(" RESERVED");
			}
			description.append(type);
			if (arraySize > 0) {
				description.append("[").append(arraySize).append("]");
			}
			description.append(" offset: ").append(offset);
			return description.toString();
		}
	}
}
