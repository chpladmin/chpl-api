package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.SurveillanceType;

public class SurveillanceTypeXmlGenerator extends XmlGenerator {
    public static void addSurveillanceType(SurveillanceType st, String rootNodeNanme, XMLStreamWriter sw) throws XMLStreamException {
        if (st != null) {
            sw.writeStartElement(rootNodeNanme);
            createSimpleElement(st.getId(), "id", sw);
            createSimpleElement(st.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
