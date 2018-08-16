package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.DeveloperStatus;

public class DeveloperStatusXmlGenerator extends XmlGenerator {
    public static void add(DeveloperStatus status, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (status != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(status.getId(), "id", sw);
            createSimpleElement(status.getStatus(), "status", sw);
            sw.writeEndElement();
        }
    }
}
