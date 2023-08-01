package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationEdition;

public class CertificationEditionXmlGenerator extends XmlGenerator {
    public static void add(CertificationEdition edition, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (edition != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(edition.getName(), "name", sw);
            createSimpleElement(edition.getId(), "id", sw);
            sw.writeEndElement();
        }
    }
}
