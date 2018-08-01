package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResult;

public class CertificationResultXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResult> results, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (results != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResult result : results) {
                add(result, "certificationResult", sw);
            }
            sw.writeEndElement();
        }
    }
    
    public static void add(CertificationResult result, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (result != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(result.getNumber(), "number", sw);
            createSimpleElement(result.getTitle(), "title", sw);
            createSimpleElement(result.isSuccess(), "success", sw);
            createSimpleElement(result.isGap(), "gap", sw);
            createSimpleElement(result.isSed(), "sed", sw);
            createSimpleElement(result.isG1Success(), "g1Success", sw);
            createSimpleElement(result.isG2Success(), "g2Success", sw);
            createSimpleElement(result.getApiDocumentation(), "apiDocumentation", sw);
            createSimpleElement(result.getPrivacySecurityFramework(), "privacySecurityFramework", sw);
            createSimpleElement(result.getNumber(), "number", sw);
            CertificationResultTestFunctionalityXmlGenerator.add(result.getTestFunctionality(), "testFunctionalityList", sw);
            CertificationResultTestProcedureXmlGenerator.add(result.getTestProcedures(), "testProcedures", sw);
            CertificationResultTestDataXmlGenerator.add(result.getTestDataUsed(), "testDataList", sw);
            CertificationResultAdditionalSoftwareXmlGenerator.add(result.getAdditionalSoftware(), "additionalSoftwareList", sw);
            CertificationResultTestStandardXmlGenerator.add(result.getTestStandards(), "testStandards", sw);
            CertificationResultTestToolXmlGenerator.add(result.getTestToolsUsed(), "testTools", sw);
            MacraMeasureXmlGenerator.add(result.getG1MacraMeasures(), "g1MacraMeasures", sw);
            MacraMeasureXmlGenerator.add(result.getG2MacraMeasures(), "g2MacraMeasures", sw);
            sw.writeEndElement();
        }
    }
}
