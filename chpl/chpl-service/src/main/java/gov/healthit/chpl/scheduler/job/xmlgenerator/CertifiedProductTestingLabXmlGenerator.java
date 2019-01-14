package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductTestingLab;

public class CertifiedProductTestingLabXmlGenerator extends XmlGenerator {
    public static void addTestingLabs(List<CertifiedProductTestingLab> tls, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tls != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductTestingLab tl : tls) {
                addTestingLab(tl, "testingLab", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void addTestingLab(CertifiedProductTestingLab tl, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tl != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(tl.getId(), "id", sw);
            createSimpleElement(tl.getTestingLabCode(), "testingLabCode", sw);
            createSimpleElement(tl.getTestingLabId(), "testingLabId", sw);
            createSimpleElement(tl.getTestingLabName(), "testingLabName", sw);
            sw.writeEndElement();
        }
    }
}
