package old.gov.healthit.chpl.scheduler.job.xmlgenerator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.scheduler.job.xmlgenerator.CertificationStatusEventXmlGenerator;

public class CertificationStatusEventXmlGeneratorTest {
    @Test
    public void addCertificationStatusTest() throws XMLStreamException, IOException {
        CertificationStatusEvent cse =
                getCertificationStatusEvent(20180101L, 1L, "Status Reason", 1L, "Open");
        String expected = "<event><eventDate>20180101</eventDate><id>1</id><reason>Status Reason</reason><status><id>1</id><name>Open</name></status></event>";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        CertificationStatusEventXmlGenerator.add(cse, "event", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addCertificationStatusTestList() throws XMLStreamException, IOException {
        List<CertificationStatusEvent> list = new ArrayList<CertificationStatusEvent>();
        list.add(getCertificationStatusEvent(20180101L, 1L, "Status Reason", 1L, "Open"));
        list.add(getCertificationStatusEvent(20180201L, 2L, "Status Reason Closed", 2L, "Closed"));

        String expected = "<events><certificationEvent><eventDate>20180101</eventDate><id>1</id><reason>Status Reason</reason><status><id>1</id><name>Open</name></status></certificationEvent><certificationEvent><eventDate>20180201</eventDate><id>2</id><reason>Status Reason Closed</reason><status><id>2</id><name>Closed</name></status></certificationEvent></events>";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        CertificationStatusEventXmlGenerator.add(list, "events", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    private CertificationStatusEvent getCertificationStatusEvent(Long eventDate, Long id, String reason, Long statusId, String statusName) {
        CertificationStatusEvent cse = new CertificationStatusEvent();
        cse.setEventDate(eventDate);
        cse.setId(id);
        cse.setReason(reason);
        cse.setStatus(getCertificationStatus(statusId, statusName));
        return cse;
    }

    private CertificationStatus getCertificationStatus(Long id, String name) {
        CertificationStatus cs = new CertificationStatus();
        cs.setId(id);
        cs.setName(name);
        return cs;
    }
}
