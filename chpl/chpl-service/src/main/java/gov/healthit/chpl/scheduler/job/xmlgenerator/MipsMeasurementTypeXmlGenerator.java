package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.MipsMeasurementType;

public class MipsMeasurementTypeXmlGenerator extends XmlGenerator {
    public static void add(List<MipsMeasurementType> measurementTypes, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (measurementTypes != null) {
            sw.writeStartElement(rootNodeName);
            for (MipsMeasurementType measurementType : measurementTypes) {
                add(measurementType, "measurementType", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(MipsMeasurementType measurementType, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (measurementType != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(measurementType.getId(), "id", sw);
            createSimpleElement(measurementType.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
