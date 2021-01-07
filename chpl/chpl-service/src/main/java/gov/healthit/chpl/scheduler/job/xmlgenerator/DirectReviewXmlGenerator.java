package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.compliance.DirectReview;

public class DirectReviewXmlGenerator extends XmlGenerator {
    public static void add(List<DirectReview> drs, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (drs != null) {
            sw.writeStartElement(rootNodeName);
            for (DirectReview dr : drs) {
                add(dr, "directReview", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(DirectReview directReview, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        //TODO
        sw.writeEndElement();
    }
}
