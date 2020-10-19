package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.ListingMipsMeasure;

public class ListingMipsMeasureXmlGenerator extends XmlGenerator {
    public static void add(List<ListingMipsMeasure> measures, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measures != null) {
            sw.writeStartElement(rootNodeName);
            for (ListingMipsMeasure measure : measures) {
                add(measure, "mipsMeasure", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(ListingMipsMeasure measure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measure != null) {
            sw.writeStartElement(rootNodeName);
            CertificationCriterionXmlGenerator.add(measure.getAssociatedCriteria(), "associatedCriteria", sw);
            createSimpleElement(measure.getId(), "id", sw);
            MipsMeasureXmlGenerator.add(measure.getMeasure(), "measure", sw);
            MipsMeasurementTypeXmlGenerator.add(measure.getMeasurementType(), "measurementType", sw);
            sw.writeEndElement();
        }
    }
}
