package gov.healthit.chpl.testtool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.upload.listing.normalizer.TestToolNormalizer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CertificationResultTestToolService {
    private TestToolNormalizer testToolNormalizer;
    private CertificationResultDAO certResultDAO;

    @Autowired
    public CertificationResultTestToolService(TestToolNormalizer testToolNormalizer, CertificationResultDAO certResultDAO) {
        this.testToolNormalizer = testToolNormalizer;
        this.certResultDAO = certResultDAO;
    }

    public int synchronizeTestTools(CertificationResult certResult, List<CertificationResultTestTool> certResultTestToolsFromDb,
            List<CertificationResultTestTool> certResultTestTools) throws EntityCreationException {

        List<CertificationResultTestTool> updatedTestTools = new ArrayList<CertificationResultTestTool>();
        List<CertificationResultTestTool> addedTestTools = new ArrayList<CertificationResultTestTool>();
        List<CertificationResultTestTool> removedTestTools = new ArrayList<CertificationResultTestTool>();

        //Find the updated Test Tools (version)
        if (!CollectionUtils.isEmpty(certResultTestTools)) {
            updatedTestTools = certResultTestTools.stream()
                    .filter(crtt -> {
                        Optional<CertificationResultTestTool> found = getMatchingItemInList(crtt, certResultTestToolsFromDb);
                        return found.isPresent()
                                && !found.get().getVersion().equals(crtt.getVersion());
                    })
                    .toList();

            updatedTestTools.forEach(x -> LOGGER.info("Updated Test Tool/Version: {}/{}", x.getTestTool().getValue(), x.getVersion()));

            updatedTestTools.forEach(updatedTestTool -> certResultDAO.updateTestToolMapping(
                    getMatchingItemInList(updatedTestTool, certResultTestToolsFromDb).get().getId(),
                    updatedTestTool));
        }

        //Find the added Test Tools
        if (!CollectionUtils.isEmpty(certResultTestTools)) {
            addedTestTools = certResultTestTools.stream()
                    .filter(crtt -> getMatchingItemInList(crtt, certResultTestToolsFromDb).isEmpty())
                    .toList();

            addedTestTools.forEach(x -> LOGGER.info("Added Test Tool: {}", x.getTestTool().getValue()));

            addedTestTools.forEach(addedTestTool -> addCertificationResultTestTool(addedTestTool, certResult.getId()));
        }

        //Find the removed
        if (!CollectionUtils.isEmpty(certResultTestToolsFromDb)) {
            removedTestTools = certResultTestToolsFromDb.stream()
                    .filter(crtt -> getMatchingItemInList(crtt, certResultTestTools).isEmpty())
                    .toList();

            removedTestTools.forEach(x -> LOGGER.info("Removed Test Tool: {}", x.getTestTool().getValue()));

            removedTestTools.forEach(removedTestTool -> certResultDAO.deleteTestToolMapping(
                    getMatchingItemInList(removedTestTool, certResultTestToolsFromDb).get().getId()));
        }

        return updatedTestTools.size() + addedTestTools.size() + removedTestTools.size();
    }

    private CertificationResultTestTool addCertificationResultTestTool(CertificationResultTestTool crtt, Long certificationResultId) {
        try {
            return certResultDAO.addTestToolMapping(
                    CertificationResultTestTool.builder()
                            .certificationResultId(certificationResultId)
                            .testTool(TestTool.builder()
                                    .id(crtt.getTestTool().getId())
                                    .build())
                            .version(crtt.getVersion())
                            .build());

        } catch (EntityCreationException e) {
            LOGGER.error("Could not create Certification Result Test Tool.", e);
            return null;
        }
    }

    private Optional<CertificationResultTestTool> getMatchingItemInList(CertificationResultTestTool crtt, List<CertificationResultTestTool> certificationResultTestTools) {
        if (CollectionUtils.isEmpty(certificationResultTestTools)) {
            return Optional.empty();
        }
        return certificationResultTestTools.stream()
                .filter(certificationResultTestTool ->
                        certificationResultTestTool != null ? certificationResultTestTool.getTestTool().getId().equals(crtt.getTestTool().getId()) : false)
                .findAny();
    }
}
