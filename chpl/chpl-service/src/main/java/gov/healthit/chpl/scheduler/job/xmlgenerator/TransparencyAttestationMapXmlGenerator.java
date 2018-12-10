package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TransparencyAttestationMap;

public class TransparencyAttestationMapXmlGenerator extends XmlGenerator {
    public static void add(List<TransparencyAttestationMap> list, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (list != null && list.size() > 0) {
            sw.writeStartElement(rootNodeName);
            for (TransparencyAttestationMap map : list) {
                add(map, "transparencyAttestationMap", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(TransparencyAttestationMap map, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (map != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(map.getAcbId(), "acbId", sw);
            createSimpleElement(map.getAcbName(), "acbName", sw);
            sw.writeEndElement();
        }
    }
}
