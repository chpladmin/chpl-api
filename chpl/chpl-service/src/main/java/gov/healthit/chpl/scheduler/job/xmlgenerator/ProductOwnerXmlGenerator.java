package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.ProductOwner;

public class ProductOwnerXmlGenerator extends XmlGenerator {
    public static void add(final List<ProductOwner> list, final String rootNodeName,
            final XMLStreamWriter sw) throws XMLStreamException {
        if (list != null) {
            sw.writeStartElement(rootNodeName);
            for (ProductOwner po : list) {
                add(po, "owner", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(final ProductOwner po, final String rootNodeName,
            final XMLStreamWriter sw) throws XMLStreamException {
        if (po != null) {
            sw.writeStartElement(rootNodeName);
            DeveloperXmlGenerator.addDeveloper(po.getDeveloper(), "developer", sw);
            createSimpleElement(po.getId(), "id", sw);
            createSimpleElement(po.getTransferDate(), "transferDate", sw);
            sw.writeEndElement();
        }
    }
}
