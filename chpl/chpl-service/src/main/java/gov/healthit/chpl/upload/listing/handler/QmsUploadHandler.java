package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("qmsUploadHandler")
@Log4j2
public class QmsUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private QmsStandardDAO dao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public QmsUploadHandler(ListingUploadHandlerUtil uploadUtil,
            QmsStandardDAO dao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.dao = dao;
        this.msgUtil = msgUtil;
    }

    public List<CertifiedProductQmsStandard> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();
        List<String> qmsStandardNames = parseQmsStandardNames(headingRecord, listingRecords);
        List<String> qmsApplicableCriteria = parseQmsApplicableCriteria(headingRecord, listingRecords);
        List<String> qmsModifications = parseQmsModifications(headingRecord, listingRecords);
        if (CollectionUtils.isEmpty(qmsStandardNames)
                && CollectionUtils.isEmpty(qmsApplicableCriteria)
                && CollectionUtils.isEmpty(qmsModifications)) {
            return qmsStandards;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(qmsStandardNames)) {
            max = Math.max(max, qmsStandardNames.size());
        }
        if (CollectionUtils.isNotEmpty(qmsApplicableCriteria)) {
            max = Math.max(max, qmsApplicableCriteria.size());
        }
        if (CollectionUtils.isNotEmpty(qmsModifications)) {
            max = Math.max(max, qmsModifications.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        qmsStandards = IntStream.range(0, max)
            .mapToObj(index -> buildQmsStandard(index, qmsStandardNames, qmsApplicableCriteria, qmsModifications))
            .collect(Collectors.toList());
        return qmsStandards;
    }

    private CertifiedProductQmsStandard buildQmsStandard(int index, List<String> qmsNames, List<String> qmsApplicableCriterias,
            List<String> qmsModifications) {
        String qmsName = (qmsNames != null && qmsNames.size() > index) ? qmsNames.get(index) : null;
        String qmsApplicableCriteria = (qmsApplicableCriterias != null && qmsApplicableCriterias.size() > index)
                ? qmsApplicableCriterias.get(index) : null;
        String qmsModification = (qmsModifications != null && qmsModifications.size() > index)
                ? qmsModifications.get(index) : null;

        CertifiedProductQmsStandard qms = CertifiedProductQmsStandard.builder()
                .qmsStandardName(qmsName)
                .qmsStandardId(lookupQmsId(qmsName))
                .applicableCriteria(qmsApplicableCriteria)
                .qmsModification(qmsModification)
                .build();
        return qms;
    }

    private Long lookupQmsId(String qmsName) {
        QmsStandardDTO foundQms = dao.getByName(qmsName);
        if (foundQms != null) {
            return foundQms.getId();
        } else {
            LOGGER.warn("Could not find QMS Standard with value " + qmsName);
        }
        return null;
    }

    private List<String> parseQmsStandardNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.QMS_STANDARD_NAME, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseQmsApplicableCriteria(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.QMS_STANDARD_APPLICABLE_CRITERIA, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseQmsModifications(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.QMS_MODIFICATION, headingRecord, listingRecords);
        return values;
    }
}
