package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Measure;

public class MeasureXmlGenerator extends XmlGenerator {
    public static void add(List<Measure> measures, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measures != null) {
            sw.writeStartElement(rootNodeName);
            for (Measure measure : measures) {
                add(measure, "measure", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(Measure measure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measure != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(measure.getAbbreviation(), "abbreviation", sw);
            CertificationCriterionXmlGenerator.add(measure.getAllowedCriteria(), "allowedCriteria", sw);
            MeasureDomainXmlGenerator.add(measure.getDomain(), "domain", sw);
            createSimpleElement(measure.getId(), "id", sw);
            createSimpleElement(measure.getName(), "name", sw);
            createSimpleElement(measure.getRemoved(), "removed", sw);
            createSimpleElement(measure.getRequiredTest(), "requiredTest", sw);
            createSimpleElement(measure.getRequiresCriteriaSelection(), "requiresCriteriaSelection", sw);
            sw.writeEndElement();
        }
    }
}
