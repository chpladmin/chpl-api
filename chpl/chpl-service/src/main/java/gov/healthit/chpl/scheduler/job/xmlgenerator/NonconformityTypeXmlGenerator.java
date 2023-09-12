package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.NonconformityType;

public class NonconformityTypeXmlGenerator extends XmlGenerator {
    public static void add(NonconformityType nct, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (nct != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(nct.getEdition(), "edition", sw);
            createSimpleElement(nct.getEndDay(), "endDay", sw);
            createSimpleElement(nct.getId(), "id", sw);
            createSimpleElement(nct.getNumber(), "number", sw);
            createSimpleElement(nct.getStartDay(), "startDay", sw);
            createSimpleElement(nct.getTitle(), "title", sw);
            sw.writeEndElement();
        }
    }
}
