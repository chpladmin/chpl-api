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
		ClassPathResource cpr = new ClassPathResource("certificationResultRules.xml");
		InputStream xmlInput = null;
		try {
			xmlInput = cpr.getInputStream();
		} catch(IOException ioEx) {
			fail(ioEx.getMessage());
			return;
		}
		
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(xmlInput);
            Element doc = dom.getDocumentElement();
            NodeList certNodes = doc.getElementsByTagName("certificationResult");
            for(int i = 0; i < certNodes.getLength(); i++) {
            	Element certNode = (Element)certNodes.item(i);
            	String certNumber = getTextValue(certNode, "number"); 
            	NodeList propertiesNodes = certNode.getElementsByTagName("properties");
            	if(propertiesNodes.getLength() == 1) {
            		Element certProperties = (Element)propertiesNodes.item(0);
            		NodeList certPropertiesNodes = certProperties.getChildNodes();
                	for(int j = 0; j < certPropertiesNodes.getLength(); j++) {
                		Node propertyNode = certPropertiesNodes.item(j);
                		if(propertyNode instanceof Element) {
                			Element propertyElement = (Element)propertyNode;
	                		String propName = propertyElement.getNodeName();
	                		String propValue = propertyElement.getTextContent();
	                		boolean canHaveProperty = new Boolean(propValue).booleanValue();
	                		CertificationResultOption option = new CertificationResultOption();
	                		option.setOptionName(propName);
	                		option.setCanHaveOption(canHaveProperty);
                		}
                	}
            	}
            }
        } catch (ParserConfigurationException pce) {
            fail(pce.getMessage());
        } catch (SAXException se) {
        	fail(se.getMessage());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
	}

	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

}
