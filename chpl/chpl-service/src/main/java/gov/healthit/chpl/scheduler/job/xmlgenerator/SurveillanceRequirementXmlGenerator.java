package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;

public class SurveillanceRequirementXmlGenerator extends XmlGenerator {
    public static void add(Set<SurveillanceRequirement> srs, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (srs != null) {
            sw.writeStartElement(rootNodeName);
            for (SurveillanceRequirement sr : srs) {
                add(sr, "requirement", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(SurveillanceRequirement sr, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (sr != null) {
            sw.writeStartElement(rootNodeName);
            CertificationCriterionXmlGenerator.add(sr.getCriterion(), "criterion", sw);
            createSimpleElement(sr.getId(), "id", sw);
            createSimpleElement(sr.getRequirement(), "requirement", sw);
            SurveillanceResultTypeXmlGenerator.add(sr.getResult(), "result", sw);
            SurveillanceRequirementTypeXmlGenerator.addSurveillanceRequirementType(sr.getType(), "type", sw);
            sw.writeEndElement();
        }
    }
}
