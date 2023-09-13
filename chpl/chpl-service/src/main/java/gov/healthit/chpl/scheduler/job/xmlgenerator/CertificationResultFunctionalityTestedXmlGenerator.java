package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;

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

    public static void add(CertificationResultFunctionalityTested crft, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (crft != null) {
            sw.writeStartElement(rootNodeName);
            FunctionalityTestedXmlGenerator.add(crft.getFunctionalityTested(), "functionalityTested", sw);
            createSimpleElement(crft.getId(), "id", sw);
            sw.writeEndElement();
        }
    }
}
