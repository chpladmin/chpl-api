package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;

public class ConformanceMethodXmlGenerator extends XmlGenerator {
    public static void add(ConformanceMethod method, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (method != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(method.getId(), "id", sw);
            createSimpleElement(method.getName(), "name", sw);
            createSimpleElement(method.getRemoved(), "removed", sw);
            sw.writeEndElement();
        }
    }
}
