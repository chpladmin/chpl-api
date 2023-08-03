package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationStatusEvent;

public class CertificationStatusEventXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationStatusEvent> events, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
       if (events != null) {
           sw.writeStartElement(rootNodeName);
           for (CertificationStatusEvent event : events) {
               add(event, "certificationEvent", sw);
           }
           sw.writeEndElement();
       }
    }

    public static void add(CertificationStatusEvent event, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (event != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(event.getEventDate(), "eventDate", sw);
            createSimpleElement(event.getId(), "id", sw);
            CertificationStatusXmlGenerator.addCertificationStatus(event.getStatus(), "status", sw);
            sw.writeEndElement();
        }
    }
}
