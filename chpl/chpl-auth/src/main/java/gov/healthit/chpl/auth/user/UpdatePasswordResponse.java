package gov.healthit.chpl.auth.user;

import java.util.List;

import com.nulabinc.zxcvbn.Strength;

/**
 * Response class for an update password request.
 * 
 * @author alarned
 *
 */
public class UpdatePasswordResponse {

    private boolean passwordUpdated;
    private String warning;
    private List<String> suggestions;
    private int score;

    /** Default constructor. */
    public UpdatePasswordResponse() {
    }

    /**
     * Constructed from Strength object.
     * 
     * @param strength
     *            the password strength object
     */
    public UpdatePasswordResponse(final Strength strength) {
        setStrength(strength);
    }

    /**
     * Set parameters in this response to match values from passed in object.
     * 
     * @param strength
     *            Strength object of password check
     */
    public void setStrength(final Strength strength) {
        this.warning = strength.getFeedback().getWarning();
        this.suggestions = strength.getFeedback().getSuggestions();
        this.score = strength.getScore();
    }

    public boolean isPasswordUpdated() {
        return passwordUpdated;
    }

    public void setPasswordUpdated(final boolean passwordUpdated) {
        this.passwordUpdated = passwordUpdated;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(final String warning) {
        this.warning = warning;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(final List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public int getScore() {
        return score;
    }

    public void setScore(final int score) {
        this.score = score;
    }
}
