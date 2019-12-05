package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.MacraMeasure;

public class MacraMeasureXmlGenerator extends XmlGenerator {
    public static void add(List<MacraMeasure> measures, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measures != null) {
            sw.writeStartElement(rootNodeName);
            for (MacraMeasure measure : measures) {
                add(measure, "macraMeasure", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(MacraMeasure measure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (measure != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(measure.getAbbreviation(), "abbreviation", sw);
            CertificationCriterionXmlGenerator.add(measure.getCriteria(), "criteria", sw);
            createSimpleElement(measure.getDescription(), "description", sw);
            createSimpleElement(measure.getId(), "id", sw);
            createSimpleElement(measure.getName(), "name", sw);
            createSimpleElement(measure.getRemoved(), "removed", sw);
            sw.writeEndElement();
        }
    }
}
