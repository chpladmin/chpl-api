package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TestProcedure;

public class TestProcedureXmlGenerator extends XmlGenerator {
    public static void add(TestProcedure procedure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (procedure != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(procedure.getId(), "id", sw);
            createSimpleElement(procedure.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
