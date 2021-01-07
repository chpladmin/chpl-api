package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;

public class DirectReviewNonConformityXmlGenerator extends XmlGenerator {
    public static void add(List<DirectReviewNonConformity> nonconformities, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (nonconformities != null) {
            sw.writeStartElement(rootNodeName);
            for (DirectReviewNonConformity nonconformity : nonconformities) {
                add(nonconformity, "nonConformity", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(DirectReviewNonConformity nonconformity, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        //TODO
        sw.writeEndElement();
    }
}
