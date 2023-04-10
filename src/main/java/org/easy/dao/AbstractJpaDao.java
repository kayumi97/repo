package org.easy.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


public abstract class AbstractJpaDao< T extends Serializable > {


	private Class< T > clazz;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJpaDao.class);

	@PersistenceContext
	EntityManager entityManager;

	public final void setClazz( Class< T > clazzToSet ){
		this.clazz = clazzToSet;
	}

	public T findById( long id ){
		return entityManager.find( clazz, id );
	}


	public List< T > findAll(){		  
		TypedQuery< T > query = entityManager.createQuery("from " + clazz.getName(), clazz);
		return query.getResultList();	     
	}	

	public List< T > findAll(int firstResult, int maxResults){		  
		TypedQuery< T > query = entityManager.createQuery("from " + clazz.getName(), clazz);
		return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();	     
	}	 	 

	public Long count() {
		TypedQuery<Long> query = this.entityManager.createQuery("select count(c) from "+clazz.getName()+" c", Long.class);
		return query.getSingleResult();
	}

	@Transactional(noRollbackFor = Exception.class)
	public T save( T entity ){	  

		try{			 
			entityManager.persist( entity );			  
			entityManager.flush();
			
			return entity;
		}catch(Exception e){
			LOG.info(e.getMessage());
		}
		
		return null;
	}

	@Transactional(noRollbackFor = Exception.class)
	public T update( T entity ){
		return entityManager.merge( entity );	     
	}

	@Transactional(noRollbackFor = Exception.class)
	public void delete( T entity ){
		
		entityManager.remove( entity );
	}
	
	
	@Transactional(noRollbackFor = Exception.class)
	public void deleteById( long entityId ){
		T entity = findById( entityId );
		delete( entity );
	}
}
