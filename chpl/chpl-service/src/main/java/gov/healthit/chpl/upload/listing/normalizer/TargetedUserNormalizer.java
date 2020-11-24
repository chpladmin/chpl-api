package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.dto.TargetedUserDTO;

@Component
public class TargetedUserNormalizer {
    private TargetedUserDAO targetedUserDao;

    @Autowired
    public TargetedUserNormalizer(TargetedUserDAO targetedUserDao) {
        this.targetedUserDao = targetedUserDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getTargetedUsers() != null && listing.getTargetedUsers().size() > 0) {
            listing.getTargetedUsers().stream()
                .forEach(targetedUser -> lookupTargetedUserId(targetedUser));
        }
    }

    private void lookupTargetedUserId(CertifiedProductTargetedUser targetedUser) {
        if (!StringUtils.isEmpty(targetedUser.getTargetedUserName())) {
            TargetedUserDTO targetedUserDto =
                    targetedUserDao.getByName(targetedUser.getTargetedUserName());
            if (targetedUserDto != null) {
                targetedUser.setTargetedUserId(targetedUserDto.getId());
            }
        }
    }
}
