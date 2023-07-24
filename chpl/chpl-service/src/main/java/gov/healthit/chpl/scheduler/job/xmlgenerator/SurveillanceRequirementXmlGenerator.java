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
            createSimpleElement(sr.getId(), "id", sw);
            SurveillanceNonConformityXmlGenerator.add(sr.getNonconformities(), "nonconformities", sw);
            RequirementTypeXmlGenerator.add(sr.getRequirementType(), "requirementType", sw);
            createSimpleElement(sr.getRequirementTypeOther(), "requirementTypeOther", sw);
            SurveillanceResultTypeXmlGenerator.add(sr.getResult(), "result", sw);
            sw.writeEndElement();
        }
    }
}
