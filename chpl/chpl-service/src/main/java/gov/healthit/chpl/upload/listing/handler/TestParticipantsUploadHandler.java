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

import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantAge;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantEducation;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import lombok.extern.log4j.Log4j2;

@Component("testParticipantsUploadHandler")
@Log4j2
public class TestParticipantsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public TestParticipantsUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<TestParticipant> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<TestParticipant> testParticipants = new ArrayList<TestParticipant>();
        List<String> ids = parseIds(headingRecord, listingRecords);
        List<String> genders = parseGenders(headingRecord, listingRecords);
        List<String> ages = parseAges(headingRecord, listingRecords);
        List<String> educations = parseEducations(headingRecord, listingRecords);
        List<String> occupations = parseOccupations(headingRecord, listingRecords);
        List<String> professionalExperience = parseProfessionalExperience(headingRecord, listingRecords);
        List<String> computerExperience = parseComputerExperience(headingRecord, listingRecords);
        List<String> productExperience = parseProductExperience(headingRecord, listingRecords);
        List<String> assistiveTech = parseAssistiveTechnologyNeeds(headingRecord, listingRecords);

        if (uploadUtil.areCollectionsEmpty(ids, genders, ages, educations, occupations,
                professionalExperience, computerExperience, productExperience, assistiveTech)) {
            return testParticipants;
        }

        int max = calculateMaxListSize(ids, genders, ages, educations, occupations,
                professionalExperience, computerExperience, productExperience, assistiveTech);

        //I think everything remains ordered using these data structures so this should be okay.
        testParticipants = IntStream.range(0, max)
            .mapToObj(index -> buildTestParticipant(index, ids, genders, ages,
                    educations, occupations, professionalExperience, computerExperience,
                    productExperience, assistiveTech))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return testParticipants;
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private TestParticipant buildTestParticipant(int index, List<String> ids, List<String> genders,
            List<String> ages, List<String> educations, List<String> occupations,
            List<String> professionalExperience, List<String> computerExperience,
            List<String> productExperience, List<String> assistiveTech) {
        String id = (ids != null && ids.size() > index) ? ids.get(index) : null;
        String gender = (genders != null && genders.size() > index) ? genders.get(index) : null;
        String age = (ages != null && ages.size() > index) ? ages.get(index) : null;
        String education = (educations != null && educations.size() > index)
                ? educations.get(index) : null;
        String occupation = (occupations != null && occupations.size() > index)
                ? occupations.get(index) : null;
        String professionalExperienceAtIndex = (professionalExperience != null && professionalExperience.size() > index)
                ? professionalExperience.get(index) : null;
        Integer professionalExperienceMonths = null;
        if (!StringUtils.isEmpty(professionalExperienceAtIndex)) {
            try {
                professionalExperienceMonths = Integer.parseInt(professionalExperienceAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse professional experience '" + professionalExperienceAtIndex + "' into an Integer.");
            }
        }
        String computerExperienceAtIndex = (computerExperience != null && computerExperience.size() > index)
                ? computerExperience.get(index) : null;
        Integer computerExperienceMonths = null;
        if (!StringUtils.isEmpty(computerExperienceAtIndex)) {
            try {
                computerExperienceMonths = Integer.parseInt(computerExperienceAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse computer experience '" + computerExperienceAtIndex + "' into an Integer.");
            }
        }
        String productExperienceAtIndex = (productExperience != null && productExperience.size() > index)
                ? productExperience.get(index) : null;
        Integer productExperienceMonths = null;
        if (!StringUtils.isEmpty(productExperienceAtIndex)) {
            try {
                productExperienceMonths = Integer.parseInt(productExperienceAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Cannot parse product experience '" + productExperienceAtIndex + "' into an Integer.");
            }
        }
        String assistiveTechAtIndex = (assistiveTech != null && assistiveTech.size() > index)
                ? assistiveTech.get(index) : null;

        if (StringUtils.isAllEmpty(id, gender, age, education, occupation, professionalExperienceAtIndex,
                computerExperienceAtIndex, productExperienceAtIndex, assistiveTechAtIndex)) {
            return null;
        }

        return TestParticipant.builder()
                .uniqueId(id)
                .gender(gender)
                .age(TestParticipantAge.builder().name(age).build())
                .ageRange(age)
                .educationType(TestParticipantEducation.builder().name(education).build())
                .educationTypeName(education)
                .occupation(occupation)
                .professionalExperienceMonths(professionalExperienceMonths)
                .professionalExperienceMonthsStr(professionalExperienceAtIndex)
                .computerExperienceMonths(computerExperienceMonths)
                .computerExperienceMonthsStr(computerExperienceAtIndex)
                .productExperienceMonths(productExperienceMonths)
                .productExperienceMonthsStr(productExperienceAtIndex)
                .assistiveTechnologyNeeds(assistiveTechAtIndex)
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
                Heading.PARTICIPANT_ID, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseGenders(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_GENDER, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseAges(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_AGE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseEducations(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_EDUCATION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseOccupations(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_OCCUPATION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseProfessionalExperience(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_PROFESSIONAL_EXPERIENCE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseComputerExperience(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_COMPUTER_EXPERIENCE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseProductExperience(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_PRODUCT_EXPERIENCE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseAssistiveTechnologyNeeds(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Heading.PARTICIPANT_ASSISTIVE_TECH, headingRecord, listingRecords);
        return values;
    }
}
