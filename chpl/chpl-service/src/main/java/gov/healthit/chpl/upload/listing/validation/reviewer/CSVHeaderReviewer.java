package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CSVHeaderReviewer {
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CSVHeaderReviewer(ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public void review(ListingUpload uploadedMetadata, CertifiedProductSearchDetails listing) {
        if (uploadedMetadata.getRecords() == null || uploadedMetadata.getRecords().size() == 0) {
            return;
        }
        CSVRecord heading = uploadUtil.getHeadingRecord(uploadedMetadata.getRecords());
        reviewUnrecognizedHeadings(listing, heading);
        reviewDuplicateHeadings(listing, heading);
    }

    private void reviewUnrecognizedHeadings(CertifiedProductSearchDetails listing, CSVRecord heading) {
        heading.forEach(headingVal -> {
            if (!StringUtils.isEmpty(headingVal) && Headings.getHeading(headingVal) == null) {
                listing.getWarningMessages().add(msgUtil.getMessage("listing.upload.unrecognizedHeading", headingVal));
            }
        });
    }

    private void reviewDuplicateHeadings(CertifiedProductSearchDetails listing, CSVRecord headingRecord) {
        List<String> allHeadingColumns = uploadUtil.convertToList(headingRecord);
        List<String> allCriteriaColumns = new ArrayList<String>();
        //get each criteria heading start/end, check for duplicates
        int nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        while (nextCertResultIndex >= 0) {
            List<CSVRecord> parsedCertResultRecords = uploadUtil.getCertificationResultRecordsFromIndex(
                    nextCertResultIndex, headingRecord, Collections.emptyList());
            CSVRecord certHeadingRecord = uploadUtil.getHeadingRecord(parsedCertResultRecords);

            //add error messages for this set of cert result headings
            listing.getErrorMessages().addAll(getDuplicateCriteriaLevelHeadingMessages(uploadUtil.convertToList(certHeadingRecord), certHeadingRecord.get(0)));
            //remove these items from the set of all columns so we don't check them again
            allCriteriaColumns.add(allHeadingColumns.get(nextCertResultIndex));
            allHeadingColumns.subList(nextCertResultIndex, nextCertResultIndex + certHeadingRecord.size()).clear();
            headingRecord = uploadUtil.convertToCsvRecord(allHeadingColumns);
            nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        }
        //Note that if there were any listing-level headings (like VENDOR__C) that were duplicated
        //at the certification result level (like if it was put in between A_1 and A_2) then
        //it won't get counted as a duplicate. I'm not sure how to deal with this and the root cause
        //of the difficulty is there are two headings TASK_ID and PARTICIPANT_ID that can be found
        //at both the listing level and criteria level.

        //look for duplicates outside of criteria headings
        listing.getErrorMessages().addAll(getDuplicateHeadingMessages(allHeadingColumns));
        //look for duplicate criteria headings
        listing.getErrorMessages().addAll(getDuplicateHeadingMessages(allCriteriaColumns));
    }

    private Set<String> getDuplicateHeadingMessages(List<String> headings) {
        Set<String> messages = new LinkedHashSet<String>();
        Set<String> duplicates = findDuplicates(headings);
        duplicates.stream().forEach(
                duplicate -> messages.add(msgUtil.getMessage("listing.upload.duplicateHeading", duplicate)));
        return messages;
    }

    private Set<String> getDuplicateCriteriaLevelHeadingMessages(List<String> headings, String criteriaCol) {
        Set<String> messages = new LinkedHashSet<String>();
        Set<String> duplicates = findDuplicates(headings);
        duplicates.stream().forEach(
                duplicate -> messages.add(msgUtil.getMessage("listing.upload.duplicateCriteriaHeading", duplicate, criteriaCol)));
        return messages;
    }

    public Set<String> findDuplicates(List<String> originalValues) {
        Set<String> setOfDuplicates = new LinkedHashSet<String>();
        Set<Headings> setToTest = new LinkedHashSet<Headings>();
        for (String value : originalValues) {
            if (!StringUtils.isEmpty(value) && Headings.getHeading(value) != null) {
                if (!setToTest.add(Headings.getHeading(value))) {
                    setOfDuplicates.add(value);
                }
            }
        }
        return setOfDuplicates;
    }
}
