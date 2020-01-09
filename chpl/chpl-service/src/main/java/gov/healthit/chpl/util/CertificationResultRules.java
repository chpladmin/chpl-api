package gov.healthit.chpl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Component("certificationResultRules")
public class CertificationResultRules {
    public static final String GAP = "gap";
    public static final String PRIVACY_SECURITY = "privacySecurity";
    public static final String STANDARDS_TESTED = "standardsTested";
    public static final String FUNCTIONALITY_TESTED = "functionalityTested";
    public static final String API_DOCUMENTATION = "apiDocumentation";
    public static final String EXPORT_DOCUMENTATION = "exportDocumentation";
    public static final String DOCUMENTATION_URL = "documentationUrl";
    public static final String USE_CASES = "useCases";
    public static final String G1_SUCCESS = "g1Success";
    public static final String G2_SUCCESS = "g2Success";
    public static final String G1_MACRA = "g1Macra";
    public static final String G2_MACRA = "g2Macra";
    public static final String ATTESTATION_ANSWER = "attestationAnswer";
    public static final String ADDITIONAL_SOFTWARE = "additionalSoftware";
    public static final String TEST_TOOLS_USED = "testTool";
    public static final String TEST_PROCEDURE = "testProcedure";
    public static final String TEST_DATA = "testData";
    public static final String SED = "sed";
    public static final String UCD_FIELDS = "ucd";
    public static final String TEST_PARTICIPANT = "participant";
    public static final String TEST_TASK = "task";

    private static final Logger LOGGER = LogManager.getLogger(CertificationResultRules.class);

    private Map<String, List<CertificationResultOption>> rules = new HashMap<String, List<CertificationResultOption>>();

    public CertificationResultRules() {
        Document dom;
        // Make an instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        ClassPathResource cpr = new ClassPathResource("certificationResultRules.xml");
        try (InputStream xmlInput = cpr.getInputStream()) {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            dom = db.parse(xmlInput);
            Element doc = dom.getDocumentElement();
            NodeList certNodes = doc.getElementsByTagName("certificationResult");
            for (int i = 0; i < certNodes.getLength(); i++) {
                Element certNode = (Element) certNodes.item(i);
                String certNumber = getTextValue(certNode, "number");
                NodeList propertiesNodes = certNode.getElementsByTagName("properties");
                if (propertiesNodes.getLength() == 1) {
                    Element certProperties = (Element) propertiesNodes.item(0);
                    NodeList certPropertiesNodes = certProperties.getChildNodes();
                    for (int j = 0; j < certPropertiesNodes.getLength(); j++) {
                        Node propertyNode = certPropertiesNodes.item(j);
                        if (propertyNode instanceof Element) {
                            Element propertyElement = (Element) propertyNode;
                            String propName = propertyElement.getNodeName();
                            String propValue = propertyElement.getTextContent();
                            boolean canHaveProperty = Boolean.valueOf(propValue);
                            CertificationResultOption option = new CertificationResultOption();
                            option.setOptionName(propName);
                            if (propName.equals("gap")) {
                                option.setCanHaveOption(true);
                            } else {
                                option.setCanHaveOption(canHaveProperty);
                            }
                            if (rules.get(certNumber) == null) {
                                List<CertificationResultOption> options = new ArrayList<CertificationResultOption>();
                                options.add(option);
                                rules.put(certNumber, options);
                            } else {
                                rules.get(certNumber).add(option);
                            }
                        }
                    }
                }
            }
        } catch (final ParserConfigurationException pce) {
            LOGGER.error(pce.getMessage(), pce);
        } catch (final SAXException se) {
            LOGGER.error(se.getMessage(), se);
        } catch (final IOException ioe) {
            LOGGER.error(ioe.getMessage(), ioe);
        }
    }

    public boolean hasCertOption(String certNumber, String optionName) {
        boolean result = false;

        List<CertificationResultOption> options = rules.get(certNumber);
        if (options != null && options.size() > 0) {
            for (CertificationResultOption option : options) {
                if (option.getOptionName().equalsIgnoreCase(optionName)) {
                    result = option.isCanHaveOption();
                }
            }
        }
        return result;
    }

    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }
}
