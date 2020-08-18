package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.UcdProcess;

public class UcdProcessXmlGenerator extends XmlGenerator {
    public static void add(List<UcdProcess> processes, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (processes != null) {
            sw.writeStartElement(rootNodeName);
            for (UcdProcess process : processes) {
                add(process, "ucdProcess", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(UcdProcess process, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
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
