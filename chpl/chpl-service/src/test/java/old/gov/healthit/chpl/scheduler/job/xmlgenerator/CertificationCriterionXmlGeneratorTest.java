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

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.scheduler.job.xmlgenerator.CertificationCriterionXmlGenerator;

public class CertificationCriterionXmlGeneratorTest {
    @Test
    public void addCertificationCriterionTest() throws XMLStreamException, IOException {
        CertificationCriterion cc = getCertifiectionCriterion("2014", 2L, "This is the description", 1L, "314.123", "Short title");
        String expected = "<criteria><certificationEdition>2014</certificationEdition><certificationEditionId>2</certificationEditionId><description>This is the description</description><id>1</id><number>314.123</number><title>Short title</title></criteria>";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        CertificationCriterionXmlGenerator.add(cc, "criteria", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addCertificationCriterionTestWithNullValue() throws XMLStreamException, IOException {
        CertificationCriterion cc = getCertifiectionCriterion(null, 2L, "This is the description", 1L, "314.123", "Short title");
        String expected = "<criteria><certificationEditionId>2</certificationEditionId><description>This is the description</description><id>1</id><number>314.123</number><title>Short title</title></criteria>";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        CertificationCriterionXmlGenerator.add(cc, "criteria", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addCertificationCriterionTestWithNullObject() throws XMLStreamException, IOException {
        CertificationCriterion cc = null;
        String expected = "";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        CertificationCriterionXmlGenerator.add(cc, "criteria", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    @Test
    public void addCertificationCriterionListTest() throws XMLStreamException, IOException {
        List<CertificationCriterion> list = new ArrayList<CertificationCriterion>();
        list.add(getCertifiectionCriterion("2014", 2L, "This is the description", 1L, "314.123", "Short title"));
        list.add(getCertifiectionCriterion("2014", 2L, "This is the description2", 2L, "314.124", "Short title2"));

        String expected = "<criteriaList><criteria><certificationEdition>2014</certificationEdition><certificationEditionId>2</certificationEditionId><description>This is the description</description><id>1</id><number>314.123</number><title>Short title</title></criteria><criteria><certificationEdition>2014</certificationEdition><certificationEditionId>2</certificationEditionId><description>This is the description2</description><id>2</id><number>314.124</number><title>Short title2</title></criteria></criteriaList>";

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringOut = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringOut);

        CertificationCriterionXmlGenerator.add(list, "criteriaList", writer);
        writer.close();

        assertEquals(expected, stringOut.toString());
        stringOut.close();
    }

    private CertificationCriterion getCertifiectionCriterion(String certificationEdition, Long certificationEditionId, String description, Long id, String number, String title) {
        CertificationCriterion cc = new CertificationCriterion();
        cc.setCertificationEdition(certificationEdition);
        cc.setCertificationEditionId(certificationEditionId);
        cc.setDescription(description);
        cc.setId(id);
        cc.setNumber(number);
        cc.setTitle(title);
        return cc;
    }
}
