package org.easy.dao.inlabrad;

import java.util.List;

import org.easy.entity.inlabrad.security.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UsuarioDao extends JpaRepository<Usuario, Long> {


	@Query(value="SELECT e from Usuario e  WHERE (e.login like CONCAT('%',:login,'%') or e.cpf = :login) and e.enabled = :enabled order by login")       // using @query
	public List<Usuario> findByEnabled(
			 @Param("login") String login, 
			 @Param("enabled") Boolean enabled);
	
	@Query(value="SELECT e from Usuario e  where e.login = :login")       // using @query
	public Usuario findByLogin(@Param("login") String login);
	
	
	@Query(value="SELECT e from Usuario e where e.login = :login")       // using @query
	public List<Usuario> doLogin(@Param("login") String login);


}