package gov.healthit.chpl.acb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;





import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;


@Repository
public class CertificationBodyDAOImpl implements CertificationBodyDAO {
	
	
	@Override
	public void create(CertificationBody acb) {
		/*getJdbcTemplate().update("insert into certification_body values (?, ?)",
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setLong(1, acb.getId());
						ps.setString(2, acb.getName());
					}
				});
		*/
	}
	
	
	@Override
	public void delete(Long acbId) {
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
		return null;
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
	public CertificationBody getById(Long id) {
		return null;	
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
