package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.PracticeType;

public class PracticeTypeXmlGenerator extends XmlGenerator {
    public static void add(PracticeType practiceType, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (practiceType != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(practiceType.getId(), "id", sw);
            createSimpleElement(practiceType.getDescription(), "description", sw);
            createSimpleElement(practiceType.getName(), "name", sw);
            sw.writeEndElement();
        }
    }

}
