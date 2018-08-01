package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.InheritedCertificationStatus;

public class InheritedCertificationStatusXmlGenerator extends XmlGenerator {
    public static void add(InheritedCertificationStatus status, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (status != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(status.getInherits(), "inherits", sw);
            CertifiedProductXmlGenerator.add(status.getParents(), "parents", "parent", sw);
            CertifiedProductXmlGenerator.add(status.getChildren(), "children", "child", sw);
            sw.writeEndElement();
        }
    }
}
