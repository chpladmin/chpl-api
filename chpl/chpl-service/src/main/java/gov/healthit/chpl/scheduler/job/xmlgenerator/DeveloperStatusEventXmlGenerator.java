package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.DeveloperStatusEvent;

public class DeveloperStatusEventXmlGenerator extends XmlGenerator {
    public static void add(List<DeveloperStatusEvent> list, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (list != null) {
            sw.writeStartElement(rootNodeName);
            for (DeveloperStatusEvent event : list) {
                add(event, "statusEvent", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(DeveloperStatusEvent event, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (event != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(event.getDeveloperId(), "developerId", sw);
            createSimpleElement(event.getId(), "id", sw);
            DeveloperStatusXmlGenerator.add(event.getStatus(), "status", sw);
            createSimpleElement(event.getStatusDate(), "statusDate", sw);
            createSimpleElement(event.getReason(), "reason", sw);
            sw.writeEndElement();
        }
    }

}
