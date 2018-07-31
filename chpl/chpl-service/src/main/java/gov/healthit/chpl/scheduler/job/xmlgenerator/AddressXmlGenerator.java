package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Address;

public class AddressXmlGenerator extends XmlGenerator {
    public static void addAddress(Address addr, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (addr != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(addr.getAddressId(), "addressId", sw);
            createSimpleElement(addr.getLine1(), "line1", sw);
            createSimpleElement(addr.getLine2(), "line2", sw);
            createSimpleElement(addr.getCity(), "city", sw);
            createSimpleElement(addr.getState(), "state", sw);
            createSimpleElement(addr.getZipcode(), "zipcode", sw);
            createSimpleElement(addr.getCountry(), "country", sw);
            sw.writeEndElement();
        }
    }
}
