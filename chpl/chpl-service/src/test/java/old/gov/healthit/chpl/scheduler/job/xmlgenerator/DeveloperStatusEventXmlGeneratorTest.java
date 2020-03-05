package old.gov.healthit.chpl.scheduler.job.xmlgenerator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.scheduler.job.xmlgenerator.DeveloperStatusEventXmlGenerator;
import gov.healthit.chpl.scheduler.job.xmlgenerator.XmlGenerator;

public class DeveloperStatusEventXmlGeneratorTest {
    private SimpleDateFormat sdf = new SimpleDateFormat(XmlGenerator.DATE_FORMAT);

    @Test
    public void addDeveloperStatusTest_NoReason() throws XMLStreamException, IOException {
        DeveloperStatusEvent dse = getDeveloperStatusEvent(1L, 1534425861000L, 1L, null, 1L,
                DeveloperStatusType.Active.toString());
        String expected =
                "<statusEvent>"
                        + "<developerId>1</developerId>"
                        + "<id>1</id>"
                        + "<status><id>1</id><status>Active</status></status>"
                        + "<statusDate>" + sdf.format(dse.getStatusDate()) + "</statusDate>"
                        + "</statusEvent>";
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        DeveloperStatusEventXmlGenerator.add(dse, "statusEvent", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addDeveloperStatusTest_WithReason() throws XMLStreamException, IOException {
        DeveloperStatusEvent dse = getDeveloperStatusEvent(1L, 1534425861000L, 1L, "In trouble", 1L,
                DeveloperStatusType.SuspendedByOnc.toString());
        String expected =
                "<statusEvent>"
                    + "<developerId>1</developerId>"
                    + "<id>1</id>"
                    + "<status><id>1</id><status>Suspended by ONC</status></status>"
                    + "<statusDate>" + sdf.format(dse.getStatusDate()) + "</statusDate>"
                    + "<reason>In trouble</reason>"
                    + "</statusEvent>";
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        DeveloperStatusEventXmlGenerator.add(dse, "statusEvent", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addDeveloperStatusTestList() throws XMLStreamException, IOException {
        DeveloperStatusEvent firstDse = getDeveloperStatusEvent(1L, 1534425861000L, 1L, null, 1L,
                DeveloperStatusType.Active.toString());
        DeveloperStatusEvent secondDse = getDeveloperStatusEvent(1L, 1534425862000L, 2L, "They did bad stuff.", 3L,
                DeveloperStatusType.UnderCertificationBanByOnc.toString());

        List<DeveloperStatusEvent> list = new ArrayList<DeveloperStatusEvent>();
        list.add(firstDse);
        list.add(secondDse);

        String expected = "<statusEvents>"
                + "<statusEvent>"
                +   "<developerId>1</developerId>"
                +   "<id>1</id>"
                +   "<status><id>1</id><status>Active</status></status>"
                +   "<statusDate>" + sdf.format(firstDse.getStatusDate()) + "</statusDate>"
                + "</statusEvent>"
                + "<statusEvent>"
                +   "<developerId>1</developerId>"
                +   "<id>2</id>"
                +   "<status><id>3</id><status>Under certification ban by ONC</status></status>"
                +   "<statusDate>" + sdf.format(secondDse.getStatusDate()) + "</statusDate>"
                +   "<reason>They did bad stuff.</reason>"
                + "</statusEvent>"
                + "</statusEvents>";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);
        DeveloperStatusEventXmlGenerator.add(list, "statusEvents", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    private DeveloperStatusEvent getDeveloperStatusEvent(Long developerId,
            Long eventDate, Long id, String reason, Long statusId, String statusName) {
        DeveloperStatusEvent cse = new DeveloperStatusEvent();
        cse.setDeveloperId(developerId);
        cse.setStatusDate(new Date(eventDate));
        cse.setId(id);
        cse.setReason(reason);
        cse.setStatus(getDeveloperStatus(statusId, statusName));
        return cse;
    }

    private DeveloperStatus getDeveloperStatus(Long id, String name) {
        DeveloperStatus ds = new DeveloperStatus();
        ds.setId(id);
        ds.setStatus(name);
        return ds;
    }
}
