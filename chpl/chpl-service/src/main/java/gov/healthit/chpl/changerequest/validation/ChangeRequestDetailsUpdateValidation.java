package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestDetailsUpdateValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
//        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
//            if (context.getNewChangeRequest().getDetails() != null) {
//                if (isChangeRequestWebsite(context)) {
//                    return isChangeRequestWebsiteValid((HashMap) context.getNewChangeRequest().getDetails());
//                }
//            }
//        }

        return true;
    }

//    private boolean isChangeRequestWebsiteValid(HashMap<String, String> map) {
//        if (!doesKeyExistWithStringData(map, "website")) {
//            return false;
//        }
//        return true;
//    }
//
//    private boolean doesKeyExistWithStringData(final HashMap<String, String> map, final String key) {
//        return map.containsKey(key) && !StringUtils.isEmpty(map.get(key));
//    }
//
//    private boolean isChangeRequestWebsite(ChangeRequestValidationContext context) {
//        return context.getOrigChangeRequest().getChangeRequestType().getId().equals(context.getChangeRequestTypeIds().getWebsiteChangeRequestType());
//    }
}
