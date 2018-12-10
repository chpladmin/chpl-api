package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultTestFunctionality;

public class CertificationResultTestFunctionalityXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultTestFunctionality> tests, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tests != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultTestFunctionality test : tests) {
                add(test, "testFunctionality", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultTestFunctionality test, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (test != null) {
            sw.writeStartElement(rootNodeName);
           createSimpleElement(test.getDescription(), "description", sw);
            createSimpleElement(test.getId(), "id", sw);
            createSimpleElement(test.getName(), "name", sw);
            createSimpleElement(test.getTestFunctionalityId(), "testFunctionalityId", sw);
            createSimpleElement(test.getYear(), "year", sw);
            sw.writeEndElement();
        }
    }
}
