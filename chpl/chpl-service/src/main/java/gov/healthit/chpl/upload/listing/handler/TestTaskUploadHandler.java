package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("testTasksUploadHandler")
@Log4j2
public class TestTaskUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestTaskUploadHandler(ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public List<TestTask> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<TestTask> testTasks = new ArrayList<TestTask>();
        List<String> ids = parseIds(headingRecord, listingRecords);
        List<String> descriptions = parseDescription(headingRecord, listingRecords);
        List<String> taskSuccessAvg = parseTaskSuccessAvg(headingRecord, listingRecords);
        List<String> taskSuccessStdDev = parseTaskSuccessStdDev(headingRecord, listingRecords);
        List<String> taskPathDevObs = parseTaskPathDeviationObserved(headingRecord, listingRecords);
        List<String> taskPathDevOpt = parseTaskPathDeviationOptimal(headingRecord, listingRecords);
        List<String> taskTimeAvg = parseTaskTimeAvg(headingRecord, listingRecords);
        List<String> taskTimeStdDev = parseTaskTimeStdDev(headingRecord, listingRecords);
        List<String> taskTimeDevObs = parseTaskTimeDeviationObserved(headingRecord, listingRecords);
        List<String> taskTimeDevOpt = parseTaskTimeDeviationOptimal(headingRecord, listingRecords);
        List<String> taskErrorsAvg = parseTaskErrorsAvg(headingRecord, listingRecords);
        List<String> taskErrorsStdDev = parseTaskErrorsStdDev(headingRecord, listingRecords);
        List<String> taskRatingScale = parseTaskRatingScale(headingRecord, listingRecords);
        List<String> taskRating = parseTaskRating(headingRecord, listingRecords);
        List<String> taskRatingStdDev = parseTaskRatingStdDev(headingRecord, listingRecords);

        if (CollectionUtils.isEmpty(ids) && CollectionUtils.isEmpty(descriptions)
                && CollectionUtils.isEmpty(taskSuccessAvg) && CollectionUtils.isEmpty(taskSuccessStdDev)
                && CollectionUtils.isEmpty(taskPathDevObs) && CollectionUtils.isEmpty(taskPathDevOpt)
                && CollectionUtils.isEmpty(taskTimeAvg) && CollectionUtils.isEmpty(taskTimeStdDev)
                && CollectionUtils.isEmpty(taskTimeDevObs) && CollectionUtils.isEmpty(taskTimeDevOpt)
                && CollectionUtils.isEmpty(taskErrorsAvg) && CollectionUtils.isEmpty(taskErrorsStdDev)
                && CollectionUtils.isEmpty(taskRatingScale) && CollectionUtils.isEmpty(taskRating)
                && CollectionUtils.isEmpty(taskRatingStdDev)) {
            return testTasks;
        }

        int max = calculateMaxListSize(ids, descriptions, taskSuccessAvg, taskSuccessStdDev,
                taskPathDevObs, taskPathDevOpt, taskTimeAvg, taskTimeStdDev, taskTimeDevObs,
                taskTimeDevOpt, taskErrorsAvg, taskErrorsStdDev, taskRatingScale, taskRating,
                taskRatingStdDev);

        //I think everything remains ordered using these data structures so this should be okay.
        testTasks = IntStream.range(0, max)
            .mapToObj(index -> buildTestTask(index, ids, descriptions, taskSuccessAvg, taskSuccessStdDev,
                    taskPathDevObs, taskPathDevOpt, taskTimeAvg, taskTimeStdDev, taskTimeDevObs,
                    taskTimeDevOpt, taskErrorsAvg, taskErrorsStdDev, taskRatingScale, taskRating,
                    taskRatingStdDev))
            .collect(Collectors.toList());
        return testTasks;
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private TestTask buildTestTask(int index, List<String> ids, List<String> descriptions,
            List<String> taskSuccessAvg, List<String> taskSuccessStdDev, List<String> taskPathDevObs,
            List<String> taskPathDevOpt, List<String> taskTimeAvg, List<String> taskTimeStdDev,
            List<String> taskTimeDevObs, List<String> taskTimeDevOpt, List<String> taskErrorsAvg,
            List<String> taskErrorsStdDev, List<String> taskRatingScale, List<String> taskRating,
            List<String> taskRatingStdDev) {
        String id = (ids != null && ids.size() > index) ? ids.get(index) : null;
        String description = (descriptions != null && descriptions.size() > index) ? descriptions.get(index) : null;
        String taskSuccessAvgAtIndex = (taskSuccessAvg != null && taskSuccessAvg.size() > index)
                ? taskSuccessAvg.get(index) : null;
        String taskSuccessStdDevAtIndex = (taskSuccessStdDev != null && taskSuccessStdDev.size() > index)
                ? taskSuccessStdDev.get(index) : null;
        String taskPathDevObsAtIndex = (taskPathDevObs != null && taskPathDevObs.size() > index)
                ? taskPathDevObs.get(index) : null;
        String taskPathDevOptAtIndex = (taskPathDevOpt != null && taskPathDevOpt.size() > index)
                ? taskPathDevOpt.get(index) : null;
        String taskTimeAvgAtIndex = (taskTimeAvg != null && taskTimeAvg.size() > index)
                ? taskTimeAvg.get(index) : null;
        String taskTimeStdDevAtIndex = (taskTimeStdDev != null && taskTimeStdDev.size() > index)
                ? taskTimeStdDev.get(index) : null;
        String taskTimeDevObsAtIndex = (taskTimeDevObs != null && taskTimeDevObs.size() > index)
                ? taskTimeDevObs.get(index) : null;
        String taskTimeDevOptAtIndex = (taskTimeDevOpt != null && taskTimeDevOpt.size() > index)
                ? taskTimeDevOpt.get(index) : null;
        String taskErrorsAvgAtIndex = (taskErrorsAvg != null && taskErrorsAvg.size() > index)
                ? taskErrorsAvg.get(index) : null;
        String taskErrorsStdDevAtIndex = (taskErrorsStdDev != null && taskErrorsStdDev.size() > index)
                ? taskErrorsStdDev.get(index) : null;
        String taskRatingScaleAtIndex = (taskRatingScale != null && taskRatingScale.size() > index)
                ? taskRatingScale.get(index) : null;
        String taskRatingAtIndex = (taskRating != null && taskRating.size() > index)
                ? taskRating.get(index) : null;
        String taskRatingStdDevAtIndex = (taskRatingStdDev != null && taskRatingStdDev.size() > index)
                ? taskRatingStdDev.get(index) : null;

        TestTask tt = TestTask.builder()
                .uniqueId(id)
                .description(description)
                //TODO: need some temporary/transient fields to hold the inputs as string values
                .build();
        return tt;
    }

    @SafeVarargs
    private final int calculateMaxListSize(List<String>... lists) {
        int max = 0;
        for (List<String> list : lists) {
            if (!CollectionUtils.isEmpty(list)) {
                max = Math.max(max, list.size());
            }
        }
        return max;
    }

    private List<String> parseIds(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_ID, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseDescription(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_DESCRIPTION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskSuccessAvg(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_SUCCESS_MEAN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskSuccessStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_SUCCESS_STDDEV, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskPathDeviationObserved(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_PATH_DEV_OBS, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskPathDeviationOptimal(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_PATH_DEV_OPT, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeAvg(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_TIME_MEAN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_TIME_STDDEV, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeDeviationObserved(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_TIME_DEV_OBS, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeDeviationOptimal(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_TIME_DEV_OPT, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskErrorsAvg(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_ERRORS_MEAN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskErrorsStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_ERRORS_STDDEV, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskRatingScale(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_RATING_SCALE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskRating(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_RATING, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskRatingStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TASK_RATING_STDDEV, headingRecord, listingRecords);
        return values;
    }
}
