package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TestingLab;

public class TestingLabXmlGenerator extends XmlGenerator {
    public static void addTestingLab(TestingLab tl, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tl != null) {
            sw.writeStartElement(rootNodeName);
            AddressXmlGenerator.addAddress(tl.getAddress(), "address", sw);
            createSimpleElement(tl.getAtlCode(), "atlCode", sw);
            createSimpleElement(tl.getId(), "id", sw);
            createSimpleElement(tl.getName(), "name", sw);
            createSimpleElement(tl.isRetired(), "retired", sw);
            createSimpleElement(tl.getRetirementDay(), "retirementDay", sw);
            createSimpleElement(tl.getWebsite(), "website", sw);
            sw.writeEndElement();
        }
    }
}
