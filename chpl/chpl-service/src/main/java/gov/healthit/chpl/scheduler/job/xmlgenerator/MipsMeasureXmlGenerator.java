package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.MipsMeasure;

public class MipsMeasureXmlGenerator extends XmlGenerator {
    public static void add(List<MipsMeasure> measures, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measures != null) {
            sw.writeStartElement(rootNodeName);
            for (MipsMeasure measure : measures) {
                add(measure, "measure", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(MipsMeasure measure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measure != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(measure.getAbbreviation(), "abbreviation", sw);
            CertificationCriterionXmlGenerator.add(measure.getAllowedCriteria(), "allowedCriteria", sw);
            MipsMeasureDomainXmlGenerator.add(measure.getDomain(), "domain", sw);
            createSimpleElement(measure.getId(), "id", sw);
            createSimpleElement(measure.getName(), "name", sw);
            createSimpleElement(measure.getRemoved(), "removed", sw);
            createSimpleElement(measure.getRequiredTest(), "requiredTest", sw);
            createSimpleElement(measure.getRequiresCriteriaSelection(), "requiresCriteriaSelection", sw);
            sw.writeEndElement();
        }
    }
}
