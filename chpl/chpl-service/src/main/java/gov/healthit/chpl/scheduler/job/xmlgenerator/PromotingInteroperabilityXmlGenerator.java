package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.PromotingInteroperabilityUser;

public class PromotingInteroperabilityXmlGenerator extends XmlGenerator {

    public static void add(final List<PromotingInteroperabilityUser> muus, final String rootNodeName,
            final XMLStreamWriter sw) throws XMLStreamException {
       if (muus != null) {
           sw.writeStartElement(rootNodeName);
           for (PromotingInteroperabilityUser muu : muus) {
               add(muu, "meaningfulUseEntry", sw);
           }
           sw.writeEndElement();
       }
    }

    public static void add(final PromotingInteroperabilityUser muu, final String rootNodeName, final XMLStreamWriter sw)
            throws XMLStreamException {
        if (muu != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(muu.getId(), "id", sw);
            createSimpleElement(muu.getMuuCount(), "muuCount", sw);
            createSimpleElement(muu.getMuuDate(), "muuDate", sw);
            sw.writeEndElement();
        }
    }
}
