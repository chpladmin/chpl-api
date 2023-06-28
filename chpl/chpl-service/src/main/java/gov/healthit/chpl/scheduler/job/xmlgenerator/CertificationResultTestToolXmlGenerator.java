package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultTestTool;

public class CertificationResultTestToolXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultTestTool> tools, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tools != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultTestTool tool : tools) {
                add(tool, "testTool", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultTestTool tool, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tool != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(tool.getEndDay(), "endDay", sw);
            createSimpleElement(tool.getId(), "id", sw);
            createSimpleElement(tool.getRegulationTextCitation(), "regulationCitationText", sw);
            createSimpleElement(tool.getRequiredDay(), "requiredDay", sw);
            createSimpleElement(tool.isRetired(), "retired", sw);
            //TODO: OCD-4242
            createSimpleElement("", "rule", sw);
            createSimpleElement(tool.getStartDay(), "startDay", sw);
            createSimpleElement(tool.getTestToolId(), "testToolId", sw);
            createSimpleElement(tool.getValue(), "value", sw);
            createSimpleElement(tool.getVersion(), "version", sw);
            sw.writeEndElement();
        }
    }
}
