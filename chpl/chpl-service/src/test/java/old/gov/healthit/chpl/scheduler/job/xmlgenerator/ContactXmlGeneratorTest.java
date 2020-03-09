package old.gov.healthit.chpl.scheduler.job.xmlgenerator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.scheduler.job.xmlgenerator.ContactXmlGenerator;

public class ContactXmlGeneratorTest {

    @Test
    public void addContactTest() throws XMLStreamException, IOException {
        Contact contact = getContact(1L, "abc@def.com", "John", "Doe", "5555551212", "Mr.");

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        ContactXmlGenerator.addContact(contact, "contact", writer);
        writer.close();

        String expected = "<contact><contactId>1</contactId><email>abc@def.com</email><fullName>John</fullName><friendlyName>Doe</friendlyName><phoneNumber>5555551212</phoneNumber><title>Mr.</title></contact>";
        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addContactTestWithANullValue() throws XMLStreamException, IOException {
        Contact contact = getContact(1L, null, "John", "Doe", "5555551212", "Mr.");

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        ContactXmlGenerator.addContact(contact, "contact", writer);
        writer.close();

        String expected = "<contact><contactId>1</contactId><fullName>John</fullName><friendlyName>Doe</friendlyName><phoneNumber>5555551212</phoneNumber><title>Mr.</title></contact>";
        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addContactTestWithANullContact() throws XMLStreamException, IOException {
        Contact contact = null;

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        ContactXmlGenerator.addContact(contact, "contact", writer);
        writer.close();

        String expected = "";
        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    private Contact getContact(final Long contactId, final String email, final String fullName,
            final String friendlyName, final String phoneNumber, final String title) {
        Contact c = new Contact();
        c.setContactId(contactId);
        c.setEmail(email);
        c.setFullName(fullName);
        c.setFriendlyName(friendlyName);
        c.setPhoneNumber(phoneNumber);
        c.setTitle(title);
        return c;
    }
}
