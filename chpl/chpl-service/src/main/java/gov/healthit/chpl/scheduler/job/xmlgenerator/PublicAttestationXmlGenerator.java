package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.PublicAttestation;

public class PublicAttestationXmlGenerator extends XmlGenerator {
    public static void add(List<PublicAttestation> attestations, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (attestations != null) {
            sw.writeStartElement(rootNodeName);
            for (PublicAttestation attestation : attestations) {
                add(attestation, "attestation", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(PublicAttestation attestation, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (attestation != null) {
            sw.writeStartElement(rootNodeName);
            AttestationPeriodXmlGenerator.add(attestation.getAttestationPeriod(), "attestationPeriod", sw);
            createSimpleElement(attestation.getId(), "id", sw);
            createSimpleElement(attestation.getStatus().name(), "status", sw);
            sw.writeEndElement();
        }
    }
}
