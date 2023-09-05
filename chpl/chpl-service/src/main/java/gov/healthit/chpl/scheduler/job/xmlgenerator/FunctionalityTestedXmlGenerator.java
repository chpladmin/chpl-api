package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.functionalitytested.FunctionalityTested;

public class FunctionalityTestedXmlGenerator extends XmlGenerator {
    public static void add(FunctionalityTested functionalityTested, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (functionalityTested != null) {
            //Criteria Attribute values first
            sw.writeStartElement(rootNodeName);
            createSimpleElement(functionalityTested.getAdditionalInformation(), "additionalInformation", sw);
            createSimpleElement(functionalityTested.getEndDay(), "endDay", sw);
            createSimpleElement(functionalityTested.getId(), "id", sw);
            PracticeTypeXmlGenerator.add(functionalityTested.getPracticeType(), "practiceType", sw);
            createSimpleElement(functionalityTested.getRegulatoryTextCitation(), "regulatoryTextCitation", sw);
            createSimpleElement(functionalityTested.getRequiredDay(), "requiredDay", sw);
            RuleXmlGenerator.add(functionalityTested.getRule(), "rule", sw);
            createSimpleElement(functionalityTested.getStartDay(), "startDay", sw);
            createSimpleElement(functionalityTested.getValue(), "value", sw);
            sw.writeEndElement();
        }
    }
}
