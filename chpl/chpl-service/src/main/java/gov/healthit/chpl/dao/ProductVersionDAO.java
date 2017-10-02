package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;

public interface ProductVersionDAO {

	public ProductVersionDTO create(ProductVersionDTO dto) throws EntityCreationException, EntityRetrievalException;

	public ProductVersionEntity update(ProductVersionDTO dto) throws EntityRetrievalException;

	public void delete(Long id) throws EntityRetrievalException;

	public List<ProductVersionDTO> findAll();

	public ProductVersionDTO getById(Long id) throws EntityRetrievalException;

	public List<ProductVersionDTO> getByProductId(Long productId);

	public List<ProductVersionDTO> getByProductIds(List<Long> productId);

	public ProductVersionDTO getByProductAndVersion(Long productId, String version);

}
