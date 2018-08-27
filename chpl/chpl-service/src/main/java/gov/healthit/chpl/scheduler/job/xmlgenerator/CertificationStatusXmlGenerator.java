package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationStatus;

public class CertificationStatusXmlGenerator extends XmlGenerator {
    public static void addCertificationStatus(CertificationStatus cs, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cs != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(cs.getId(), "id", sw);
            createSimpleElement(cs.getName(), "name", sw);
            sw.writeEndElement();
        }
    }

}
