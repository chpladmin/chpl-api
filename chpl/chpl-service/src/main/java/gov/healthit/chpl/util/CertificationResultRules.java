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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.entity.CertificationCriterionAttributeEntity;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("certificationResultRules")
public class CertificationResultRules {
    public static final String GAP = "gap";
    public static final String PRIVACY_SECURITY = "privacySecurity";
    public static final String CONFORMANCE_METHOD = "conformanceMethod";
    public static final String OPTIONAL_STANDARD = "optionalStandard";
    public static final String STANDARDS_TESTED = "standardsTested";
    public static final String FUNCTIONALITY_TESTED = "functionalityTested";
    public static final String API_DOCUMENTATION = "apiDocumentation";
    public static final String EXPORT_DOCUMENTATION = "exportDocumentation";
    public static final String DOCUMENTATION_URL = "documentationUrl";
    public static final String USE_CASES = "useCases";
    public static final String G1_SUCCESS = "g1Success";
    public static final String G2_SUCCESS = "g2Success";
    public static final String ATTESTATION_ANSWER = "attestationAnswer";
    public static final String ADDITIONAL_SOFTWARE = "additionalSoftware";
    public static final String TEST_TOOLS_USED = "testTool";
    public static final String TEST_PROCEDURE = "testProcedure";
    public static final String TEST_DATA = "testData";
    public static final String SED = "sed";
    public static final String SERVICE_BASE_URL_LIST = "serviceBaseUrlList";
    public static final String SVAP = "svap";
    public static final String UCD_FIELDS = "ucd";
    public static final String TEST_PARTICIPANT = "participant";
    public static final String TEST_TASK = "task";

    private Map<String, List<CertificationResultOption>> rules = new HashMap<String, List<CertificationResultOption>>();

    @Autowired
    public CertificationResultRules(CertificationCriterionAttributeDAO certificationCriterionAttributeDAO) {
        setRulesUsingLegacyXmlFile();
        setRulesUsingDatabase(certificationCriterionAttributeDAO);
    }

    private void setRulesUsingLegacyXmlFile() {
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
                            CertificationResultOption option = CertificationResultOption.builder()
                                    .canHaveOption(propName.equals("gap") || canHaveProperty)
                                    .optionName(propName)
                                    .build();
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
        } catch (ParserConfigurationException pce) {
            LOGGER.error(pce.getMessage(), pce);
        } catch (SAXException se) {
            LOGGER.error(se.getMessage(), se);
        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage(), ioe);
        }
    }

    private void setRulesUsingDatabase(CertificationCriterionAttributeDAO certificationCriterionAttributeDAO) {
        List<CertificationCriterionAttributeEntity> attributes = certificationCriterionAttributeDAO.getAllCriteriaAttributes();
        for (CertificationCriterionAttributeEntity attribute : attributes) {
            if (rules.get(attribute.getCriterion().getNumber()) == null) {
                List<CertificationResultOption> options = new ArrayList<CertificationResultOption>();
                rules.put(attribute.getCriterion().getNumber(), options);
            }
            if (attribute.getConformanceMethod()) {
                rules.get(attribute.getCriterion().getNumber()).add(
                        CertificationResultOption.builder()
                        .canHaveOption(true)
                        .optionName(CONFORMANCE_METHOD)
                        .build());
            }
            if (attribute.getOptionalStandard()) {
                rules.get(attribute.getCriterion().getNumber()).add(
                        CertificationResultOption.builder()
                        .canHaveOption(true)
                        .optionName(OPTIONAL_STANDARD)
                        .build());
            }
            if (attribute.getServiceBaseUrlList()) {
                rules.get(attribute.getCriterion().getNumber()).add(
                        CertificationResultOption.builder()
                        .canHaveOption(true)
                        .optionName(SERVICE_BASE_URL_LIST)
                        .build());
            }
            if (attribute.getSvap()) {
                rules.get(attribute.getCriterion().getNumber()).add(
                        CertificationResultOption.builder()
                        .canHaveOption(true)
                        .optionName(SVAP)
                        .build());
            }
            if (attribute.getTestProcedure()) {
                rules.get(attribute.getCriterion().getNumber()).add(
                        CertificationResultOption.builder()
                        .canHaveOption(true)
                        .optionName(TEST_PROCEDURE)
                        .build());
            }
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
