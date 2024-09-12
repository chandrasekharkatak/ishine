package com.apmosys.employeeportal.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class AuditCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;
	
	
	
	public List<Object[]> readAudit(Class className, Long entityId) {
		AuditReader reader = AuditReaderFactory.get(entityManager);
		AuditQuery query=reader.createQuery()
				.forRevisionsOfEntity( className, false, true )
				.add( AuditEntity.id().eq(entityId) );
		return query.getResultList();
	}
	
	public List<Object[]> readAuditCustomNativeQuery(String query) {
		Session s=entityManager.unwrap(Session.class);
		javax.persistence.Query q=s.createNativeQuery(query);
		try {
			List<Object[]> o=q.getResultList();
			return o;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	public List<Object[]> readAuditCustomNativeQueryy(String query, Long empId) {
		Session s=entityManager.unwrap(Session.class);
		javax.persistence.Query q=s.createNativeQuery(query);
		q.setParameter("emp_id", empId);
		try {
			List<Object[]> o=q.getResultList();
			return o;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
}
