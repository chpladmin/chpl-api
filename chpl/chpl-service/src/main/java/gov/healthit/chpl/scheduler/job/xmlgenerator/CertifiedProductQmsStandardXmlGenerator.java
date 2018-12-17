package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;

public class CertifiedProductQmsStandardXmlGenerator extends XmlGenerator {
    public static void add(List<CertifiedProductQmsStandard> standards, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standards != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductQmsStandard standard : standards) {
                add(standard, "qmsStandard", sw);
            }
            sw.writeEndElement();
        }
    }

public static void add(CertifiedProductQmsStandard standard, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standard != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(standard.getApplicableCriteria(), "applicableCriteria", sw);
            createSimpleElement(standard.getId(), "id", sw);
            createSimpleElement(standard.getQmsModification(), "qmsModification", sw);
            createSimpleElement(standard.getQmsStandardId(), "qmsStandardId", sw);
            createSimpleElement(standard.getQmsStandardName(), "qmsStandardName", sw);
            sw.writeEndElement();
        }
    }

}
