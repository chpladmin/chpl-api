package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Developer;

public class DeveloperXmlGenerator extends XmlGenerator{
    public static void addDeveloper(Developer dev, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        createSimpleElement(dev.getDeveloperId(), "developerId", sw);
        createSimpleElement(dev.getDeveloperCode(), "developerCode", sw);
        createSimpleElement(dev.getName(), "name", sw);
        createSimpleElement(dev.getWebsite(), "website", sw);
        AddressXmlGenerator.addAddress(dev.getAddress(), "address", sw);
        ContactXmlGenerator.addContact(dev.getContact(),"contact", sw);
        TransparencyAttestationMapXmlGenerator.add(dev.getTransparencyAttestations(), rootNodeName, sw);
        DeveloperStatusEventXmlGenerator.add(dev.getStatusEvents(), "statusEvents", sw);
        DeveloperStatusXmlGenerator.add(dev.getStatus(), "status", sw);
        sw.writeEndElement();
        
    }
}
