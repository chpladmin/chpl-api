package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.criteriaattribute.testtool.TestTool;

public class TestToolXmlGenerator extends XmlGenerator {
    public static void add(TestTool testTool, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (testTool != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(testTool.getEndDay(), "endDay", sw);
            createSimpleElement(testTool.getId(), "id", sw);
            createSimpleElement(testTool.getRegulatoryTextCitation(), "regulatoryTextCitation", sw);
            createSimpleElement(testTool.getRequiredDay(), "requiredDay", sw);
            RuleXmlGenerator.add(testTool.getRule(), "rule", sw);
            createSimpleElement(testTool.getStartDay(), "startDay", sw);
            createSimpleElement(testTool.getValue(), "value", sw);
            sw.writeEndElement();
        }
    }
}
