package gov.healthit.chpl.util;

public class CertificationResultOption {
    private String optionName;
    private boolean canHaveOption;

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(final String optionName) {
        this.optionName = optionName;
    }

    public boolean isCanHaveOption() {
        return canHaveOption;
    }

    public void setCanHaveOption(final boolean canHaveOption) {
        this.canHaveOption = canHaveOption;
    }
}
