package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;

public class CertifiedProductAccessibilityStandardXmlGenerator extends XmlGenerator {
    public static void addAccessibilityStandards(List<CertifiedProductAccessibilityStandard> cpass, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cpass != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductAccessibilityStandard cpas : cpass) {
                addAccessibilityStandard(cpas, "accessibilityStandard", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void addAccessibilityStandard(CertifiedProductAccessibilityStandard cpas, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cpas != null) {
           sw.writeStartElement(rootNodeName);
           createSimpleElement(cpas.getAccessibilityStandardId(), "accessibilityStandardId", sw);
           createSimpleElement(cpas.getAccessibilityStandardName(), "accessibilityStandardName", sw);
           createSimpleElement(cpas.getId(), "id", sw);
           sw.writeEndElement();
        }
    }

}
