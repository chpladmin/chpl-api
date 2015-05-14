package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.acb.CertificationBody;
import gov.healthit.chpl.dao.CertificationBodyDAO;

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
	public void create(CertificationBody acb) {
		
		entityManager.persist(acb);
		
	}
	
	
	@Override
	public void delete(Long acbId) {
		
		Query query = entityManager.createNativeQuery("delete FROM certification_body WHERE id = :acbid");
		query.setParameter("acbid", acbId);
		query.executeUpdate();
		
	}
	
	
	@Override
	public List<CertificationBody> findAll() {
		
		List<CertificationBody> result = entityManager.createQuery( "from CertificationBody", CertificationBody.class ).getResultList();
		
		return result;
		
	}

	@Override
	public CertificationBody getById(Long acbId) {
		
		CertificationBody acb = null;
		
		Query query = entityManager.createQuery( "from CertificationBody where id = :acbid", CertificationBody.class );
		query.setParameter("acbid", acbId);
		List<CertificationBody> result = query.getResultList();
		acb = result.get(0);
		
		return acb;
		
	}

	@Override
	public void update(CertificationBody acb) {
		
		entityManager.merge(acb);
		
	}
	
	private CertificationBody mapCertificationBody(ResultSet rs) throws SQLException {
		
		CertificationBody acb = new CertificationBody();
		acb.setId(new Long(rs.getLong("id")));
		acb.setName(rs.getString("name"));
		acb.setWebsite(rs.getString("website"));
		return acb;
		
	}
	
}
