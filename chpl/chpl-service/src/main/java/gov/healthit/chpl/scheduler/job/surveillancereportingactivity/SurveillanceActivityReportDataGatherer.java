package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.SurveillanceManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SurveillanceActivityReportDataGatherer {

    @Autowired
    private SurveillanceManager surveillanceManager;

    public List<CSVRecord> getData(LocalDate startDate, LocalDate endDate) throws Exception {
        return StreamSupport.stream(getIterableCsvRecords(getSurveillanceCsv()).spliterator(), false)
                .filter(record -> isSurveillanceOpenBetweenDates(
                        getLocalDate(record.get("SURVEILLANCE_BEGAN"), LocalDate.MIN),
                        getLocalDate(record.get("SURVEILLANCE_ENDED"), LocalDate.MAX),
                        startDate, endDate))
                .collect(Collectors.toList());
    }

    private Boolean isSurveillanceOpenBetweenDates(LocalDate surveillanceStartDate, LocalDate surveillanceEndDate,
            LocalDate startDate, LocalDate endDate) {

        //Logic taken from https://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
        return startDate.isBefore(surveillanceEndDate) && surveillanceStartDate.isBefore(endDate);
    }

    private String getSurveillanceCsv() throws IOException {
        return surveillanceManager.getAllSurveillanceDownloadFile().getPath();
    }

    private LocalDate getLocalDate(String toConvert, LocalDate defaultWhenInvalid) {
        if (StringUtils.isEmpty(toConvert)) {
            return defaultWhenInvalid;
        }

        try {
            return LocalDate.parse(toConvert, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException e) {
            LOGGER.error("Could not parse date: " + toConvert);
            return defaultWhenInvalid;
        }
    }

    private Iterable<CSVRecord> getIterableCsvRecords(String fileName) throws FileNotFoundException, IOException  {
        return CSVFormat
                .DEFAULT
                .withHeader()
                .parse(new BufferedReader(new FileReader(fileName)));
    }
}
