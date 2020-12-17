package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.MeasureType;

public class MeasureTypeXmlGenerator extends XmlGenerator {
    public static void add(List<MeasureType> measureTypes, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (measureTypes != null) {
            sw.writeStartElement(rootNodeName);
            for (MeasureType measureType : measureTypes) {
                add(measureType, "measureType", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(MeasureType measureType, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (measureType != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(measureType.getId(), "id", sw);
            createSimpleElement(measureType.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
