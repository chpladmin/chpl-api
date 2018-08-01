package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultTestData;

public class CertificationResultTestDataXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultTestData> dataList, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (dataList != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultTestData data : dataList) {
                add(data, "testData", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultTestData data, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (data != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(data.getId(), "id", sw);
            TestDataXmlGenerator.add(data.getTestData(), "testData", sw);
            createSimpleElement(data.getVersion(), "version", sw);
            createSimpleElement(data.getAlteration(), "alteration", sw);
            sw.writeEndElement();
        }
    }
}
