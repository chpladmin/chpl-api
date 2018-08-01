package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.ProductOwner;

public class ProductOwnerXmlGenerator extends XmlGenerator {
    public static void add(List<ProductOwner> list, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (list != null ) {
            sw.writeStartElement(rootNodeName);
            for(ProductOwner po : list) {
                add(po, "owner", sw);
            }
            sw.writeEndElement();
        }
    }
    
    public static void add(ProductOwner po, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (po != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(po.getId(), "id", sw);
            DeveloperXmlGenerator.addDeveloper(po.getDeveloper(), "developer", sw);
            createSimpleElement(po.getTransferDate(), "transferDate", sw);
            sw.writeEndElement();
        }
    }
}
