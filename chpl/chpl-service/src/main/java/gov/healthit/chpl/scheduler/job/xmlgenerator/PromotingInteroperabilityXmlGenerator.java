package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.PromotingInteroperabilityUser;

public class PromotingInteroperabilityXmlGenerator extends XmlGenerator {

    public static void add(List<PromotingInteroperabilityUser> pius, String rootNodeName,
            final XMLStreamWriter sw) throws XMLStreamException {
       if (pius != null) {
           sw.writeStartElement(rootNodeName);
           for (PromotingInteroperabilityUser piu : pius) {
               add(piu, "promotingInteroperabilityUserEntry", sw);
           }
           sw.writeEndElement();
       }
    }

    public static void add(PromotingInteroperabilityUser piu, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (piu != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(piu.getId(), "id", sw);
            createSimpleElement(piu.getUserCount(), "userCount", sw);
            createSimpleElement(piu.getUserCountDate().toString(), "userCountDate", sw);
            sw.writeEndElement();
        }
    }
}
