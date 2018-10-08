package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.Contact;

public class ContactXmlGenerator extends XmlGenerator {
    public static void addContact(final Contact contact, final String rootNodeName,
            final XMLStreamWriter sw) throws XMLStreamException {
        if (contact != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(contact.getContactId(), "contactId", sw);
            createSimpleElement(contact.getEmail(), "email", sw);
            createSimpleElement(contact.getFullName(), "fullName", sw);
            createSimpleElement(contact.getFriendlyName(), "friendlyName", sw);
            createSimpleElement(contact.getPhoneNumber(), "phoneNumber", sw);
            createSimpleElement(contact.getTitle(), "title", sw);
            sw.writeEndElement();
        }
    }
}
