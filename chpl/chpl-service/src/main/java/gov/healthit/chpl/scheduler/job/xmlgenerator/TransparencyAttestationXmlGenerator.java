package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TransparencyAttestation;

public class TransparencyAttestationXmlGenerator extends XmlGenerator {

    public static void add(TransparencyAttestation ta, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (ta != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(ta.getTransparencyAttestation(), "transparencyAttestation", sw);
            createSimpleElement(ta.getRemoved(), "removed", sw);
            sw.writeEndElement();
        }
    }

}
