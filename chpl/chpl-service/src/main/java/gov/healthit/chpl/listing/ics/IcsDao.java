package gov.healthit.chpl.listing.ics;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.listing.ics.ListingIcsNode.IdNamePair;
import gov.healthit.chpl.listing.ics.ListingIcsNode.ListingRelative;
import gov.healthit.chpl.search.entity.ListingSearchEntity;

@Component
public class IcsDao extends BaseDAOImpl {

    public ListingIcsNode getIcsFamilyTree(Long id) {
        Query query = entityManager.createQuery("SELECT listing "
                + "FROM ListingSearchEntity listing "
                + "WHERE listing.id = :id",
                ListingSearchEntity.class);
        query.setParameter("id", id);
        List<ListingSearchEntity> searchResult = query.getResultList();
        ListingSearchEntity result = null;
        if (searchResult.size() > 0 && searchResult.get(0) != null) {
            result = searchResult.get(0);
            return convertIcs(result);
        } else {
            return null;
        }
    }

    private ListingIcsNode convertIcs(ListingSearchEntity result) {
        return ListingIcsNode.builder()
                .id(result.getId())
                .chplProductNumber(result.getChplProductNumber())
                .certificationDate(result.getCertificationDate())
                .certificationStatus(CertificationStatus.builder()
                        .id(result.getCertificationStatusId())
                        .name(result.getCertificationStatus())
                        .build())
                .developer(IdNamePair.builder()
                        .id(result.getDeveloperId())
                        .name(result.getDeveloper())
                        .build())
                .product(IdNamePair.builder()
                        .id(result.getProductId())
                        .name(result.getProduct())
                        .build())
                .version(IdNamePair.builder()
                        .id(result.getVersionId())
                        .name(result.getVersion())
                        .build())
                .children(getRelatives(result.getChildren()))
                .parents(getRelatives(result.getParents()))
                .build();
    }

    private List<ListingRelative> getRelatives(String aggregatedRelativesString) {
        List<ListingRelative> relatives = new ArrayList<ListingRelative>();
        if (!StringUtils.isEmpty(aggregatedRelativesString)) {
            String[] relativeIdsAndNumbers = aggregatedRelativesString.split(ListingSearchEntity.LISTING_SEPARATOR);
            for (String relativeIdAndNumber : relativeIdsAndNumbers) {
                String[] splitRelativeIdAndNumber = relativeIdAndNumber.split(ListingSearchEntity.LISTING_ID_NUMBER_SEPARATOR);
                relatives.add(ListingRelative.builder()
                        .id(Long.parseLong(splitRelativeIdAndNumber[0]))
                        .chplProductNumber(splitRelativeIdAndNumber[1])
                        .build());
            }
        }
        return relatives;
    }
}
