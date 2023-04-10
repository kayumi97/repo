package org.easy.dao.inlabrad;

import java.util.List;

import org.easy.entity.inlabrad.Hospital;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


public interface HospitalDao{

	List<Hospital> findByIpSender(String ipSender);


	List<Hospital> findByNome(String nome);
	
	List<Hospital> findAll();

	
	List<Hospital> findAll(int firstResult, int maxResults);
	
	
	Hospital save(Hospital study);
	
	Hospital update(Hospital study);

	
	Long count();
	
	Hospital findById(long id);


	void deleteById(long id);
}
