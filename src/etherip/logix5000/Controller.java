package etherip.logix5000;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import etherip.EtherNetIP;
import etherip.types.CNPath;
import etherip.types.CNSymbolPath;
import etherip.types.CNSymbolPath.PathElement;

public class Controller {
	private static final long PingPeriod = 1*60*1000;
	private Timer pingTimer;
	private EtherNetIP plcLink;
	private Map<String, Symbol> symbolsByName = new HashMap<>();
	private Map<Short, Type> typesByCode = new HashMap<>();
	private String address;
	private int slot;
	
	public Controller(final String address, final int slot) {
		this.address = address;
		this.slot = slot;
		clearTypes();
		startPingTask();
	}
	
	private void startPingTask() {
		pingTimer = new Timer("Logix 4000 Controller ping timer", true);
		pingTimer.schedule(new TimerTask() {
			public void run() {
				ping();
			}
		}, PingPeriod, PingPeriod);

	}
	
	public void close() throws Exception {
		plcLink.close();
	}
	
	public EtherNetIP plcLink() throws Exception {
		if (plcLink == null) {
			plcLink = new EtherNetIP(address, slot);
			plcLink.connectTcp();
		}
		return plcLink;
	}
	
	public void disconnect() {
		try {
			if (plcLink != null) {
				plcLink.close();
			}
		}
		catch (Exception e) {}
		finally {
			plcLink = null;
		}
	}
	
	public boolean ping() {
		try {
			plcLink().getIdentity();
			return true;
		}
		catch (Exception e) {
			disconnect();
			try {
				plcLink().getIdentity();
				return true;
			}
			catch (Exception e1) {
				disconnect();
			}
			return false;
		}
	}
	
	public String instancePathFromSymbolicPath(String path) {
		StringBuilder newPath = new StringBuilder();
		CNSymbolPath cnPath = CNPath.Symbol(path);
		Type currentType = null;
		for (PathElement pathElement : cnPath.getElements()) {
			if (currentType == null) {
				Symbol symbol = symbolWithName(pathElement.getPath());
				newPath.append(symbol.instanceId);
				currentType = symbol.type();
			}
			else {
				Template template = (Template) currentType;
				int memberIndex = template.elementIndexOfMember(pathElement.getPath());
				newPath.append(".");
				newPath.append(memberIndex);
				currentType = template.typeOfMember(pathElement.getPath());
			}
			if (pathElement.getIndex() != null) {
				newPath.append(".");
				newPath.append(pathElement.getIndex());
			}
		}
		return newPath.toString();
	}
	
	public TagStructure structureWithPath(String path) {
		return (TagStructure) valueWithPath(path);
	}

	public TagArray<?> arrayWithPath(String path) {
		return (TagArray<?>) valueWithPath(path);
	}

	public Object valueWithPath(String path) {
		Object value;
		String[] parts = path.split("\\.");
		
		String part = parts[0];
		int arrayMarkerPosition = part.indexOf('[');
		if (arrayMarkerPosition > 0) {
			int index = Integer.parseInt(part.substring(arrayMarkerPosition+1, part.length()-1));
			part = part.substring(0, arrayMarkerPosition);
			TagArray<?> arrayValue = (TagArray<?>) symbolWithName(part).value();
			value = arrayValue.get(index);
		}
		else {
			value = symbolWithName(parts[0]).value();
		}
		
		for (int i = 1; i < parts.length; i++) {
			part = parts[i];
			arrayMarkerPosition = part.indexOf('[');
			if (arrayMarkerPosition > 0) {
				int index = Integer.parseInt(part.substring(arrayMarkerPosition+1, part.length()-1));
				part = part.substring(0, arrayMarkerPosition);
				value = ((TagStructure) value).getArray(part).get(index);
			}
			else {
				value = ((TagStructure) value).get(part);
			}
		}
		return value;
	}

	public Symbol symbolWithName(String name) {
		if (symbolsByName.isEmpty()) {
			try {
				readSymbols();
			}
			catch (Exception e) {
				disconnect();
				throw new RuntimeException("Error reading symbol list from controller", e);
			}
		}
		return symbolsByName.get(name);
	}

	public Type typeForCode(short typeCode) {
		Type type = typesByCode.get(typeCode);
		if (type == null) {
			ReadTemplate reader = new ReadTemplate();
			try {
				type = reader.readTemplate(this, typeCode);
			}
			catch (Exception e) {
				disconnect();
				throw new RuntimeException("Error reading template "+typeCode, e);
			}
			System.out.println("Template read: "+type);
		}
		return type;
	}

	private void readSymbols() throws Exception {
		symbolsByName.clear();
		
		ReadSymbols reader = new ReadSymbols();
		List<Symbol> symbols = reader.readSymbols(this);
		System.out.println("symbols: "+symbols);
		for (Symbol symbol : symbols) {
			symbolsByName.put(symbol.name, symbol);
		}
	}

	private void clearTypes() {
		typesByCode.clear();
		typesByCode.put(BasicType.BOOL.code(), BasicType.BOOL);
		typesByCode.put(BasicType.BOOL1.code(), BasicType.BOOL1);
		typesByCode.put(BasicType.BOOL2.code(), BasicType.BOOL2);
		typesByCode.put(BasicType.BOOL3.code(), BasicType.BOOL3);
		typesByCode.put(BasicType.BOOL4.code(), BasicType.BOOL4);
		typesByCode.put(BasicType.BOOL5.code(), BasicType.BOOL5);
		typesByCode.put(BasicType.BOOL6.code(), BasicType.BOOL6);
		typesByCode.put(BasicType.BOOL7.code(), BasicType.BOOL7);
		typesByCode.put(BasicType.SINT.code(), BasicType.SINT);
		typesByCode.put(BasicType.INT.code(), BasicType.INT);
		typesByCode.put(BasicType.DINT.code(), BasicType.DINT);
		typesByCode.put(BasicType.LINT.code(), BasicType.LINT);
		typesByCode.put(BasicType.REAL.code(), BasicType.REAL);
		typesByCode.put(BasicType.DWORD.code(), BasicType.DWORD);
//		typesByCode.put(BasicType.STRING.code(), BasicType.STRING);
	}
}
