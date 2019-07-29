package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;

public class SurveillanceResultTypeXmlGenerator extends XmlGenerator {
    public static void add(SurveillanceResultType srt, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (srt != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(srt.getId(), "id", sw);
            createSimpleElement(srt.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
