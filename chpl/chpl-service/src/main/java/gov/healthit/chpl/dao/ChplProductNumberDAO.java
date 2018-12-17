package gov.healthit.chpl.dao;

/**
 * @author TYoung
 *
 */
public interface ChplProductNumberDAO {
    /**
     * Returns the CHPL Product Number as calculated by the DB.
     * @param certifiedProductId - Long
     * @return - String
     */
    String getChplProductNumber(Long certifiedProductId);
}
