package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
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

        if (CollectionUtils.isEmpty(ids) && CollectionUtils.isEmpty(genders)
                && CollectionUtils.isEmpty(ages) && CollectionUtils.isEmpty(educations)
                && CollectionUtils.isEmpty(occupations)
                && CollectionUtils.isEmpty(professionalExperience)
                && CollectionUtils.isEmpty(computerExperience)
                && CollectionUtils.isEmpty(productExperience)
                && CollectionUtils.isEmpty(assistiveTech)) {
            return testParticipants;
        }

        int max = calculateMaxListSize(ids, genders, ages, educations, occupations,
                professionalExperience, computerExperience, productExperience, assistiveTech);

        //I think everything remains ordered using these data structures so this should be okay.
        testParticipants = IntStream.range(0, max)
            .mapToObj(index -> buildTestParticipant(index, ids, genders, ages,
                    educations, occupations, professionalExperience, computerExperience,
                    productExperience, assistiveTech))
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
                //TODO: the user won't be able to get an error message about this value being wrong
                //the field will just be blank. They will get an error message about that if the field is required.
                //Should we add some transient fields to these objects that save what the user has put in here?
                LOGGER.warn("Cannot parse professional experience '" + professionalExperienceAtIndex + "' into an Integer.");
            }
        }
        String computerExperienceAtIndex = (computerExperience != null && computerExperience.size() > index)
                ? computerExperience.get(index) : null;
        Integer computerExperienceMonths = null;
        if (!StringUtils.isEmpty(computerExperienceAtIndex)) {
            try {
                computerExperienceMonths = Integer.parseInt(computerExperienceAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Cannot parse computer experience '" + computerExperienceAtIndex + "' into an Integer.");
            }
        }
        String productExperienceAtIndex = (productExperience != null && productExperience.size() > index)
                ? productExperience.get(index) : null;
        Integer productExperienceMonths = null;
        if (!StringUtils.isEmpty(productExperienceAtIndex)) {
            try {
                productExperienceMonths = Integer.parseInt(productExperienceAtIndex);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Cannot parse product experience '" + productExperienceAtIndex + "' into an Integer.");
            }
        }
        String assistiveTechAtIndex = (assistiveTech != null && assistiveTech.size() > index)
                ? assistiveTech.get(index) : null;

        TestParticipant tp = TestParticipant.builder()
                .uniqueId(id)
                .gender(gender)
                .ageRange(age)
                .educationTypeName(education)
                .occupation(occupation)
                .professionalExperienceMonths(professionalExperienceMonths)
                .computerExperienceMonths(computerExperienceMonths)
                .productExperienceMonths(productExperienceMonths)
                .assistiveTechnologyNeeds(assistiveTechAtIndex)
                .build();
        return tp;
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
                Headings.PARTICIPANT_ID, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseGenders(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_GENDER, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseAges(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_AGE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseEducations(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_EDUCATION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseOccupations(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_OCCUPATION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseProfessionalExperience(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_PROFESSIONAL_EXPERIENCE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseComputerExperience(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_COMPUTER_EXPERIENCE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseProductExperience(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_PRODUCT_EXPERIENCE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseAssistiveTechnologyNeeds(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.PARTICIPANT_ASSISTIVE_TECH, headingRecord, listingRecords);
        return values;
    }
}
