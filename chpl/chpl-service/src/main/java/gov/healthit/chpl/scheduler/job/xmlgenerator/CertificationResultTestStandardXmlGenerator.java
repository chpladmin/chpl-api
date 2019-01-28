package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultTestStandard;

public class CertificationResultTestStandardXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultTestStandard> standards, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standards != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultTestStandard standard : standards) {
                add(standard, "testStandard", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultTestStandard standard, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standard != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(standard.getId(), "id", sw);
            createSimpleElement(standard.getTestStandardDescription(), "testStandardDescription", sw);
            createSimpleElement(standard.getTestStandardId(), "testStandardId", sw);
            createSimpleElement(standard.getTestStandardName(), "testStandardName", sw);
            sw.writeEndElement();
        }
     }
}

