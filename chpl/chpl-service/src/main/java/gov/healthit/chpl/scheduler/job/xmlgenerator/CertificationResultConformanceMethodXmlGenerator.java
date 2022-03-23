package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultConformanceMethod;

public class CertificationResultConformanceMethodXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultConformanceMethod> methods, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (methods != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultConformanceMethod method : methods) {
                add(method, "conformanceMethod", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultConformanceMethod method, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (method != null) {
            sw.writeStartElement(rootNodeName);
            ConformanceMethodXmlGenerator.add(method.getConformanceMethod(), "conformanceMethod", sw);
            createSimpleElement(method.getConformanceMethodVersion(), "conformanceMethodVersion", sw);
            createSimpleElement(method.getId(), "id", sw);
            sw.writeEndElement();
        }
    }
}
