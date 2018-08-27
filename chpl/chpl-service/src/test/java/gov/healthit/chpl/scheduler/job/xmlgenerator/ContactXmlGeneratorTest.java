package gov.healthit.chpl.scheduler.job.xmlgenerator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import gov.healthit.chpl.domain.Contact;

public class ContactXmlGeneratorTest {
    
    
    @Test
    public void addContactTest() throws XMLStreamException, IOException {
        Contact contact = 
                getContact(1l, "abc@def.com", "John", "Doe", "5555551212", "Mr.");
        
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);
        
        ContactXmlGenerator.addContact(contact, "contact", writer);
        writer.close();
        
        String expected = "<contact><contactId>1</contactId><email>abc@def.com</email><firstName>John</firstName><lastName>Doe</lastName><phoneNumber>5555551212</phoneNumber><title>Mr.</title></contact>";
        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }
    
    @Test
    public void addContactTestWithANullValue() throws XMLStreamException, IOException {
        Contact contact = 
                getContact(1l, null, "John", "Doe", "5555551212", "Mr.");
        
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);
        
        ContactXmlGenerator.addContact(contact, "contact", writer);
        writer.close();
        
        String expected = "<contact><contactId>1</contactId><firstName>John</firstName><lastName>Doe</lastName><phoneNumber>5555551212</phoneNumber><title>Mr.</title></contact>";
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
    
    private Contact getContact(Long contactId, String email, String firstName, String lastName, String phoneNumber, String title) {
        Contact c = new Contact();
        c.setContactId(contactId);
        c.setEmail(email);
        c.setFirstName(firstName);
        c.setLastName(lastName);
        c.setPhoneNumber(phoneNumber);
        c.setTitle(title);
        return c;
    }
}
