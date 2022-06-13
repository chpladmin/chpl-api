package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Product;

public class ProductXmlGenerator extends XmlGenerator {
    public static void addProduct(Product prod, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (prod != null) {
            sw.writeStartElement(rootNodeName);
            PointOfContactXmlGenerator.addContact(prod.getContact(), "contact", sw);
            createSimpleElement(prod.getId(), "id", sw);
            createSimpleElement(prod.getName(), "name", sw);
            DeveloperXmlGenerator.addDeveloper(prod.getOwner(), "owner", sw);
            ProductOwnerXmlGenerator.add(prod.getOwnerHistory(), "ownerHistory", sw);
            createSimpleElement(prod.getReportFileLocation(), "reportFileLocation", sw);
            sw.writeEndElement();
        }
    }
}
