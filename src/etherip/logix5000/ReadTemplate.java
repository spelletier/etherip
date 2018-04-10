package etherip.logix5000;

import java.io.ByteArrayOutputStream;

import etherip.EtherNetIP;
import etherip.protocol.MessageRouterProtocol;
import etherip.types.CNClassPath;
import etherip.types.CNPath;
import etherip.types.CNService;

/**
 * An helper class to read a template definition from Logix 5000 in multiple requests using read template attribute and definition protocols.
 *
 * @author Samuel Pelletier
 */
public class ReadTemplate {
	
	private CNClassPath path;
	private Template template;

	public ReadTemplate() {
		this.path = CNPath.TemplateAttributes();
	}
	
	public Template readTemplate(Controller controller, short templateId) throws Exception {
		readTemplateAttributes(controller, templateId);
		
		ByteArrayOutputStream templateDefinition = new ByteArrayOutputStream();
		while (readDefinition(controller.plcLink(), templateDefinition)) {
		}

		template.readDefinition(templateDefinition.toByteArray());
		return template;
	}
	
	private void readTemplateAttributes(Controller controller, short templateId) throws Exception {
		this.path.instance(templateId);
		CIPReadTemplateAttributesProtocol reader = new CIPReadTemplateAttributesProtocol(controller);
		MessageRouterProtocol message = new MessageRouterProtocol(CNService.Get_Attributes_List, path, reader);
		controller.plcLink().executeRequest(message);
		template = reader.createTemplate(templateId);
	}


	private boolean readDefinition(EtherNetIP etherNetIP, ByteArrayOutputStream templateDefinition) throws Exception {
		CIPReadTemplateDefinitionProtocol reader = new CIPReadTemplateDefinitionProtocol(template.definitionByteCount(), templateDefinition.size());
		MessageRouterProtocol message = new MessageRouterProtocol(CNService.CIP_ReadData, path, reader);
		etherNetIP.executeRequest(message);
		templateDefinition.write(reader.getBytes());
		return message.isPartialTransfert();
	}
}
