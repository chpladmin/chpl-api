package gov.healthit.chpl.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class RunnableJobFactory {

    @Autowired private UserDAO userDao;

    public RunnableJob getRunnableJob(final JobDTO job) throws NoJobTypeException {
        RunnableJob result = null;
        JobTypeDTO jobType = job.getJobType();
        if (jobType == null || StringUtils.isEmpty(jobType.getName())) {
            throw new NoJobTypeException();
        }

        // find the job type enum value
        JobTypeConcept jobTypeConcept = JobTypeConcept.findByName(jobType.getName());
        if (jobTypeConcept == null) {
            throw new NoJobTypeException();
        }

        switch (jobTypeConcept) {
        case MUU_UPLOAD:
            result = getMeaningfulUseUploadJob();
            break;
        case SURV_UPLOAD:
            result = getSurveillanceUploadJob();
            break;
        default:
            throw new NoJobTypeException();
        }

        result.setJob(job);
        if (AuthUtil.getCurrentUser() == null || AuthUtil.getCurrentUser().getId() == null) {
            JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
            jobUser.setFullName(job.getUser().getFullName());
            jobUser.setId(job.getUser().getId());
            jobUser.setFriendlyName(job.getUser().getFriendlyName());
            jobUser.setSubjectName(job.getUser().getUsername());

            //add granted authorities which are like ROLE_ACB, ROLE_ADMIN, etc.
            //so that the jobs can make calls to methods with security on them
            try {
                UserDTO user = userDao.getById(job.getUser().getId());
                if (user != null) {
                    jobUser.addPermission(user.getPermission().getAuthority());
                }
            } catch (UserRetrievalException ex) { }
            result.setUser(jobUser);
        } else {
            result.setUser(AuthUtil.getCurrentUser());
        }
        return result;
    }

    @Lookup
    public MeaningfulUseUploadJob getMeaningfulUseUploadJob() {
        // spring will override this method
        // and create a new instance of MeaningfulUseUploadJob
        return null;
    }

    @Lookup
    public SurveillanceUploadJob getSurveillanceUploadJob() {
        // spring will override this method
        // and create a new instance of SurveillanceUploadJob
        return null;
    }
}
