package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;

public class CertifiedProductChplProductNumberHistoryXmlGenerator extends XmlGenerator {
    public static void add(List<CertifiedProductChplProductNumberHistory> history, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (history != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductChplProductNumberHistory historyItem : history) {
                add(historyItem, "chplProductNumberHistoryItem", sw);
            }
            sw.writeEndElement();
        }
    }

public static void add(CertifiedProductChplProductNumberHistory historyItem, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (historyItem != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(historyItem.getChplProductNumber(), "chplProductNumber", sw);
            createSimpleElement(historyItem.getEndDateTime(), "endDateTime", sw);
            createSimpleElement(historyItem.getId(), "id", sw);
            sw.writeEndElement();
        }
    }

}
