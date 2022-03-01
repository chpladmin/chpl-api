package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;

public class AttestationPeriodXmlGenerator extends XmlGenerator {
    public static void add(AttestationPeriod period, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (period != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(period.getDescription(), "description", sw);
            createSimpleElement(period.getId(), "id", sw);
            createSimpleElement(period.getPeriodEnd(), "periodEnd", sw);
            createSimpleElement(period.getPeriodStart(), "periodStart", sw);
            createSimpleElement(period.getSubmissionEnd(), "submissionEnd", sw);
            createSimpleElement(period.getSubmissionStart(), "submissionStart", sw);
            sw.writeEndElement();
        }
    }
}
