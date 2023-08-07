package gov.healthit.chpl.criteriaattribute.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleManager {

    private RuleDAO ruleDAO;

    @Autowired
    public RuleManager(RuleDAO ruleDAO) {
        this.ruleDAO = ruleDAO;
    }

    public List<Rule> getAll() {
        return ruleDAO.getAll();
    }
}
