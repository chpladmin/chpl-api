package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.ClearAllCaches;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanCertificationResultDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDocumentationDAO;
import gov.healthit.chpl.domain.CorrectiveActionPlanDetails;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CorrectiveActionPlanManager;

@Service
public class CorrectiveActionPlanManagerImpl implements CorrectiveActionPlanManager {

    @Autowired
    CertifiedProductDAO cpDao;
    @Autowired
    CorrectiveActionPlanDAO capDao;
    @Autowired
    CorrectiveActionPlanCertificationResultDAO capCertDao;
    @Autowired
    CorrectiveActionPlanDocumentationDAO capDocDao;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    @ClearAllCaches
    public CorrectiveActionPlanDetails create(Long acbId, CorrectiveActionPlanDTO toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        CorrectiveActionPlanDTO created = capDao.create(toCreate);
        CorrectiveActionPlanDetails result = new CorrectiveActionPlanDetails(created, null);

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    @ClearAllCaches
    public CorrectiveActionPlanDocumentationDTO addDocumentationToPlan(Long acbId,
            CorrectiveActionPlanDocumentationDTO doc)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        CorrectiveActionPlanDocumentationDTO created = capDocDao.create(doc);
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    @ClearAllCaches
    public CorrectiveActionPlanDetails addCertificationsToPlan(Long acbId, Long correctiveActionPlanId,
            List<CorrectiveActionPlanCertificationResultDTO> certs)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        CorrectiveActionPlanDTO plan = capDao.getById(correctiveActionPlanId);

        for (CorrectiveActionPlanCertificationResultDTO toCreate : certs) {
            capCertDao.create(toCreate);
        }

        List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao
                .getAllForCorrectiveActionPlan(correctiveActionPlanId);
        return new CorrectiveActionPlanDetails(plan, planCerts);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    @ClearAllCaches
    public void removeCertificationsFromPlan(Long acbId, List<CorrectiveActionPlanCertificationResultDTO> certs)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        for (CorrectiveActionPlanCertificationResultDTO toDelete : certs) {
            capCertDao.delete(toDelete.getId());
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    @ClearAllCaches
    public CorrectiveActionPlanCertificationResultDTO updateCertification(Long acbId,
            CorrectiveActionPlanCertificationResultDTO cert)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        CorrectiveActionPlanCertificationResultDTO updatedCert = capCertDao.update(cert);
        return updatedCert;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    @ClearAllCaches
    public void removeDocumentation(Long acbId, CorrectiveActionPlanDocumentationDTO toRemove)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        capDocDao.delete(toRemove.getId());
    }

    @Override
    public CorrectiveActionPlanDTO getPlanById(Long capId) throws EntityRetrievalException {
        return capDao.getById(capId);
    }

    @Override
    public List<CorrectiveActionPlanDTO> getPlansForCertifiedProduct(Long certifiedProductId)
            throws EntityRetrievalException {
        return capDao.getAllForCertifiedProduct(certifiedProductId);
    }

    @Override
    public List<CorrectiveActionPlanCertificationResultDTO> getCertificationsForPlan(Long capId)
            throws EntityRetrievalException {
        return capCertDao.getAllForCorrectiveActionPlan(capId);
    }

    @Override
    public List<CorrectiveActionPlanDocumentationDTO> getDocumentationForPlan(Long capId)
            throws EntityRetrievalException {
        return capDocDao.getAllForCorrectiveActionPlan(capId);
    }

    @Override
    public CorrectiveActionPlanDocumentationDTO getDocumentationById(Long docId) throws EntityRetrievalException {
        return capDocDao.getById(docId);
    }

    public CorrectiveActionPlanDetails getPlanDetails(Long capId) throws EntityRetrievalException {
        CorrectiveActionPlanDTO plan = capDao.getById(capId);
        List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(capId);
        List<CorrectiveActionPlanDocumentationDTO> planDocs = capDocDao.getAllForCorrectiveActionPlan(capId);

        return new CorrectiveActionPlanDetails(plan, planCerts, planDocs);
    }

    public List<CorrectiveActionPlanDetails> getPlansForCertifiedProductDetails(Long certifiedProductId)
            throws EntityRetrievalException {

        List<CorrectiveActionPlanDetails> result = new ArrayList<CorrectiveActionPlanDetails>();
        List<CorrectiveActionPlanDTO> plans = capDao.getAllForCertifiedProduct(certifiedProductId);
        for (CorrectiveActionPlanDTO plan : plans) {
            List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao
                    .getAllForCorrectiveActionPlan(plan.getId());
            List<CorrectiveActionPlanDocumentationDTO> planDocs = capDocDao.getAllForCorrectiveActionPlan(plan.getId());
            CorrectiveActionPlanDetails currDetails = new CorrectiveActionPlanDetails(plan, planCerts, planDocs);
            result.add(currDetails);
        }

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public CorrectiveActionPlanDTO update(Long acbId, CorrectiveActionPlanDTO toUpdate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        CorrectiveActionPlanDTO updatedPlan = capDao.update(toUpdate);
        return updatedPlan;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void delete(Long acbId, Long capId)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(capId);
        if (planCerts != null && planCerts.size() > 0) {
            for (CorrectiveActionPlanCertificationResultDTO cert : planCerts) {
                capCertDao.delete(cert.getId());
            }
        }

        List<CorrectiveActionPlanDocumentationDTO> planDocs = capDocDao.getAllForCorrectiveActionPlan(capId);
        if (planDocs != null && planDocs.size() > 0) {
            for (CorrectiveActionPlanDocumentationDTO doc : planDocs) {
                capDocDao.delete(doc.getId());
            }
        }
        capDao.delete(capId);
    }

}
