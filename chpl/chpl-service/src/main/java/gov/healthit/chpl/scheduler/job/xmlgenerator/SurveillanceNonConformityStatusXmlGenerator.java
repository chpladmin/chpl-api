package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;

public class SurveillanceNonConformityStatusXmlGenerator extends XmlGenerator {
    public static void add(SurveillanceNonconformityStatus status, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (status != null ) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(status.getId(), "id", sw);
            createSimpleElement(status.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
