package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.Surveillance;

/**
 * Generate &lt;surveillances&gt;&lt;surveillance&gt;&lt;/surveillance&gt;&lt;/surveillances&gt; nodes.
 */
public class SurveillanceXmlGenerator extends XmlGenerator {
    /**
     * Add surveillances to root.
     * @param surveillances surveillances to add
     * @param rootNodeName root node name
     * @param sw stream writer
     * @throws XMLStreamException if exception during writing process
     */
    public static void add(final List<Surveillance> surveillances, final String rootNodeName, final XMLStreamWriter sw)
            throws XMLStreamException {
        if (surveillances != null) {
            sw.writeStartElement(rootNodeName);
            for (Surveillance s : surveillances) {
                add(s, "surveillance", sw);
            }
            sw.writeEndElement();
        }
    }

    /**
     * Add a single surveillance.
     * @param surveillance the surveillance
     * @param rootNodeName root node name
     * @param sw stream writer
     * @throws XMLStreamException if exception during writing process
     */
    public static void add(final Surveillance surveillance, final String rootNodeName, final XMLStreamWriter sw)
            throws XMLStreamException {
        if (surveillance != null) {
            sw.writeStartElement(rootNodeName);
            CertifiedProductXmlGenerator.add(surveillance.getCertifiedProduct(), "certifiedProduct", sw);
            createSimpleElement(surveillance.getEndDay(), "endDay", sw);
            createSimpleElement(surveillance.getFriendlyId(), "friendlyId", sw);
            createSimpleElement(surveillance.getId(), "id", sw);
            createSimpleElement(surveillance.getLastModifiedDate(), "lastModifiedDate", sw);
            createSimpleElement(surveillance.getRandomizedSitesUsed(), "randomizedSitesUsed", sw);
            SurveillanceRequirementXmlGenerator.add(surveillance.getRequirements(), "surveilledRequirements", sw);
            createSimpleElement(surveillance.getStartDay(), "startDay", sw);
            SurveillanceTypeXmlGenerator.addSurveillanceType(surveillance.getType(), "type", sw);
            sw.writeEndElement();
        }
    }
}
