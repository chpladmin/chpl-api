package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.standard.Standard;

public class StandardXmlGenerator extends XmlGenerator {
    public static void add(Standard standard, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standard != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(standard.getAdditionalInformation(), "additionalInformation", sw);
            createSimpleElement(standard.getEndDay(), "endDay", sw);
            createSimpleElement(standard.getId(), "id", sw);
            createSimpleElement(standard.getRegulatoryTextCitation(), "regulatoryTextCitation", sw);
            createSimpleElement(standard.getRequiredDay(), "requiredDay", sw);
            RuleXmlGenerator.add(standard.getRule(), "rule", sw);
            createSimpleElement(standard.getStartDay(), "startDay", sw);
            createSimpleElement(standard.getValue(), "value", sw);
            sw.writeEndElement();
        }
    }

}
