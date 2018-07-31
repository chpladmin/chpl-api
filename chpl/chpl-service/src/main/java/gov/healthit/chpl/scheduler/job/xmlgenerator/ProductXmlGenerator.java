package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;

public class ProductXmlGenerator extends XmlGenerator {
    public static void addProduct(Product prod, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (prod != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(prod.getProductId(), "productId", sw);
            createSimpleElement(prod.getName(), "name", sw);
            createSimpleElement(prod.getReportFileLocation(), "reportFileLocation", sw);
            ContactXmlGenerator.addContact(prod.getContact(), "contact", sw);
            DeveloperXmlGenerator.addDeveloper(prod.getOwner(), "owner", sw);
            addProductOwners(prod.getOwnerHistory(), "ownerHistory", sw);
            sw.writeEndElement();
        }
    }
    
    private static void addProductOwners(List<ProductOwner> list, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (list != null ) {
            sw.writeStartElement(rootNodeName);
            for(ProductOwner po : list) {
                addProductOwner(po, "owner", sw);
            }
            sw.writeEndElement();
        }
    }
    
    private static void addProductOwner(ProductOwner po, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (po != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(po.getId(), "id", sw);
            DeveloperXmlGenerator.addDeveloper(po.getDeveloper(), "developer", sw);
            createSimpleElement(po.getTransferDate(), "transferDate", sw);
            sw.writeEndElement();
        }
    }
}
