package etherip.logix5000;

import java.util.ArrayList;
import java.util.List;

import etherip.protocol.MessageRouterProtocol;
import etherip.types.CNClassPath;
import etherip.types.CNPath;
import etherip.types.CNService;

/**
 * An helper class to read the symbol list from Logix 5000 in multiple requests using the read symbol protocol.
 *
 * @author Samuel Pelletier
 */
public class ReadSymbols {
	
	private CNClassPath path;

	public ReadSymbols() {
		this.path = CNPath.SymbolList();
	}
	
	public List<Symbol> readSymbols(Controller controller) throws Exception {
		List<Symbol> symbols = new ArrayList<>();
		
		while (readTag(controller, symbols)) {
			Symbol lastSymbol = symbols.get(symbols.size()-1);
			this.path.instance(lastSymbol.instanceId + 1);
		}
		
		return symbols;
	}

	private boolean readTag(Controller controller, List<Symbol> symbols) throws Exception {
		CIPReadSymbolProtocol reader = new CIPReadSymbolProtocol(controller);
		MessageRouterProtocol message = new MessageRouterProtocol(CNService.Get_Instance_Attribute_List, path, reader);
		controller.plcLink().executeRequest(message);
		symbols.addAll(reader.getSymbols());
		return message.isPartialTransfert();
	}
}
