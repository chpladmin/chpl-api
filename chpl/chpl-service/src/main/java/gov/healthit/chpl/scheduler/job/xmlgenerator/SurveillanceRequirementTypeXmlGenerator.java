package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;

public class SurveillanceRequirementTypeXmlGenerator extends XmlGenerator {

    public static void addSurveillanceRequirementType(SurveillanceRequirementType srt, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (srt != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(srt.getId(), "id", sw);
            createSimpleElement(srt.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
