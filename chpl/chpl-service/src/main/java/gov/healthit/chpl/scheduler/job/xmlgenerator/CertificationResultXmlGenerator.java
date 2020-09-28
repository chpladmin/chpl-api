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
            CertificationResultAdditionalSoftwareXmlGenerator.add(result.getAdditionalSoftware(), "additionalSoftwareList", sw);
            createSimpleElement(result.getApiDocumentation(), "apiDocumentation", sw);
            createSimpleElement(result.getAttestationAnswer(), "attestationAnswer", sw);
            CertificationCriterionXmlGenerator.add(result.getCriterion(), "criterion", sw);
            createSimpleElement(result.getDocumentationUrl(), "documentationUrl", sw);
            createSimpleElement(result.getExportDocumentation(), "exportDocumentation", sw);
            MacraMeasureXmlGenerator.add(result.getG1MacraMeasures(), "g1MacraMeasures", sw);
            createSimpleElement(result.isG1Success(), "g1Success", sw);
            MacraMeasureXmlGenerator.add(result.getG2MacraMeasures(), "g2MacraMeasures", sw);
            createSimpleElement(result.isG2Success(), "g2Success", sw);
            createSimpleElement(result.isGap(), "gap", sw);
            createSimpleElement(result.getNumber(), "number", sw);
            createSimpleElement(result.getPrivacySecurityFramework(), "privacySecurityFramework", sw);
            createSimpleElement(result.isSed(), "sed", sw);
            createSimpleElement(result.isSuccess(), "success", sw);
            CertificationResultSvapXmlGenerator.add(result.getSvaps(), "svaps", sw);
            CertificationResultTestDataXmlGenerator.add(result.getTestDataUsed(), "testDataList", sw);
            CertificationResultTestFunctionalityXmlGenerator.add(result.getTestFunctionality(), "testFunctionalityList", sw);
            CertificationResultTestProcedureXmlGenerator.add(result.getTestProcedures(), "testProcedures", sw);
            CertificationResultTestStandardXmlGenerator.add(result.getTestStandards(), "testStandards", sw);
            CertificationResultTestToolXmlGenerator.add(result.getTestToolsUsed(), "testTools", sw);
            createSimpleElement(result.getTitle(), "title", sw);
            createSimpleElement(result.getUseCases(), "useCases", sw);
            sw.writeEndElement();
        }
    }
}
