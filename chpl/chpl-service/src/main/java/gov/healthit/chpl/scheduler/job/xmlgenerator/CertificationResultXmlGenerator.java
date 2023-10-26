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
            CertificationResultConformanceMethodXmlGenerator.add(result.getConformanceMethods(), "conformanceMethods", sw);
            CertificationCriterionXmlGenerator.add(result.getCriterion(), "criterion", sw);
            createSimpleElement(result.getDocumentationUrl(), "documentationUrl", sw);
            createSimpleElement(result.getExportDocumentation(), "exportDocumentation", sw);
            CertificationResultFunctionalityTestedXmlGenerator.add(result.getFunctionalitiesTested(), "functionalitiesTested", sw);
            createSimpleElement(result.isG1Success(), "g1Success", sw);
            createSimpleElement(result.isG2Success(), "g2Success", sw);
            createSimpleElement(result.isGap(), "gap", sw);
            CertificationResultOptionalStandardXmlGenerator.add(result.getOptionalStandards(), "optionalStandards", sw);
            createSimpleElement(result.getPrivacySecurityFramework(), "privacySecurityFramework", sw);
            createSimpleElement(result.isSed(), "sed", sw);
            createSimpleElement(result.getServiceBaseUrlList(), "serviceBaseUrlList", sw);
            CertificationResultStandardXmlGenerator.add(result.getStandards(), "standards", sw);
            createSimpleElement(result.isSuccess(), "success", sw);
            CertificationResultSvapXmlGenerator.add(result.getSvaps(), "svaps", sw);
            CertificationResultTestDataXmlGenerator.add(result.getTestDataUsed(), "testDataList", sw);
            CertificationResultTestProcedureXmlGenerator.add(result.getTestProcedures(), "testProcedures", sw);
            CertificationResultTestStandardXmlGenerator.add(result.getTestStandards(), "testStandards", sw);
            CertificationResultTestToolXmlGenerator.add(result.getTestToolsUsed(), "testTools", sw);
            createSimpleElement(result.getUseCases(), "useCases", sw);
            sw.writeEndElement();
        }
    }
}
