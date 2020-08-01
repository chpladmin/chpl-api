package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TestParticipant;

public class TestParticipantXmlGenerator extends XmlGenerator {
    public static void add(List<TestParticipant> participants, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (participants != null) {
            sw.writeStartElement(rootNodeName);
            for (TestParticipant participant : participants) {
                add(participant, "participant", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(TestParticipant participant, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (participant != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(participant.getAgeRange(), "ageRange", sw);
            createSimpleElement(participant.getAgeRangeId(), "ageRangeId", sw);
            createSimpleElement(participant.getAssistiveTechnologyNeeds(), "assistiveTechnologyNeeds", sw);
            createSimpleElement(participant.getComputerExperienceMonths(), "computerExperienceMonths", sw);
            createSimpleElement(participant.getEducationTypeId(), "educationTypeId", sw);
            createSimpleElement(participant.getEducationTypeName(), "educationTypeName", sw);
            createSimpleElement(participant.getGender(), "gender", sw);
            createSimpleElement(participant.getId(), "id", sw);
            createSimpleElement(participant.getOccupation(), "occupation", sw);
            createSimpleElement(participant.getProductExperienceMonths(), "productExperienceMonths", sw);
            createSimpleElement(participant.getProfessionalExperienceMonths(), "professionalExperienceMonths", sw);
            createSimpleElement(participant.getUniqueId(), "uniqueId", sw);

            sw.writeEndElement();
        }
    }
}
