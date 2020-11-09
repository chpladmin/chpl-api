package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Developer;

public class DeveloperXmlGenerator extends XmlGenerator {
    public static void addDeveloper(Developer dev, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        AddressXmlGenerator.addAddress(dev.getAddress(), "address", sw);
        PointOfContactXmlGenerator.addContact(dev.getContact(), "contact", sw);
        createSimpleElement(dev.getDeveloperCode(), "developerCode", sw);
        createSimpleElement(dev.getDeveloperId(), "developerId", sw);
        createSimpleElement(dev.getName(), "name", sw);
        createSimpleElement(dev.getSelfDeveloper(), "selfDeveloper", sw);
        DeveloperStatusXmlGenerator.add(dev.getStatus(), "status", sw);
        DeveloperStatusEventXmlGenerator.add(dev.getStatusEvents(), "statusEvents", sw);
        TransparencyAttestationMapXmlGenerator.add(dev.getTransparencyAttestations(), "transparencyAttestations", sw);
        createSimpleElement(dev.getWebsite(), "website", sw);
        sw.writeEndElement();
    }
}
