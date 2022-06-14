package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.ProductVersion;

public class ProductVersionXmlGenerator extends XmlGenerator {
    public static void addProductVersion(ProductVersion pv, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (pv != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(pv.getDetails(), "details", sw);
            createSimpleElement(pv.getId(), "id", sw);
            createSimpleElement(pv.getVersion(), "version", sw);
            sw.writeEndElement();
        }
    }
}
