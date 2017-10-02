package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.entity.ApiKeyActivityEntity;

public interface ApiKeyActivityDAO {

    ApiKeyActivityDTO create(ApiKeyActivityDTO apiKeyActivityDto) throws EntityCreationException;

    ApiKeyActivityDTO update(ApiKeyActivityDTO apiKeyActivityDto) throws EntityRetrievalException;

    void delete(Long id);

    List<ApiKeyActivityDTO> findAll();

    List<ApiKeyActivityDTO> findAll(Integer pageNumber, Integer pageSize);

    List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId);

    List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId, Integer pageNumber, Integer pageSize);

    ApiKeyActivityDTO getById(Long id) throws EntityRetrievalException;

    List<ApiKeyActivityDTO> getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize,
            boolean dateAscending, Long startDate, Long endDate);

    List<ApiKeyActivityEntity> getActivityEntitiesByKeyStringWithFilter(String apiKeyFilter, Integer pageNumber,
            Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli);
}
