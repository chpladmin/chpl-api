package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import lombok.extern.log4j.Log4j2;

@Component("testTasksUploadHandler")
@Log4j2
public class TestTaskUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public TestTaskUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
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

        if (uploadUtil.areCollectionsEmpty(ids, descriptions, taskSuccessAvg, taskSuccessStdDev,
                taskPathDevObs, taskPathDevOpt, taskTimeAvg, taskTimeStdDev, taskTimeDevObs,
                taskTimeDevOpt, taskErrorsAvg, taskErrorsStdDev, taskRatingScale, taskRating,
                taskRatingStdDev)) {
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
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return testTasks;
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private TestTask buildTestTask(int index, List<String> ids, List<String> descriptions,
            List<String> taskSuccessAvgs, List<String> taskSuccessStdDevs, List<String> taskPathDevObserveds,
            List<String> taskPathDevOpts, List<String> taskTimeAvgs, List<String> taskTimeStdDevs,
            List<String> taskTimeDevObserveds, List<String> taskTimeDevOpts, List<String> taskErrorsAvgs,
            List<String> taskErrorsStdDevs, List<String> taskRatingScales, List<String> taskRatings,
            List<String> taskRatingStdDevs) {
        String id = (ids != null && ids.size() > index) ? ids.get(index) : null;
        String description = (descriptions != null && descriptions.size() > index) ? descriptions.get(index) : null;
        String taskSuccessAvgAtIndex = (taskSuccessAvgs != null && taskSuccessAvgs.size() > index)
                ? taskSuccessAvgs.get(index) : null;
        Float taskSuccessAvg = null;
        if (!StringUtils.isEmpty(taskSuccessAvgAtIndex)) {
            try {
                taskSuccessAvg = Float.parseFloat(taskSuccessAvgAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskSuccessAvg '" + taskSuccessAvgAtIndex + "' into a Float.");
            }
        }
        String taskSuccessStdDevAtIndex = (taskSuccessStdDevs != null && taskSuccessStdDevs.size() > index)
                ? taskSuccessStdDevs.get(index) : null;
        Float taskSuccessStdDev = null;
        if (!StringUtils.isEmpty(taskSuccessStdDevAtIndex)) {
            try {
                taskSuccessStdDev = Float.parseFloat(taskSuccessStdDevAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskSuccessStdDev '" + taskSuccessStdDevAtIndex + "' into a Float.");
            }
        }
        String taskPathDevObsAtIndex = (taskPathDevObserveds != null && taskPathDevObserveds.size() > index)
                ? taskPathDevObserveds.get(index) : null;
        Integer taskPathDevObs = null;
        if (!StringUtils.isEmpty(taskPathDevObsAtIndex)) {
            try {
                taskPathDevObs = Integer.parseInt(taskPathDevObsAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskPathDevObs '" + taskPathDevObsAtIndex + "' into an Integer.");
                try {
                    taskPathDevObs = Math.round(Float.valueOf(taskPathDevObsAtIndex));
                } catch (NumberFormatException ex2) {
                    LOGGER.debug("Cannot round taskPathDevObs '" + taskPathDevObsAtIndex + "' to an Integer.");
                }
            }
        }
        String taskPathDevOptAtIndex = (taskPathDevOpts != null && taskPathDevOpts.size() > index)
                ? taskPathDevOpts.get(index) : null;
        Integer taskPathDevOpt = null;
        if (!StringUtils.isEmpty(taskPathDevOptAtIndex)) {
            try {
                taskPathDevOpt = Integer.parseInt(taskPathDevOptAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskPathDevOpt '" + taskPathDevOptAtIndex + "' into an Integer.");
                try {
                    taskPathDevOpt = Math.round(Float.valueOf(taskPathDevOptAtIndex));
                } catch (NumberFormatException ex2) {
                    LOGGER.debug("Cannot round taskPathDevOpt '" + taskPathDevOptAtIndex + "' to an Integer.");
                }
            }
        }
        String taskTimeAvgAtIndex = (taskTimeAvgs != null && taskTimeAvgs.size() > index)
                ? taskTimeAvgs.get(index) : null;
        Long taskTimeAvg = null;
        if (!StringUtils.isEmpty(taskTimeAvgAtIndex)) {
            try {
                taskTimeAvg = Long.parseLong(taskTimeAvgAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskTimeAvg '" + taskTimeAvgAtIndex + "' into a Long.");
                try {
                    taskTimeAvg = new Long(Math.round(Float.valueOf(taskTimeAvgAtIndex)));
                } catch (NumberFormatException ex2) {
                    LOGGER.debug("Cannot round taskTimeAvg '" + taskTimeAvgAtIndex + "' to a Long.");
                }
            }
        }
        String taskTimeStdDevAtIndex = (taskTimeStdDevs != null && taskTimeStdDevs.size() > index)
                ? taskTimeStdDevs.get(index) : null;
        Integer taskTimeStdDev = null;
        if (!StringUtils.isEmpty(taskTimeStdDevAtIndex)) {
            try {
                taskTimeStdDev = Integer.parseInt(taskTimeStdDevAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskTimeStdDev '" + taskTimeStdDevAtIndex + "' into an Integer.");
                try {
                    taskTimeStdDev = Math.round(Float.valueOf(taskTimeStdDevAtIndex));
                } catch (NumberFormatException ex2) {
                    LOGGER.debug("Cannot round taskTimeStdDev '" + taskTimeStdDevAtIndex + "' to an Integer.");
                }
            }
        }
        String taskTimeDevObsAtIndex = (taskTimeDevObserveds != null && taskTimeDevObserveds.size() > index)
                ? taskTimeDevObserveds.get(index) : null;
        Integer taskTimeDevObs = null;
        if (!StringUtils.isEmpty(taskTimeDevObsAtIndex)) {
            try {
                taskTimeDevObs = Integer.parseInt(taskTimeDevObsAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskTimeDevObs '" + taskTimeDevObsAtIndex + "' into an Integer.");
                try {
                    taskTimeDevObs = Math.round(Float.valueOf(taskTimeDevObsAtIndex));
                } catch (NumberFormatException ex2) {
                    LOGGER.debug("Cannot round taskTimeDevObs '" + taskTimeDevObsAtIndex + "' to an Integer.");
                }
            }
        }
        String taskTimeDevOptAtIndex = (taskTimeDevOpts != null && taskTimeDevOpts.size() > index)
                ? taskTimeDevOpts.get(index) : null;
        Integer taskTimeDevOpt = null;
        if (!StringUtils.isEmpty(taskTimeDevOptAtIndex)) {
            try {
                taskTimeDevOpt = Integer.parseInt(taskTimeDevOptAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskTimeDevOpt '" + taskTimeDevOptAtIndex + "' into an Integer.");
                try {
                    taskTimeDevOpt = Math.round(Float.valueOf(taskTimeDevOptAtIndex));
                } catch (NumberFormatException ex2) {
                    LOGGER.debug("Cannot round taskTimeDevOpt '" + taskTimeDevOptAtIndex + "' to an Integer.");
                }
            }
        }
        String taskErrorsAvgAtIndex = (taskErrorsAvgs != null && taskErrorsAvgs.size() > index)
                ? taskErrorsAvgs.get(index) : null;
        Float taskErrorsAvg = null;
        if (!StringUtils.isEmpty(taskErrorsAvgAtIndex)) {
            try {
                taskErrorsAvg = Float.parseFloat(taskErrorsAvgAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskErrorsAvg '" + taskErrorsAvgAtIndex + "' into a Float.");
            }
        }
        String taskErrorsStdDevAtIndex = (taskErrorsStdDevs != null && taskErrorsStdDevs.size() > index)
                ? taskErrorsStdDevs.get(index) : null;
        Float taskErrorsStdDev = null;
        if (!StringUtils.isEmpty(taskErrorsStdDevAtIndex)) {
            try {
                taskErrorsStdDev = Float.parseFloat(taskErrorsStdDevAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskErrorsStdDev '" + taskErrorsStdDevAtIndex + "' into a Float.");
            }
        }
        String taskRatingScaleAtIndex = (taskRatingScales != null && taskRatingScales.size() > index)
                ? taskRatingScales.get(index) : null;
        String taskRatingAtIndex = (taskRatings != null && taskRatings.size() > index)
                ? taskRatings.get(index) : null;
        Float taskRating = null;
        if (!StringUtils.isEmpty(taskRatingAtIndex)) {
            try {
                taskRating = Float.parseFloat(taskRatingAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskRating '" + taskRatingAtIndex + "' into a Float.");
            }
        }
        String taskRatingStdDevAtIndex = (taskRatingStdDevs != null && taskRatingStdDevs.size() > index)
                ? taskRatingStdDevs.get(index) : null;
        Float taskRatingStdDev = null;
        if (!StringUtils.isEmpty(taskRatingStdDevAtIndex)) {
            try {
                taskRatingStdDev = Float.parseFloat(taskRatingStdDevAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse taskRatingStdDev '" + taskRatingStdDevAtIndex + "' into a Float.");
            }
        }

        if (StringUtils.isAllEmpty(id, description, taskSuccessAvgAtIndex,
                taskSuccessStdDevAtIndex, taskPathDevObsAtIndex, taskPathDevOptAtIndex,
                taskTimeAvgAtIndex, taskTimeStdDevAtIndex, taskTimeDevObsAtIndex,
                taskTimeDevOptAtIndex, taskErrorsAvgAtIndex, taskErrorsStdDevAtIndex,
                taskRatingScaleAtIndex, taskRatingAtIndex, taskRatingStdDevAtIndex)) {
            return null;
        }

        return TestTask.builder()
                .uniqueId(id)
                .description(description)
                .taskSuccessAverage(taskSuccessAvg)
                .taskSuccessAverageStr(taskSuccessAvgAtIndex)
                .taskSuccessStddev(taskSuccessStdDev)
                .taskSuccessStddevStr(taskSuccessStdDevAtIndex)
                .taskPathDeviationObserved(taskPathDevObs)
                .taskPathDeviationObservedStr(taskPathDevObsAtIndex)
                .taskPathDeviationOptimal(taskPathDevOpt)
                .taskPathDeviationOptimalStr(taskPathDevOptAtIndex)
                .taskTimeAvg(taskTimeAvg)
                .taskTimeAvgStr(taskTimeAvgAtIndex)
                .taskTimeStddev(taskTimeStdDev)
                .taskTimeStddevStr(taskTimeStdDevAtIndex)
                .taskTimeDeviationObservedAvg(taskTimeDevObs)
                .taskTimeDeviationObservedAvgStr(taskTimeDevObsAtIndex)
                .taskTimeDeviationOptimalAvg(taskTimeDevOpt)
                .taskTimeDeviationOptimalAvgStr(taskTimeDevOptAtIndex)
                .taskErrors(taskErrorsAvg)
                .taskErrorsStr(taskErrorsAvgAtIndex)
                .taskErrorsStddev(taskErrorsStdDev)
                .taskErrorsStddevStr(taskErrorsStdDevAtIndex)
                .taskRatingScale(taskRatingScaleAtIndex)
                .taskRating(taskRating)
                .taskRatingStr(taskRatingAtIndex)
                .taskRatingStddev(taskRatingStdDev)
                .taskRatingStddevStr(taskRatingStdDevAtIndex)
                .build();
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
                Heading.TASK_ID, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseDescription(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_DESCRIPTION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskSuccessAvg(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_SUCCESS_MEAN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskSuccessStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_SUCCESS_STDDEV, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskPathDeviationObserved(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_PATH_DEV_OBS, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskPathDeviationOptimal(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_PATH_DEV_OPT, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeAvg(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_TIME_MEAN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_TIME_STDDEV, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeDeviationObserved(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_TIME_DEV_OBS, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskTimeDeviationOptimal(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_TIME_DEV_OPT, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskErrorsAvg(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_ERRORS_MEAN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskErrorsStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_ERRORS_STDDEV, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskRatingScale(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_RATING_SCALE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskRating(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_RATING, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseTaskRatingStdDev(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.TASK_RATING_STDDEV, headingRecord, listingRecords);
        return values;
    }
}
