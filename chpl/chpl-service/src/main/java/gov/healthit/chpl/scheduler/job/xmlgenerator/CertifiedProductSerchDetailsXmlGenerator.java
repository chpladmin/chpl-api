package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class CertifiedProductSerchDetailsXmlGenerator extends XmlGenerator {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductSerchDetailsXmlGenerator.class);
    
    public static void add(List<CertifiedProductSearchDetails> cps, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cps != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductSearchDetails cp : cps) {
                add(cp, "listing", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertifiedProductSearchDetails cp, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cp != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(cp.getId(), "id", sw);
            createSimpleElement(cp.getChplProductNumber(), "chplProductNumber", sw);
            createSimpleElement(cp.getReportFileLocation(), "reportFileLocation", sw);
            createSimpleElement(cp.getSedReportFileLocation(), "sedReportFileLocation", sw);
            createSimpleElement(cp.getSedIntendedUserDescription(), "sedIntendedUserDescription", sw);
            createSimpleElement(cp.getSedTestingEndDate(), "YYMMDD", "sedTestingEndDate", sw);
            createSimpleElement(cp.getAcbCertificationId(), "acbCertificationId", sw);
            createSimpleElement(cp.getOtherAcb(), "otherAcb", sw);
            createSimpleElement(cp.getCertificationDate(), "certificationDate", sw);
            createSimpleElement(cp.getDecertificationDate(), "decertificationDate", sw);
            createSimpleElement(cp.getCountCerts(), "countCerts", sw);
            createSimpleElement(cp.getCountCqms(), "countCqms", sw);
            createSimpleElement(cp.getCountSurveillance(), "countSurveillance", sw);
            createSimpleElement(cp.getCountOpenSurveillance(), "countOpenSurveillance", sw);
            createSimpleElement(cp.getCountClosedSurveillance(), "countClosedSurveillance", sw);
            createSimpleElement(cp.getCountOpenNonconformities(), "countOpenNonconformities", sw);
            createSimpleElement(cp.getCountClosedNonconformities(), "countClosedNonconformities", sw);
            createSimpleElement(cp.getAccessibilityCertified(), "accessibilityCertified", sw);
            createSimpleElement(cp.getProductAdditionalSoftware(), "productAdditionalSoftware", sw);
            createSimpleElement(cp.getTransparencyAttestation(), "transparencyAttestation", sw);
            createSimpleElement(cp.getTransparencyAttestationUrl(), "transparencyAttestationUrl", sw);
            createSimpleElement(cp.getNumMeaningfulUse(), "numMeaningfulUse", sw);
            createSimpleElement(cp.getLastModifiedDate(), "lastModifiedDate", sw);
            DeveloperXmlGenerator.addDeveloper(cp.getDeveloper(), "developer", sw);
            ProductXmlGenerator.addProduct(cp.getProduct(), "product", sw);
            ProductVersionXmlGenerator.addProductVersion(cp.getVersion(), "version", sw);
            CertifiedProductTestingLabXmlGenerator.addTestingLabs(cp.getTestingLabs(), "testingLabs", sw);
            CertifiedProductAccessibilityStandardXmlGenerator.addAccessibilityStandards(cp.getAccessibilityStandards(), "accessibilityStandards", sw);
            CertificationStatusEventXmlGenerator.add(cp.getCertificationEvents(), "certificationEvents", sw);
            CertifiedProductQmsStandardXmlGenerator.add(cp.getQmsStandards(), "qmsStandards", sw);
            CertifiedProductTargetedUserXmlGenerator.add(cp.getTargetedUsers(), "targetedUsers", sw);
            SurveillanceXmlGenerator.add(cp.getSurveillance(), "surveillanceList", sw);
            CqmResultDetailsXmlGenerator.add(cp.getCqmResults(), "cqmResults", sw);
            CertificationResultXmlGenerator.add(cp.getCertificationResults(), "certificationResults", sw);
            CertifiedProductSedXmlGenerator.add(cp.getSed(), "sed", sw);
            InheritedCertificationStatusXmlGenerator.add(cp.getIcs(), "ics", sw);
            sw.writeEndElement();
        }
    }
}
