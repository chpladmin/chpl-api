package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.acb.CertificationBody;
import gov.healthit.chpl.dao.CertificationBodyDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;








import javax.persistence.Query;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;


@Repository
public class CertificationBodyDAOImpl extends BaseDAOImpl implements CertificationBodyDAO {
	
	
	@Override
	public void create(CertificationBody acb) {
		
		entityManager.persist(acb);
		
		/*
		Query query = entityManager.createNativeQuery("insert into certification_body values (:acbid, :acbname)");
		query.setParameter("acbid", acb.getId());
		query.setParameter("acbname", acb.getName());
		query.executeUpdate();
		*/
	}
	
	
	@Override
	public void delete(Long acbId) {
		
		Query query = entityManager.createNativeQuery("delete FROM certification_body WHERE id = :acbid");
		query.setParameter("acbid", acbId);
		query.executeUpdate();
		
		/*
		getJdbcTemplate().update("delete from certification_body where id = ?",
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setLong(1, acbId);
					}
				});
		*/
	}
	
	
	@Override
	public List<CertificationBody> findAll() {
		
		entityManager.getTransaction().begin();
		List<CertificationBody> result = entityManager.createQuery( "from CertificationBody", CertificationBody.class ).getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		
		return result;
		
		/*
		return getJdbcTemplate().query(
				"select id, name from contacts order by id",
				new RowMapper<CertificationBody>() {
					public CertificationBody mapRow(ResultSet rs, int rowNum) throws SQLException {
						return mapCertificationBody(rs);
					}
				});
		*/
	}

	@Override
	public CertificationBody getById(Long acbId) {
		
		CertificationBody cb = null;
		
		Query query = entityManager.createQuery( "from CertificationBody where id = :acbid", CertificationBody.class );
		query.setParameter("acbid", acbId);
		List<CertificationBody> result = query.getResultList();
		cb = result.get(0);
		
		return cb;
		
		/*
		List<CertificationBody> list = getJdbcTemplate().query(
				"select id, name from contacts where id = ? order by id",
				new RowMapper<CertificationBody>() {
					public CertificationBody mapRow(ResultSet rs, int rowNum) throws SQLException {
						return mapCertificationBody(rs);
					}
				}, id);
	
		if (list.size() == 0) {
			return null;
		}
		else {
			return (CertificationBody) list.get(0);
		}
		*/
	}

	@Override
	public void update(CertificationBody contact) {
		
		
		Query query = entityManager.createNativeQuery("update certification_body set name = :name WHERE id = :acbid");
		query.setParameter("acbid", contact.getId());
		query.setParameter("name", contact.getName());
		query.executeUpdate();
		
		/*
		getJdbcTemplate().update(
				"update contacts set name = ? where id = ?",
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setString(1, contact.getName());
						ps.setLong(2, contact.getId());
					}
				});
		*/
	}
	
	private CertificationBody mapCertificationBody(ResultSet rs) throws SQLException {
		
		CertificationBody acb = new CertificationBody();
		acb.setId(new Long(rs.getLong("id")));
		acb.setName(rs.getString("name"));
		return acb;
		
	}
	
}
