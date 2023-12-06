package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.standard.CertificationResultStandard;

public class CertificationResultStandardXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultStandard> standards, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standards!= null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultStandard standard : standards) {
                add(standard, "standard", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultStandard standard, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (standard != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(standard.getId(), "id", sw);
            StandardXmlGenerator.add(standard.getStandard(), "standard", sw);
            sw.writeEndElement();
        }
    }


}
