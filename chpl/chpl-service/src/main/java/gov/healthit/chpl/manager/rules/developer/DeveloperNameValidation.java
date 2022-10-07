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
        if (!CollectionUtils.isEmpty(developersWithName)) {
            getMessages().add(getErrorMessage("developer.nameNotUnique"));
            return false;
        }
        return true;
    }
}
