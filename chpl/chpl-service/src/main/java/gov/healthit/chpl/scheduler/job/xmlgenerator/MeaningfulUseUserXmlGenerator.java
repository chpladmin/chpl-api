package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.MeaningfulUseUser;

public class MeaningfulUseUserXmlGenerator extends XmlGenerator {

    public static void add(final List<MeaningfulUseUser> muus, final String rootNodeName,
            final XMLStreamWriter sw) throws XMLStreamException {
       if (muus != null) {
           sw.writeStartElement(rootNodeName);
           for (MeaningfulUseUser muu : muus) {
               add(muu, "meaningfulUseEntry", sw);
           }
           sw.writeEndElement();
       }
    }

    public static void add(final MeaningfulUseUser muu, final String rootNodeName, final XMLStreamWriter sw)
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
