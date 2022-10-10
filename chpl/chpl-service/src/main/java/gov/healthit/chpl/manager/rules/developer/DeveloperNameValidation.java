package gov.healthit.chpl.manager.rules.developer;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperNameValidation extends ValidationRule<DeveloperValidationContext> {
    private DeveloperDAO developerDao;

    public DeveloperNameValidation(DeveloperDAO developerDao) {
        this.developerDao = developerDao;
    }
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (ObjectUtils.isEmpty(context.getDeveloper().getName())) {
            getMessages().add(getErrorMessage("developer.nameRequired"));
            return false;
        }

        List<Developer> developersWithName = developerDao.getAllByName(context.getDeveloper().getName());
        if (!CollectionUtils.isEmpty(context.getBeforeDevs())) {
            //merge - all the "beforeDevs" will get deleted
            if (!CollectionUtils.isEmpty(developersWithName)
                    && !anyDeveloperMatchesAnyId(developersWithName,
                            context.getBeforeDevs().stream().map(dev -> dev.getId()).toList())) {
                getMessages().add(getErrorMessage("developer.nameNotUnique"));
                return false;
            }
        } else if (context.getBeforeDev() != null) {
            //update
            if (!CollectionUtils.isEmpty(developersWithName)
                    && !anyDeveloperMatchesId(developersWithName, context.getBeforeDev().getId())) {
                getMessages().add(getErrorMessage("developer.nameNotUnique"));
                return false;
            }
        } else {
            //create or split - existing dev will remain, new dev being added
            if (!CollectionUtils.isEmpty(developersWithName)) {
                getMessages().add(getErrorMessage("developer.nameNotUnique"));
                return false;
            }
        }
        return true;
    }

    private boolean anyDeveloperMatchesId(List<Developer> developers, Long developerId) {
        return developers.stream()
                .filter(dev -> dev.getId().equals(developerId))
                .findAny().isPresent();
    }

    private boolean anyDeveloperMatchesAnyId(List<Developer> developers, List<Long> developerIds) {
        return developers.stream()
                .filter(dev -> developerIds.contains(dev.getId()))
                .findAny().isPresent();
    }
}
