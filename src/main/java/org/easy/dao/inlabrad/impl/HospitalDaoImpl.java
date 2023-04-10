package org.easy.dao.inlabrad.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.easy.dao.AbstractJpaDao;
import org.easy.dao.StudyDao;
import org.easy.dao.inlabrad.HospitalDao;
import org.easy.entity.Study;
import org.easy.entity.inlabrad.Hospital;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HospitalDaoImpl extends AbstractJpaDao<Hospital> implements HospitalDao {
	
	@PersistenceContext
	private EntityManager entityManager;	
	
	public HospitalDaoImpl(){
		super();
		setClazz(Hospital.class);
	}
	
	
	@Override
	public List<Hospital> findByIpSender(String ipSender) {
		List<Hospital> list = new ArrayList<>();
		
		try{
			list = entityManager.createQuery("SELECT e from Hospital e where  e.ipSender like CONCAT('%',:ipSender,'%')", Hospital.class)
			.setParameter("ipSender", ipSender)			
			.getResultList();
			
			if(list == null) {
				list = new ArrayList<>();
			}
		}catch(Exception e){			
			return null;		
		}
		return list;
	}

	@Override
	public List<Hospital> findByNome(String nome) {
		List<Hospital> list = new ArrayList<>();
		
		try{
			list = entityManager.createQuery("SELECT e from Hospital e where e.nome like CONCAT('%',:nome,'%') or e.ipSender  = :nome or e.cnpj  = :nome  order by nome", Hospital.class)
			.setParameter("nome", nome)			
			.getResultList();
			
			if(list == null) {
				list = new ArrayList<>();
			}
		}catch(Exception e){			
			return null;		
		}
		return list;
	}

	

}
