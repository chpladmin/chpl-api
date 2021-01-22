package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.svap.domain.CertificationResultSvap;

public class CertificationResultSvapXmlGenerator extends XmlGenerator {

    public static void add(List<CertificationResultSvap> svaps, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (svaps != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultSvap svap : svaps) {
                add(svap, "svap", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultSvap svap, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (svap != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(svap.getApprovedStandardVersion(), "approvedStandardVersion", sw);
            createSimpleElement(svap.getId(), "id", sw);
            createSimpleElement(svap.getRegulatoryTextCitation(), "regulatoryTextCitation", sw);
            createSimpleElement(svap.getReplaced(), "replaced", sw);
            createSimpleElement(svap.getSvapId(), "svapId", sw);
            sw.writeEndElement();
        }
    }
}
