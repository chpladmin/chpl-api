package gov.healthit.chpl.certificationId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attribute.CodeSetsUpToDateService;
import gov.healthit.chpl.attribute.GroupedStandardsUpToDateService;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.notifier.ChplTeamNotifier;
import gov.healthit.chpl.notifier.InvalidCertificationIdYearMessage;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ValidatorFactory {

    private Map<String, Class<?>> certIdYearToValidatorClassMap;
    private CertificationCriterionService certificationCriterionService;
    private CertificationIdYearCalculator certIdYearCalculator;
    private BaselineStandardService baselineStandardService;
    private GroupedStandardsUpToDateService groupedStandardService;
    private CodeSetsUpToDateService codeSetService;
    private ChplTeamNotifier chplTeamNotifier;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private Environment env;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ValidatorFactory(CertificationCriterionService certificationCriterionService,
            CertificationIdYearCalculator certIdYearCalculator,
            BaselineStandardService baselineStandardService,
            GroupedStandardsUpToDateService groupedStandardService,
            CodeSetsUpToDateService codeSetService,
            ChplTeamNotifier chplTeamNotifier,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder,
            Environment env,
            ErrorMessageUtil msgUtil) {
        this.certificationCriterionService = certificationCriterionService;
        this.certIdYearCalculator = certIdYearCalculator;
        this.baselineStandardService = baselineStandardService;
        this.groupedStandardService = groupedStandardService;
        this.codeSetService = codeSetService;
        this.chplTeamNotifier = chplTeamNotifier;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.env = env;
        this.msgUtil = msgUtil;

        this.certIdYearToValidatorClassMap = new LinkedHashMap<String, Class<?>>();
        this.certIdYearToValidatorClassMap.put("2014", Validator2014.class);
        this.certIdYearToValidatorClassMap.put("2014/2015", Validator20142015.class);
        this.certIdYearToValidatorClassMap.put("2015", Validator2015.class);
        this.certIdYearToValidatorClassMap.put("2025", Validator2025.class);
        this.certIdYearToValidatorClassMap.put("2026", Validator2026.class);
        //TODO: we will need to create 2027, 2028, etc validators before the cmsIdStartDayOfYear
        //day comes for the current year (so before 9/1/20XX)
        this.certIdYearToValidatorClassMap.put("2027", Validator2026.class);
    }

    public Validator getValidator(String certIdYear) throws InvalidArgumentsException {
        Validator validator = new ValidatorDefault();

        //NOTE: The "2014" and "2014/2015" validators can no longer be used to create new CMS IDs
        //but they may get called if someone calls our API to search for CMS IDs with a combinations
        //of older listings, or searches for an existing CMS ID that had been created with older listings.
        Class<?> validatorClazz = this.certIdYearToValidatorClassMap.get(certIdYear);
        if (validatorClazz != null) {
            validator = getNewInstance(validatorClazz);
        }

        if (validator == null) {
            chplTeamNotifier.sendNotification(new InvalidCertificationIdYearMessage(
                    certIdYear,
                    this.certIdYearToValidatorClassMap.keySet().stream().collect(Collectors.toList()),
                    env,
                    chplHtmlEmailBuilder));
            throw new InvalidArgumentsException(msgUtil.getMessage("certificationId.invalidYear", certIdYear));
        }
        return validator;
    }

    private Validator getNewInstance(Class<?> validatorClazz) {
        Validator result = null;
        if (validatorClazz.equals(Validator2014.class)
                || validatorClazz.equals(Validator20142015.class)) {
            try {
                result =  (Validator) validatorClazz.getDeclaredConstructors()[0].newInstance();
            } catch (Exception ex) {
                LOGGER.error("Could not instantiate validator " + validatorClazz, ex);
            }
        } else if (validatorClazz.equals(Validator2015.class)
                || validatorClazz.equals(Validator2025.class)) {
            try {
                result =  (Validator) validatorClazz.getDeclaredConstructors()[0].newInstance(certificationCriterionService);
            } catch (Exception ex) {
                LOGGER.error("Could not instantiate validator " + validatorClazz, ex);
            }
        } else if (validatorClazz.equals(Validator2026.class)) {
            try {
                result =  (Validator) validatorClazz.getDeclaredConstructors()[0].newInstance(
                        certificationCriterionService, certIdYearCalculator, baselineStandardService, groupedStandardService, codeSetService);
            } catch (Exception ex) {
                LOGGER.error("Could not instantiate validator " + validatorClazz, ex);
            }
        }
        return result;
    }
}
