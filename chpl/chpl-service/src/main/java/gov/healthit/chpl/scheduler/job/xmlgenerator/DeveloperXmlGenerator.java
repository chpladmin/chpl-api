package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.TransparencyAttestationMap;

public class DeveloperXmlGenerator extends XmlGenerator{
    public static void addDeveloper(Developer dev, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        createSimpleElement(dev.getDeveloperId(), "developerId", sw);
        createSimpleElement(dev.getDeveloperCode(), "developerCode", sw);
        createSimpleElement(dev.getName(), "name", sw);
        createSimpleElement(dev.getWebsite(), "website", sw);
        AddressXmlGenerator.addAddress(dev.getAddress(), "address", sw);
        ContactXmlGenerator.addContact(dev.getContact(),"contact", sw);
        addTransparencyAttestations(dev.getTransparencyAttestations(), rootNodeName, sw);
        addDeveloperStatusEvents(dev.getStatusEvents(), "statusEvents", sw);
        addDeveloperStatus(dev.getStatus(), "status", sw);
        sw.writeEndElement();
        
    }
    
    private static void addDeveloperStatusEvents(List<DeveloperStatusEvent> list, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (list != null) {
            sw.writeStartElement(rootNodeName);
            for (DeveloperStatusEvent event : list) {
                addDeveloperStatusEvent(event, "statusEvent", sw);
            }
            sw.writeEndElement();
        }
    }
    
    private static void addDeveloperStatusEvent(DeveloperStatusEvent event, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (event != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(event.getId(), "id", sw);
            createSimpleElement(event.getDeveloperId(), "developerId", sw);
            addDeveloperStatus(event.getStatus(), "status", sw);
            createSimpleElement(event.getStatusDate(), "statusDate", sw);
            sw.writeEndElement();
        }
    }
    
    private static void addDeveloperStatus(DeveloperStatus status, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (status != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(status.getId(), "id", sw);
            createSimpleElement(status.getStatus(), "status", sw);
            sw.writeEndElement();
        }
    }
    
    private static void addTransparencyAttestations(List<TransparencyAttestationMap> list, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (list != null) {
            sw.writeStartElement(rootNodeName);
            for (TransparencyAttestationMap map : list) {
                addTransparencyAttestationMap(map, "transparencyAttestationMap", sw);
            }
            sw.writeEndElement();
        }
    }
    
    private static void addTransparencyAttestationMap(TransparencyAttestationMap map, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (map != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(map.getAcbId(), "acbId", sw);
            createSimpleElement(map.getAcbName(), "acbName", sw);
            sw.writeEndElement();
        }
    }
    
}
