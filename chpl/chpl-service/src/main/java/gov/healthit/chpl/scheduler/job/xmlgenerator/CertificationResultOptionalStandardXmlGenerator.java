package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;

public class CertificationResultOptionalStandardXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultOptionalStandard> standards, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standards != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultOptionalStandard standard : standards) {
                add(standard, "optionalStandard", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultOptionalStandard standard, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standard != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(standard.getId(), "id", sw);
            createSimpleElement(standard.getOptionalStandardId(), "optionalStandardId", sw);
            createSimpleElement(standard.getStandard(), "standard", sw);
            sw.writeEndElement();
        }
     }
}

