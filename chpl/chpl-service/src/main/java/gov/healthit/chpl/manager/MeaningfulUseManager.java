package gov.healthit.chpl.manager;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.scheduler.job.MeaningfulUseUploadJob;
import gov.healthit.chpl.scheduler.job.SplitDeveloperJob;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MeaningfulUseManager {
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private FileUtils fileUtils;

    @Autowired
    public MeaningfulUseManager(SchedulerManager schedulerManager, UserManager userManager,
            FileUtils fileUtils) {
        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.fileUtils = fileUtils;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).UPLOAD_MUU)")
    public ChplOneTimeTrigger processUploadAsJob(MultipartFile file, Long accurateAsOfDate)
            throws EntityCreationException, EntityRetrievalException, ValidationException, SchedulerException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }

        String data = fileUtils.readFileAsString(file);
        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger uploadMuuTrigger = new ChplOneTimeTrigger();
        ChplJob uploadMuuJob = new ChplJob();
        uploadMuuJob.setName(MeaningfulUseUploadJob.JOB_NAME);
        uploadMuuJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(MeaningfulUseUploadJob.FILE_CONTENTS_KEY, data);
        jobDataMap.put(MeaningfulUseUploadJob.ACCURATE_AS_OF_DATE_KEY, accurateAsOfDate);
        jobDataMap.put(SplitDeveloperJob.USER_KEY, jobUser);
        uploadMuuJob.setJobDataMap(jobDataMap);
        uploadMuuTrigger.setJob(uploadMuuJob);
        uploadMuuTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        uploadMuuTrigger = schedulerManager.createBackgroundJobTrigger(uploadMuuTrigger);
        return uploadMuuTrigger;
    }
}
