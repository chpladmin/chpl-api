package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;

public class CertificationResultAdditionalSoftwareXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultAdditionalSoftware> softwares, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (softwares != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultAdditionalSoftware software : softwares) {
                add(software, "additionalSoftware", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultAdditionalSoftware software, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (software != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(software.getCertifiedProductId(), "certifiedProductId", sw);
            createSimpleElement(software.getCertifiedProductNumber(), "certifiedProductNumber", sw);
            createSimpleElement(software.getGrouping(), "grouping", sw);
            createSimpleElement(software.getId(), "id", sw);
            createSimpleElement(software.getJustification(), "justification", sw);
            createSimpleElement(software.getName(), "name", sw);
            createSimpleElement(software.getVersion(), "version", sw);
            sw.writeEndElement();
        }
    }
}
