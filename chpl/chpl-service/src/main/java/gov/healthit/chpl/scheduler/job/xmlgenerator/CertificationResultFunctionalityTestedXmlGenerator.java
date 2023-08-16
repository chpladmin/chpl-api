package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.criteriaattribute.functionalitytested.CertificationResultFunctionalityTested;

public class CertificationResultFunctionalityTestedXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultFunctionalityTested> tests, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tests != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultFunctionalityTested test : tests) {
                add(test, "functionalityTested", sw);
            }
            sw.writeEndElement();
        }
    }

    //TODO: OCD-4288
    public static void add(CertificationResultFunctionalityTested test, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (test != null) {
            sw.writeStartElement(rootNodeName);
            //createSimpleElement(test.getDescription(), "description", sw);
            //createSimpleElement(test.getFunctionalityTestedId(), "functionalityTestedId", sw);
            //createSimpleElement(test.getId(), "id", sw);
            //createSimpleElement(test.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
