package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.criteriaattribute.rule.Rule;

public class RuleXmlGenerator extends XmlGenerator {
    public static void add(Rule rule, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (rule != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(rule.getId(), "id", sw);
            createSimpleElement(rule.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
