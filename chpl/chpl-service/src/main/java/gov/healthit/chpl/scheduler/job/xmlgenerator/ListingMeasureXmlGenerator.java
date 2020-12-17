package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.ListingMeasure;

public class ListingMeasureXmlGenerator extends XmlGenerator {
    public static void add(List<ListingMeasure> measures, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measures != null) {
            sw.writeStartElement(rootNodeName);
            for (ListingMeasure measure : measures) {
                add(measure, "measure", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(ListingMeasure measure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measure != null) {
            sw.writeStartElement(rootNodeName);
            CertificationCriterionXmlGenerator.add(measure.getAssociatedCriteria(), "associatedCriteria", sw);
            createSimpleElement(measure.getId(), "id", sw);
            MeasureXmlGenerator.add(measure.getMeasure(), "measure", sw);
            MeasureTypeXmlGenerator.add(measure.getMeasureType(), "measureType", sw);
            sw.writeEndElement();
        }
    }
}
