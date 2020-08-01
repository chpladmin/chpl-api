package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;

public class TestTaskXmlGenerator extends XmlGenerator {
    public static void add(List<TestTask> tasks, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tasks != null) {
            sw.writeStartElement(rootNodeName);
            for (TestTask task : tasks) {
                add(task, "testTask", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(TestTask task, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (task != null) {
            sw.writeStartElement(rootNodeName);
            CertificationCriterionXmlGenerator.add(new ArrayList<CertificationCriterion>(task.getCriteria()), "criteria", sw);
            createSimpleElement(task.getDescription(), "description", sw);
            createSimpleElement(task.getId(), "id", sw);
            TestParticipantXmlGenerator.add(new ArrayList<TestParticipant>(task.getTestParticipants()), "participants", sw);
            createSimpleElement(task.getTaskErrors(), "taskErrors", sw);
            createSimpleElement(task.getTaskErrorsStddev(), "taskErrorsStddev", sw);
            createSimpleElement(task.getTaskPathDeviationObserved(), "taskPathDeviationObserved", sw);
            createSimpleElement(task.getTaskPathDeviationOptimal(), "taskPathDeviationOptimal", sw);
            createSimpleElement(task.getTaskRating(), "taskRating", sw);
            createSimpleElement(task.getTaskRatingScale(), "taskRatingScale", sw);
            createSimpleElement(task.getTaskRatingStddev(), "taskRatingStddev", sw);
            createSimpleElement(task.getTaskSuccessAverage(), "taskSuccessAverage", sw);
            createSimpleElement(task.getTaskSuccessStddev(), "taskSuccessStddev", sw);
            createSimpleElement(task.getTaskTimeDeviationObservedAvg(), "taskTimeDeviationObservedAvg", sw);
            createSimpleElement(task.getTaskTimeDeviationOptimalAvg(), "taskTimeDeviationOptimalAvg", sw);
            createSimpleElement(task.getTaskTimeStddev(), "taskTimeStddev", sw);
            createSimpleElement(task.getUniqueId(), "uniqueId", sw);
            sw.writeEndElement();
        }
    }
}
