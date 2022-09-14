package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.RequirementDetailType;

public class RequirementDetailTypeXmlGenerator extends XmlGenerator {
    public static void add(RequirementDetailType rdt, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (rdt != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(rdt.getEdition(), "edition", sw);
            createSimpleElement(rdt.getId(), "id", sw);
            createSimpleElement(rdt.getNumber(), "number", sw);
            createSimpleElement(rdt.getRemoved(), "removed", sw);
            SurveillanceRequirementTypeXmlGenerator.addSurveillanceRequirementType(rdt.getSurveillanceRequirementType(), "surveillanceRequirementType", sw);
            createSimpleElement(rdt.getTitle(), "title", sw);
            sw.writeEndElement();
        }
    }
}
