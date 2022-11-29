package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductUcdProcess;

public class CertifiedProductUcdProcessXmlGenerator extends XmlGenerator {
    public static void add(List<CertifiedProductUcdProcess> processes, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (processes != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductUcdProcess process : processes) {
                add(process, "ucdProcess", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertifiedProductUcdProcess process, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (process != null) {
            sw.writeStartElement(rootNodeName);
            CertificationCriterionXmlGenerator.add(process.getCriteria(), "criteriaList", sw);
            createSimpleElement(process.getDetails(), "details", sw);
            createSimpleElement(process.getId(), "id", sw);
            createSimpleElement(process.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
