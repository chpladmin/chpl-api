package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class CertifiedProductSearchDetailsXmlGenerator extends XmlGenerator {

    public static void add(List<CertifiedProductSearchDetails> cps, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (cps != null) {
            sw.writeStartElement(rootNodeName);

            for (CertifiedProductSearchDetails cp : cps) {
                add(cp, "listing", sw);
            }
            sw.writeEndElement();
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    public static void add(CertifiedProductSearchDetails cp, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cp != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(cp.getAcbCertificationId(), "acbCertificationId", sw);
            createSimpleElement(cp.getAccessibilityCertified(), "accessibilityCertified", sw);
            CertifiedProductAccessibilityStandardXmlGenerator.addAccessibilityStandards(cp.getAccessibilityStandards(),
                    "accessibilityStandards", sw);
            createSimpleElement(cp.getCertificationDate(), "certificationDate", sw);
            if (cp.getCertificationEdition() != null && cp.getCertificationEdition().size() > 0) {
                sw.writeStartElement("certificationEdition");
                for (Entry<String, Object> entry : cp.getCertificationEdition().entrySet()) {
                    sw.writeStartElement("entry");
                    if (entry.getKey() != null) {
                        createSimpleElement(entry.getKey(), "key", sw);
                    }
                    if (entry.getValue() != null) {
                        createSimpleElement(entry.getValue().toString(), "value", sw);
                    }
                    sw.writeEndElement();
                }
                sw.writeEndElement();
            }
            CertificationStatusEventXmlGenerator.add(cp.getCertificationEvents(), "certificationEvents", sw);
            CertificationResultXmlGenerator.add(cp.getCertificationResults(), "certificationResults", sw);
            if (cp.getCertifyingBody() != null && cp.getCertifyingBody().size() > 0) {
                sw.writeStartElement("certifyingBody");
                for (Entry<String, Object> entry : cp.getCertifyingBody().entrySet()) {
                    sw.writeStartElement("entry");
                    if (entry.getKey() != null) {
                        createSimpleElement(entry.getKey(), "key", sw);
                    }
                    if (entry.getValue() != null) {
                        createSimpleElement(entry.getValue().toString(), "value", sw);
                    }
                    sw.writeEndElement();
                }
                sw.writeEndElement();
            }
            createSimpleElement(cp.getChplProductNumber(), "chplProductNumber", sw);
            if (cp.getClassificationType() != null && cp.getClassificationType().size() > 0) {
                sw.writeStartElement("classificationType");
                for (Entry<String, Object> entry : cp.getClassificationType().entrySet()) {
                    sw.writeStartElement("entry");
                    if (entry.getKey() != null) {
                        createSimpleElement(entry.getKey(), "key", sw);
                    }
                    if (entry.getValue() != null) {
                        createSimpleElement(entry.getValue().toString(), "value", sw);
                    }
                    sw.writeEndElement();
                }
                sw.writeEndElement();
            }
            createSimpleElement(cp.getCountCerts(), "countCerts", sw);
            createSimpleElement(cp.getCountClosedNonconformities(), "countClosedNonconformities", sw);
            createSimpleElement(cp.getCountClosedSurveillance(), "countClosedSurveillance", sw);
            createSimpleElement(cp.getCountCqms(), "countCqms", sw);
            createSimpleElement(cp.getCountOpenNonconformities(), "countOpenNonconformities", sw);
            createSimpleElement(cp.getCountOpenSurveillance(), "countOpenSurveillance", sw);
            createSimpleElement(cp.getCountSurveillance(), "countSurveillance", sw);
            CqmResultDetailsXmlGenerator.add(cp.getCqmResults(), "cqmResults", sw);
            createSimpleElement(cp.getCuresUpdate() == null ? false : cp.getCuresUpdate(), "curesUpdate", sw);
            createSimpleElement(cp.getDecertificationDate(), "decertificationDate", sw);
            DeveloperXmlGenerator.addDeveloper(cp.getDeveloper(), "developer", sw);
            DirectReviewXmlGenerator.add(cp.getDirectReviews(), "directReviews", sw);
            InheritedCertificationStatusXmlGenerator.add(cp.getIcs(), "ics", sw);
            createSimpleElement(cp.getId(), "id", sw);
            createSimpleElement(cp.getLastModifiedDate(), "lastModifiedDate", sw);
            PromotingInteroperabilityXmlGenerator.add(cp.getPromotingInteroperabilityUserHistory(), "promotingInteroperabilityUserHistory", sw);
            ListingMeasureXmlGenerator.add(cp.getMeasures(), "measures", sw);
            createSimpleElement(cp.getOtherAcb(), "otherAcb", sw);
            if (cp.getPracticeType() != null && cp.getPracticeType().size() > 0) {
                sw.writeStartElement("practiceType");
                for (Entry<String, Object> entry : cp.getPracticeType().entrySet()) {
                    sw.writeStartElement("entry");
                    if (entry.getKey() != null) {
                        createSimpleElement(entry.getKey(), "key", sw);
                    }
                    if (entry.getValue() != null) {
                        createSimpleElement(entry.getValue().toString(), "value", sw);
                    }
                    sw.writeEndElement();
                }
                sw.writeEndElement();
            }
            ProductXmlGenerator.addProduct(cp.getProduct(), "product", sw);
            createSimpleElement(cp.getProductAdditionalSoftware(), "productAdditionalSoftware", sw);
            CertifiedProductQmsStandardXmlGenerator.add(cp.getQmsStandards(), "qmsStandards", sw);
            createSimpleElement(cp.getReportFileLocation(), "reportFileLocation", sw);
            createSimpleElement(cp.getRwtEligibilityYear(), "rwtEligibilityYear", sw);
            createSimpleElement(Objects.nonNull(cp.getRwtPlansCheckDate()) ? cp.getRwtPlansCheckDate().toString() : null, "rwtPlansCheckDate", sw);
            createSimpleElement(cp.getRwtPlansUrl(), "rwtPlansUrl", sw);
            createSimpleElement(Objects.nonNull(cp.getRwtResultsCheckDate()) ? cp.getRwtResultsCheckDate().toString() : null, "rwtResultsCheckDate", sw);
            createSimpleElement(cp.getRwtResultsUrl(), "rwtResultsUrl", sw);
            CertifiedProductSedXmlGenerator.add(cp.getSed(), "sed", sw);
            createSimpleElement(cp.getSedIntendedUserDescription(), "sedIntendedUserDescription", sw);
            createSimpleElement(cp.getSedReportFileLocation(), "sedReportFileLocation", sw);
            createSimpleElement(cp.getSedTestingEndDate(), "sedTestingEndDate", sw);
            SurveillanceXmlGenerator.add(cp.getSurveillance(), "surveillanceList", sw);
            createSimpleElement(cp.getSvapNoticeUrl(), "svapNoticeUrl", sw);
            CertifiedProductTargetedUserXmlGenerator.add(cp.getTargetedUsers(), "targetedUsers", sw);
            CertifiedProductTestingLabXmlGenerator.addTestingLabs(cp.getTestingLabs(), "testingLabs", sw);
            TransparencyAttestationXmlGenerator.add(cp.getTransparencyAttestation(), "transparencyAttestation", sw);
            createSimpleElement(cp.getTransparencyAttestationUrl(), "transparencyAttestationUrl", sw);
            ProductVersionXmlGenerator.addProductVersion(cp.getVersion(), "version", sw);

            sw.writeEndElement();
        }
    }
}
