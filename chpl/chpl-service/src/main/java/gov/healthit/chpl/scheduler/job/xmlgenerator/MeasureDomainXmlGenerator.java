package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.MeasureDomain;

public class MeasureDomainXmlGenerator extends XmlGenerator {
    public static void add(List<MeasureDomain> domains, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (domains != null) {
            sw.writeStartElement(rootNodeName);
            for (MeasureDomain domain : domains) {
                add(domain, "domain", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(MeasureDomain domain, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (domain != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(domain.getId(), "id", sw);
            createSimpleElement(domain.getName(), "name", sw);
            sw.writeEndElement();
        }
    }
}
