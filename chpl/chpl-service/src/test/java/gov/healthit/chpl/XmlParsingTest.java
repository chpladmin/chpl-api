package gov.healthit.chpl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.healthit.chpl.util.CertificationResultOption;

public class XmlParsingTest {

	@Test
	public void test() {
		String id = "14.07.07.1446.ECAS.63.1.1.141113";
		String[] results = id.split("\\.");
		assertEquals(9, results.length);
	}

}
