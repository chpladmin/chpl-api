package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;










import javax.persistence.Query;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository(value="certificationBodyDAO")
public class CertificationBodyDAOImpl extends BaseDAOImpl implements CertificationBodyDAO {
	
	@Transactional
	@Override
	public void create(CertificationBodyEntity acb) {
		
		entityManager.persist(acb);
		
	}
	
	@Transactional
	@Override
	public void update(CertificationBodyEntity acb) {
		
		entityManager.merge(acb);
		
	}
	
	@Transactional
	@Override
	public void delete(Long acbId) {
		
		//TODO: implement soft deletes here
		//Query query = entityManager.createNativeQuery("delete FROM certification_body WHERE id = :acbid");
		//query.setParameter("acbid", acbId);
		//query.executeUpdate();
		
	}
	
	
	@Override
	public List<CertificationBodyEntity> findAll() {
		
		List<CertificationBodyEntity> result = entityManager.createQuery( "from CertificationBody", CertificationBodyEntity.class ).getResultList();
		
		return result;
		
	}

	@Override
	public CertificationBodyEntity getById(Long acbId) {
		
		CertificationBodyEntity acb = null;
		
		Query query = entityManager.createQuery( "from CertificationBody where id = :acbid", CertificationBodyEntity.class );
		query.setParameter("acbid", acbId);
		List<CertificationBodyEntity> result = query.getResultList();
		acb = result.get(0);
		
		return acb;
		
	}
	
}
