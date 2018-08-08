package gov.healthit.chpl.domain;

/**
 * Interface for actions that can be explained.
 * @author alarned
 *
 */
public interface ExplainableAction {
    String getReason();
    void setReason(String reason);
}
