package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CQMResultDetails;

public class CqmResultDetailsXmlGenerator extends XmlGenerator {
    public static void add(List<CQMResultDetails> details, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (details != null) {
            sw.writeStartElement(rootNodeName);
            for (CQMResultDetails detail : details) {
                add(detail, "cqmResult", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CQMResultDetails detail, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (detail != null) {
            sw.writeStartElement(rootNodeName);
            if (detail.getAllVersions() != null) {
                sw.writeStartElement("allVersions");
                for (String version : detail.getAllVersions()) {
                    createSimpleElement(version, "version", sw);
                }
                sw.writeEndElement();
            }
            createSimpleElement(detail.getCmsId(), "cmsId", sw);
            CqmResultCertificationXmlGenerator.add(detail.getCriteria(), "criteriaList", sw);
            createSimpleElement(detail.getDescription(), "description", sw);
            createSimpleElement(detail.getDomain(), "domain", sw);
            createSimpleElement(detail.getId(), "id", sw);
            createSimpleElement(detail.getNqfNumber(), "nqfNumber", sw);
            createSimpleElement(detail.getNumber(), "number", sw);
            createSimpleElement(detail.isSuccess(), "success", sw);
            if (detail.getSuccessVersions() != null) {
                sw.writeStartElement("successVersions");
                for (String version : detail.getSuccessVersions()) {
                    createSimpleElement(version, "version", sw);
                }
                sw.writeEndElement();
            }
            createSimpleElement(detail.getTitle(), "title", sw);
            createSimpleElement(detail.getTypeId(), "typeId", sw);
            sw.writeEndElement();
        }
    }
}
