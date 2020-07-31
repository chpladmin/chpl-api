package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProduct;

public class CertifiedProductXmlGenerator extends XmlGenerator {
    public static void add(List<CertifiedProduct> products, String rootNodeName,
            String childNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (products != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProduct product : products) {
                add(product, childNodeName, sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertifiedProduct cp, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cp != null && cp.getId() != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(cp.getCertificationDate(), "certificationDate", sw);
            createSimpleElement(cp.getCertificationStatus(), "certificationStatus", sw);
            createSimpleElement(cp.getChplProductNumber(), "chplProductNumber", sw);
            createSimpleElement(cp.getCuresUpdate() == null ? false : cp.getCuresUpdate(), "curesUpdate", sw);
            createSimpleElement(cp.getEdition(), "edition", sw);
            createSimpleElement(cp.getId(), "id", sw);
            createSimpleElement(cp.getLastModifiedDate(), "lastModifiedDate", sw);
            sw.writeEndElement();
        }
    }

}

