package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.RequirementType;

public class RequirementTypeXmlGenerator extends XmlGenerator {
    public static void add(RequirementType rdt, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (rdt != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(rdt.getEdition(), "edition", sw);
            createSimpleElement(rdt.getEndDay(), "endDay", sw);
            createSimpleElement(rdt.getId(), "id", sw);
            createSimpleElement(rdt.getNumber(), "number", sw);
            RequirementGroupTypeXmlGenerator.addRequirementGroupType(rdt.getRequirementGroupType(), "requirementGroupType", sw);
            createSimpleElement(rdt.getStartDay(), "startDay", sw);
            createSimpleElement(rdt.getTitle(), "title", sw);
            sw.writeEndElement();
        }
    }
}
