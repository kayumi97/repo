package org.easy.dao.impl;



import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.easy.dao.AbstractJpaDao;
import org.easy.dao.PatientDao;
import org.easy.entity.Patient;
import org.easy.util.Utils;
import org.springframework.stereotype.Repository;


@Repository
public class PatientDaoImpl extends AbstractJpaDao<Patient> implements PatientDao {
	
	@PersistenceContext
	private EntityManager entityManager;

	public PatientDaoImpl(){
		super();
		setClazz(Patient.class);
	}
	
	/*@Transactional
	@Override
	public void save(Patient patient) {
		entityManager.persist(patient);		
		entityManager.flush();
	}*/


	@Override
	public List<Patient> findAll(int firstResult, int maxResults) {
		
		try{			
			TypedQuery<Patient> query = entityManager
					.createQuery("select p FROM Patient p", Patient.class);			 
			//return query.getResultList();
		      return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();	     

			
		}catch(Exception e){
			return null;		
		}
	}

	/*@Override
	public Patient findByID(long pkTBLPatientID) {
		try{			
			return entityManager.find(Patient.class, pkTBLPatientID);
			
		}catch(Exception e){
			return null;
		}
	}*/
	
	@Override 
	public Patient findByPatientID(String patientID){
		
		try{
			return entityManager.createQuery("select p from Patient p where p.patientID LIKE :patientID", Patient.class)
			.setParameter("patientID", patientID)			
			.getSingleResult();
		}catch(Exception e){			
			return null;		
		}
	}

	@Override
	public List<Patient> filter(String filtro, String prefix) {
		var filter_param = filtro == null ? "" : filtro;
		Date d = null;
		
		if(Utils.isDate(filtro, "dd/MM/yyyy")) {
			d = Utils.convertDate(filtro, "dd/MM/yyyy");
		}
		
		try{			
			TypedQuery<Patient> query = entityManager
					.createQuery("select p FROM Patient p where p.sender = :prefix and ( (patientID = :filtro or :filtro = '') or (DATE_FORMAT(modifiedDate,'%d/%m/%Y')  = :filtro) or (DATE_FORMAT(createdDate,'%d/%m/%Y')  = :filtro)) order by modifiedDate desc", Patient.class)		 
					//.createQuery("select p FROM Patient p where DATE_FORMAT(modifiedDate,'%d/%m/%Y')  = :filtro order by modifiedDate desc", Patient.class)
						.setParameter("prefix", prefix)
						.setParameter("filtro", filter_param)
						;
			

			
			return query.getResultList();

			
		}catch(Exception e){
			return null;		
		}
	} 

}
