package gov.healthit.chpl.certifiedproduct.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certifiedproduct.domain.CertificationResultDetailsDTOv2;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;

@Repository(value = "certificationResultDetailsDAOv2")
public class CertificationResultDetailsDAOv2 extends BaseDAOImpl {
    public List<CertificationResultDetailsDTOv2> getAllCertResultsForListing(Long listingId) {
        Query query = entityManager.createQuery("SELECT DISTINCT cr "
                + "FROM CertificationResultDetailsEntity cr "
                + "LEFT OUTER JOIN FETCH cr.certificationCriterion cc "
                + "LEFT OUTER JOIN FETCH cr.certificationResultTestData crtd "
                + "LEFT OUTER JOIN FETCH crtd.testData td "
                + "LEFT OUTER JOIN FETCH cr.certificationResultTestFunctionalities crtf "
                + "LEFT OUTER JOIN FETCH crtf.testFunctionality tf "
                + "LEFT OUTER JOIN FETCH cr.certificationResultTestProcedures crtp "
                + "LEFT OUTER JOIN FETCH crtp.testProcedure tp "
                + "LEFT OUTER JOIN FETCH cr.certificationResultTestTools crtt "
                + "LEFT OUTER JOIN FETCH crtt.testTool tt "
                + "LEFT OUTER JOIN FETCH cr.certificationResultTestStandards crts "
                + "LEFT OUTER JOIN FETCH cr.certificationResultAdditionalSoftware cras "
                + "WHERE cr.deleted = false "
                + "AND (crtd.deleted = false OR crtd.deleted IS NULL) "
                + "AND (crtf.deleted = false OR crtf.deleted IS NULL) "
                + "AND (crtp.deleted = false OR crtp.deleted IS NULL) "
                + "AND (crtt.deleted = false OR crtt.deleted IS NULL) "
                + "AND (crts.deleted = false OR crts.deleted IS NULL) "
                + "AND (cras.deleted = false OR cras.deleted IS NULL) "
                + "AND cr.certifiedProductId = :listingId",
                CertificationResultDetailsEntity.class);

        query.setParameter("listingId", listingId);

        List<CertificationResultDetailsEntity> result = query.getResultList();
        if (result == null) {
            return null;
        } else {
            List<CertificationResultDetailsDTOv2> dtos = new ArrayList<CertificationResultDetailsDTOv2>();
            for (CertificationResultDetailsEntity entity : result) {
                CertificationResultDetailsDTOv2 dto = new CertificationResultDetailsDTOv2(entity);
                dto.setTestData(entity.getCertificationResultTestData().stream()
                        .map(e -> new CertificationResultTestDataDTO(e))
                        .collect(Collectors.toList()));

                dto.setTestFunctionality(entity.getCertificationResultTestFunctionalities().stream()
                        .map(e -> new CertificationResultTestFunctionalityDTO(e))
                        .collect(Collectors.toList()));

                dto.setTestProcedures(entity.getCertificationResultTestProcedures().stream()
                        .map(e -> new CertificationResultTestProcedureDTO(e))
                        .collect(Collectors.toList()));

                dto.setTestTools(entity.getCertificationResultTestTools().stream()
                        .map(e -> new CertificationResultTestToolDTO(e))
                        .collect(Collectors.toList()));

                dto.setTestStandards(entity.getCertificationResultTestStandards().stream()
                        .map(e -> new CertificationResultTestStandardDTO(e))
                        .collect(Collectors.toList()));

                dto.setAdditionalSoftware(entity.getCertificationResultAdditionalSoftware().stream()
                        .map(e -> new CertificationResultAdditionalSoftwareDTO(e))
                        .collect(Collectors.toList()));

                dtos.add(dto);
            }
            return dtos;
        }
    }
}
