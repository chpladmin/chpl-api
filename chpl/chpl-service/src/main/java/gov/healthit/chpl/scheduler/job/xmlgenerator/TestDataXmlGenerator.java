package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TestData;

public class TestDataXmlGenerator extends XmlGenerator {
    public static void add(TestData data, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (data != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(data.getId(), "id", sw);
            createSimpleElement(data.getName(), "name", sw);
            sw.writeEndElement();
        }
    }

}
